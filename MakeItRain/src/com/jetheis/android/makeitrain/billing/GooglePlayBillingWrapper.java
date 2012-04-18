package com.jetheis.android.makeitrain.billing;

import java.sql.Date;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.jetheis.android.makeitrain.Constants;

public class GooglePlayBillingWrapper {

    public enum GooglePlayResponseCode {
        RESULT_OK, RESULT_USER_CANCELED, RESULT_SERVICE_UNAVAILABLE, RESULT_BILLING_UNAVAILABLE, RESULT_ITEM_UNAVAILABLE, RESULT_DEVELOPER_ERROR, RESULT_ERROR;

        public static GooglePlayResponseCode fromInt(int ord) {
            GooglePlayResponseCode[] values = GooglePlayResponseCode.values();
            if (ord < 0 || ord >= values.length) {
                return RESULT_ERROR;
            }
            return values[ord];
        }
    }

    public enum GooglePlayPurchaseState {
        PURCHASED, CANCELLED, REFUNDED;

        public static GooglePlayPurchaseState fromInt(int ord) {
            GooglePlayPurchaseState[] values = GooglePlayPurchaseState.values();
            if (ord < 0 || ord >= values.length) {
                return CANCELLED;
            }
            return values[ord];
        }
    }

    private static GooglePlayBillingWrapper sInstance;

    private ServiceConnection mConnection;
    private GooglePlayBillingService mBoundService;
    private Context mContext;
    private OnGooglePlayBillingReadyListener mOnReadyListener;
    private OnGooglePlayVipModePurchaseFoundListener mOnPurchaseListnener;

    public static GooglePlayBillingWrapper initializeInstance(Context context,
            OnGooglePlayBillingReadyListener onReadyListener, OnGooglePlayVipModePurchaseFoundListener onPurchaseListener) {
        if (sInstance == null) {
            sInstance = new GooglePlayBillingWrapper(context, onReadyListener, onPurchaseListener);
        } else {
            throw new IllegalStateException("Billing wrapper already initialized");
        }

        return sInstance;
    }

