/*
 * Copyright (C) 2012 Jimmy Theis. Licensed under the MIT License:
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jetheis.android.makeitrain.billing.googleplay;

import java.sql.Date;

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
import android.text.format.DateFormat;
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
            OnGooglePlayBillingReadyListener onReadyListener,
            OnGooglePlayVipModePurchaseFoundListener onPurchaseListener) {
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
            OnGooglePlayBillingReadyListener onReadyListener,
            OnGooglePlayVipModePurchaseFoundListener onPurchaseListener) {
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

        if (response == null
                || response.getInt("RESPONSE_CODE") != GooglePlayResponseCode.RESULT_OK.ordinal()) {
            return false;
        }

        return true;
    }

    public void requestVipStatus() {
        Bundle response;

        try {
            response = mBoundService.makeGooglePlayPurchaseRequest("vip_status");
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return;
        }

        if (response.getInt("RESPONSE_CODE") != GooglePlayResponseCode.RESULT_OK.ordinal()) {
            return;
        }

        Log.d(Constants.TAG, "Launching Google Play");

        PendingIntent pendingIntent = response.getParcelable("PURCHASE_INTENT");

        try {
            mContext.startIntentSender(pendingIntent.getIntentSender(), new Intent(), 0, 0, 0);
        } catch (SendIntentException e) {
            Log.e(Constants.TAG, "SendIntentException: " + e.getLocalizedMessage());
        }
    }

    public void requestPurchaseInfo(String[] notifyIds) {
        try {
            mBoundService.makeGooglePlayPurchaseInformationRequest(notifyIds);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return;
        }
    }

    public void restoreTransactions() {
        try {
            mBoundService.makeGooglePlayRestoreTransactionsRequest();
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
        }
    }

    public void sendNotificationConformation(String[] notificationIds) {  
        Log.v(Constants.TAG, "Confirming " + notificationIds.length + " notification(s)");

        try {
            mBoundService.makeGooglePlayConfirmNotificationsRequest(notificationIds);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
        }
    }

    public void unbind() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
            mConnection = null;
        }
    }

    public void handleJsonResponse(String response, String signature) {
        if (!GooglePlayBillingSecurity.isCorrectSignature(response, signature)) {
            Log.e(Constants.TAG, "Bad Google Play signature! Possible security breach!");
            return;
        }

        JSONObject responseJson;

        try {
            responseJson = new JSONObject(response);

            long nonce = responseJson.getLong("nonce");

            if (!GooglePlayBillingSecurity.isNonceKnown(nonce)) {
                Log.e(Constants.TAG, "Bad Google Play nonce! Possible security breach!");
                return;
            }

            Log.v(Constants.TAG, "Signature and nonce OK");

            JSONArray orders = responseJson.getJSONArray("orders");

            if (orders.length() == 0) {
                Log.v(Constants.TAG, "No orders present in response");
                return;
            }

            String[] notificationIds = new String[orders.length()];

            for (int i = 0; i < orders.length(); i++) {
                JSONObject order = orders.getJSONObject(i);

                String packageName = order.getString("packageName");
                if (!packageName.equals(mContext.getPackageName())) {
                    Log.e(Constants.TAG, "Bad Google Play package name! Possible security breach!");
                    return;
                }

                Log.v(Constants.TAG, "Package name OK");

                try {
                    notificationIds[i] = order.getString("notificationId");
                } catch (NumberFormatException e) {
                    Log.e(Constants.TAG,
                            "Found non-numerical notification ID: "
                                    + order.getString("notificationId") + ". Ignoring.");
                }

                String productId = order.getString("productId");

                Date purchaseDate = new Date(order.getLong("purchaseTime"));
                GooglePlayPurchaseState purchaseState = GooglePlayPurchaseState.fromInt(order
                        .getInt("purchaseState"));

                if (purchaseState == GooglePlayPurchaseState.PURCHASED) {

                    Log.i(Constants.TAG, "Found record of purchase of " + productId + " from "
                            + DateFormat.getLongDateFormat(mContext).format(purchaseDate));

                    if (productId.equals("vip_status")) {
                        mOnPurchaseListnener.onGooglePlayVipModePurchaseFound();
                    } else {
                        Log.e(Constants.TAG, "Product id " + productId + " not recognized");
                    }

                } else if (purchaseState == GooglePlayPurchaseState.CANCELLED) {

                } else {
                    Log.e(Constants.TAG, "Google Play refund attampted: unsupported");
                }
            }

            sendNotificationConformation(notificationIds);

        } catch (JSONException e) {
            Log.e(Constants.TAG, "JSONException: " + e.getLocalizedMessage());
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
