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
import java.util.ArrayList;
import java.util.List;

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
import com.jetheis.android.makeitrain.billing.googleplay.GooglePlayBillingService.OnGooglePlayBillingSupportResultListener;

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
    
    public static boolean isInitialized() {
        return sInstance == null;
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
                mBoundService
                        .checkIsBillingSupported(new OnGooglePlayBillingSupportResultListener() {

                            @Override
                            public void onGooglePlayBillingSupportResultFound(
                                    boolean billingSupported) {
                                if (billingSupported) {
                                    Log.i(Constants.TAG, "Google Play billing ready");
                                    if (mOnReadyListener != null) {
                                        mOnReadyListener.onGooglePlayBillingReady();
                                    }
                                } else {
                                    Log.i(Constants.TAG, "Google Play billing is not supported");
                                    if (mOnReadyListener != null) {
                                        mOnReadyListener.onGooglePlayBillingNotSupported();
                                    }
                                }
                            }

                        });

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBoundService = null;
            }

        };

        mContext.bindService(new Intent(context, GooglePlayBillingService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void requestVipStatus() {
        Bundle response;

        try {
            response = mBoundService
                    .makeGooglePlayPurchaseRequest(Constants.GOOGLE_PLAY_PRODUCT_ID);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return;
        }

        if (response.getInt(Constants.GOOGLE_PLAY_BUNDLE_KEY_RESPONSE_CODE) != GooglePlayResponseCode.RESULT_OK
                .ordinal()) {
            return;
        }

        PendingIntent pendingIntent = response
                .getParcelable(Constants.GOOGLE_PLAY_BUNDLE_KEY_PURCHASE_INTENT);

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
        Log.v(Constants.TAG, "Handling JSON response: " + response);

        if (!GooglePlayBillingSecurity.isCorrectSignature(response, signature)) {
            Log.e(Constants.TAG, "Bad Google Play signature! Possible security breach!");
            return;
        }

        JSONObject responseJson;

        try {
            responseJson = new JSONObject(response);

            long nonce = responseJson.getLong(Constants.GOOGLE_PLAY_JSON_KEY_NONCE);

            if (!GooglePlayBillingSecurity.isNonceKnown(nonce)) {
                Log.e(Constants.TAG, "Bad Google Play nonce! Possible security breach!");
                return;
            }

            Log.v(Constants.TAG, "Signature and nonce OK");

            JSONArray orders = responseJson.getJSONArray(Constants.GOOGLE_PLAY_JSON_KEY_ORDERS);

            if (orders.length() == 0) {
                Log.v(Constants.TAG, "No orders present in response");
                return;
            }

            List<String> notificationIds = new ArrayList<String>(orders.length());

            for (int i = 0; i < orders.length(); i++) {
                JSONObject order = orders.getJSONObject(i);

                String packageName = order.getString(Constants.GOOGLE_PLAY_JSON_KEY_PACKAGE_NAME);
                if (!packageName.equals(mContext.getPackageName())) {
                    Log.e(Constants.TAG, "Bad Google Play package name! Possible security breach!");
                    return;
                }

                Log.v(Constants.TAG, "Package name OK");

                if (order.has(Constants.GOOGLE_PLAY_JSON_KEY_NOTIFICATION_ID)) {
                    notificationIds.add(order
                            .getString(Constants.GOOGLE_PLAY_JSON_KEY_NOTIFICATION_ID));

                }

                String productId = order.getString(Constants.GOOGLE_PLAY_JSON_KEY_PRODUCT_ID);

                Date purchaseDate = new Date(
                        order.getLong(Constants.GOOGLE_PLAY_JSON_KEY_PURCHASE_TIME));
                GooglePlayPurchaseState purchaseState = GooglePlayPurchaseState.fromInt(order
                        .getInt(Constants.GOOGLE_PLAY_JSON_KEY_PURCHASE_STATE));

                if (purchaseState == GooglePlayPurchaseState.PURCHASED) {

                    Log.i(Constants.TAG, "Found record of purchase of " + productId + " from "
                            + DateFormat.getLongDateFormat(mContext).format(purchaseDate));

                    if (productId.equals(Constants.GOOGLE_PLAY_PRODUCT_ID)) {
                        if (mOnPurchaseListnener != null) {
                            mOnPurchaseListnener.onGooglePlayVipModePurchaseFound();
                        }
                    } else {
                        Log.e(Constants.TAG, "Product id " + productId + " not recognized");
                    }

                } else if (purchaseState == GooglePlayPurchaseState.CANCELLED) {
                    Log.i(Constants.TAG, "User cancelled purchase");
                } else {
                    Log.e(Constants.TAG, "Google Play refund attempted: unsupported");
                }
            }

            if (notificationIds.size() > 0) {
                sendNotificationConformation(notificationIds.toArray(new String[notificationIds
                        .size()]));
            }

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