    public static GooglePlayBillingWrapper getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Billing wrapper has not been initialized yet");
        }

        return sInstance;
    }
    
    public static void destroyInstance() {
        if (sInstance != null) {
            sInstance.unbind();
        }
        
        sInstance = null;
    }

    private GooglePlayBillingWrapper(Context context,
            OnGooglePlayBillingReadyListener onReadyListener, OnGooglePlayVipModePurchaseFoundListener onPurchaseListener) {
        mContext = context;
        mOnReadyListener = onReadyListener;
        mOnPurchaseListnener = onPurchaseListener;

        context.startService(new Intent(context, GooglePlayBillingService.class));

        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBoundService = ((GooglePlayBillingService.GooglePlayBillingBinder) service)
                        .getService();
                if (isBillingSupported()) {
                    Log.i(Constants.TAG, "Google Play billing ready");
                    mOnReadyListener.onGooglePlayBillingReady();
                } else {
                    Log.i(Constants.TAG, "Google Play billing is not supported");
                    mOnReadyListener.onGooglePlayBillingNotSupported();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBoundService = null;
            }

        };

        mContext.bindService(new Intent(context, GooglePlayBillingService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public boolean isBillingSupported() {
        Bundle response;

        try {
            response = mBoundService.makeGooglePlayRequest("CHECK_BILLING_SUPPORTED");
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return false;
        }
        
        if (response == null) {
            return false;
        }

        int responseCode = response.getInt("RESPONSE_CODE");

        if (responseCode != GooglePlayResponseCode.RESULT_OK.ordinal()) {
            Log.e(Constants.TAG,
                    "Google Play billing support response: "
                            + GooglePlayResponseCode.fromInt(responseCode));
            return false;
        }

        return true;
    }

    public void requestVipStatus() {
        Bundle response;

        try {
            response = mBoundService.makeGooglePlayPurchaseRequest("android.test.purchased");
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return;
        }

        int responseCode = response.getInt("RESPONSE_CODE");

        if (responseCode == GooglePlayResponseCode.RESULT_OK.ordinal()) {
            Log.d(Constants.TAG, "Google Play purchase request: " + response.getLong("REQUEST_ID"));
        } else {
            Log.e(Constants.TAG,
                    "Google Play purchase response: "
                            + GooglePlayResponseCode.fromInt(responseCode));
            return;
        }

        Log.i(Constants.TAG, "Launching Google Play");

        PendingIntent pendingIntent = response.getParcelable("PURCHASE_INTENT");

        try {
            mContext.startIntentSender(pendingIntent.getIntentSender(), new Intent(), 0, 0, 0);
        } catch (SendIntentException e) {
            Log.e(Constants.TAG, "SendIntentException: " + e.getLocalizedMessage());
        }
    }

    public void requestPurchaseInfo(String[] notifyIds) {
        Bundle response;
        try {
            response = mBoundService.makeGooglePlayPurchaseInformationRequest(notifyIds);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return;
        }

        int responseCode = response.getInt("RESPONSE_CODE");

        if (responseCode == GooglePlayResponseCode.RESULT_OK.ordinal()) {
            Log.d(Constants.TAG, "Google Play purchase info request sent");
        } else {
            Log.e(Constants.TAG,
                    "Google Play purchase info response: "
                            + GooglePlayResponseCode.fromInt(responseCode));
        }
    }

    public void restoreTransactions() {
        Bundle response;

        try {
            response = mBoundService.makeGooglePlayRestoreTransactionsRequest();
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return;
        }

        int responseCode = response.getInt("RESPONSE_CODE");

        if (responseCode == GooglePlayResponseCode.RESULT_OK.ordinal()) {
            Log.d(Constants.TAG, "Google Play restore transactions request sent");
        } else {
            Log.e(Constants.TAG, "Google Play restore transactions response: "
                    + GooglePlayResponseCode.fromInt(responseCode));
            return;
        }
    }

    public void unbind() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
            mConnection = null;
        }
    }

    public void handleJsonResponse(String response, String signature) {
        if (GooglePlayBillingSecurity.isCorrectSignature(response, signature)) {
            Log.d(Constants.TAG, "Signature good");
        } else {
            Log.e(Constants.TAG, "Bad Google Play signature! Possible security breach!");
            return;
        }
        
        JSONObject responseJson;

        try {
            responseJson = new JSONObject(response);

            long nonce = responseJson.getLong("nonce");

            if (GooglePlayBillingSecurity.isNonceKnown(nonce)) {
                Log.d(Constants.TAG, "Nonce good");
            } else {
                Log.e(Constants.TAG, "Bad Google Play nonce! Possible security breach!");
                return;
            }

            JSONArray orders = responseJson.getJSONArray("orders");

            for (int i = 0; i < orders.length(); i++) {
                JSONObject order = orders.getJSONObject(i);
                
                String packageName = order.getString("packageName");
                if (packageName.equals(mContext.getPackageName())) {
                    Log.d(Constants.TAG, "Package name good");
                } else {
                    Log.e(Constants.TAG, "Bad Google Play package name! Possible security breach!");
                    return;
                }

                String productId = order.getString("productId");
                
                Date purchaseDate = new Date(order.getLong("purchaseTime"));
                Calendar purchaseTime = Calendar.getInstance();
                purchaseTime.setTime(purchaseDate);
                
                GooglePlayPurchaseState purchaseState = GooglePlayPurchaseState.fromInt(order.getInt("purchaseState"));
                
                if (purchaseState == GooglePlayPurchaseState.PURCHASED) {
                    
                    Log.d(Constants.TAG, "Found record of purchase of " + productId + " at " + purchaseTime.toString());
                    
                    if (productId.equals("android.test.purchased")) {
                        mOnPurchaseListnener.onGooglePlayVipModePurchaseFound();
                    } else {
                        Log.e(Constants.TAG, "Product id " + productId + " not recognized");
                    }
                    
                } else if (purchaseState == GooglePlayPurchaseState.CANCELLED) {
                    
                } else {
                    Log.e(Constants.TAG, "Google Play refund attampted: unspported");
                }
            }

        } catch (JSONException e) {
            Log.e(Constants.TAG, "JSONException: " + e.getLocalizedMessage());
            return;
        }

    }

    public static interface OnGooglePlayBillingReadyListener {
        public void onGooglePlayBillingReady();
        public void onGooglePlayBillingNotSupported();
    }
    
    public static interface OnGooglePlayVipModePurchaseFoundListener {
        public void onGooglePlayVipModePurchaseFound();
    }

}
