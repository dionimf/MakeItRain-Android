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

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.jetheis.android.makeitrain.Constants;
import com.jetheis.android.makeitrain.billing.googleplay.GooglePlayBillingWrapper.GooglePlayResponseCode;

public class GooglePlayBillingService extends Service implements ServiceConnection {

    private IMarketBillingService mService;
    private final IBinder mBinder = new GooglePlayBillingBinder();
    private boolean mWillConnect;
    private OnGooglePlayBillingSupportResultListener mOnSupportListener;

    public class GooglePlayBillingBinder extends Binder {
        GooglePlayBillingService getService() {
            return GooglePlayBillingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mWillConnect = bindService(new Intent(Constants.GOOGLE_PLAY_BIND_INTENT), this,
                    BIND_AUTO_CREATE);
            if (!mWillConnect) {
                Log.e(Constants.TAG, "Could not bind to the Google Play service");
            }
        } catch (SecurityException e) {
            Log.e(Constants.TAG, "Security exception: " + e);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(Constants.TAG, "Google Play service connected");
        mService = IMarketBillingService.Stub.asInterface(service);
        
        if (mOnSupportListener != null) {
            Log.v(Constants.TAG, "Notifying listener of Google Play connection");
            mOnSupportListener.onGooglePlayBillingSupportResultFound(isBillingSupported());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    protected Bundle makeGooglePlayRequestBundle(String method) {
        Bundle request = new Bundle();
        request.putString(Constants.GOOGLE_PLAY_BUNDLE_KEY_BILLING_REQUEST, method);
        request.putInt(Constants.GOOGLE_PLAY_BUNDLE_KEY_API_VERSION,
                Constants.GOOGLE_PLAY_API_VERSION);
        request.putString(Constants.GOOGLE_PLAY_BUNDLE_KEY_PACKAGE_NAME, getPackageName());

        return request;
    }

    public Bundle makeGooglePlayRequest(String method) throws RemoteException {
        if (mService == null) {
            Log.e(Constants.TAG, "Google Play service not connected");
            return null;
        }

        return sendBillingRequest(makeGooglePlayRequestBundle(method));
    }

    private Bundle sendBillingRequest(Bundle request) throws RemoteException {

        Bundle result = mService.sendBillingRequest(request);

        String method = request.getString(Constants.GOOGLE_PLAY_BUNDLE_KEY_BILLING_REQUEST);
        int responseCode = result.getInt(Constants.GOOGLE_PLAY_BUNDLE_KEY_RESPONSE_CODE);

        if (responseCode == GooglePlayResponseCode.RESULT_OK.ordinal()) {
            Log.v(Constants.TAG,
                    "Sent Google Play " + method + " request ("
                            + result.getLong(Constants.GOOGLE_PLAY_BUNDLE_KEY_REQUEST_ID) + "): "
                            + GooglePlayResponseCode.fromInt(responseCode));
        } else {
            Log.e(Constants.TAG,
                    "Sent Google Play " + method + " request ("
                            + result.getLong(Constants.GOOGLE_PLAY_BUNDLE_KEY_REQUEST_ID) + "): "
                            + GooglePlayResponseCode.fromInt(responseCode));
        }

        return result;
    }

    public void checkIsBillingSupported(OnGooglePlayBillingSupportResultListener onSupportListener) {
        if (mService == null && mWillConnect) {
            Log.v(Constants.TAG, "Google Play service not ready. Registering listener");
            mOnSupportListener = onSupportListener;
        } else if (mService == null) {
            Log.v(Constants.TAG, "No Google Play connection found. Notifying of no support");
            onSupportListener.onGooglePlayBillingSupportResultFound(false);
        } else {
            Log.v(Constants.TAG, "Google Play already connected. Checking support");
            onSupportListener.onGooglePlayBillingSupportResultFound(isBillingSupported());
        }
    }

    private boolean isBillingSupported() {
        try {
            Bundle response = makeGooglePlayRequest(Constants.GOOGLE_PLAY_REQUEST_METHOD_CHECK_BILLING_SUPPORTED);
            return response.getInt(Constants.GOOGLE_PLAY_BUNDLE_KEY_RESPONSE_CODE) == GooglePlayResponseCode.RESULT_OK.ordinal();
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "RemoteException: " + e.getLocalizedMessage());
            return false;
        }
    }

    public Bundle makeGooglePlayPurchaseRequest(String productId) throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle(Constants.GOOGLE_PLAY_REQUEST_METHOD_REQUEST_PURCHASE);
        request.putString(Constants.GOOGLE_PLAY_BUNDLE_KEY_ITEM_ID, productId);
        return sendBillingRequest(request);
    }

    public Bundle makeGooglePlayPurchaseInformationRequest(String[] notifyIds)
            throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle(Constants.GOOGLE_PLAY_REQUEST_METHOD_GET_PURCHASE_INFORMATION);
        request.putStringArray(Constants.GOOGLE_PLAY_BUNDLE_KEY_NOTIFY_IDS, notifyIds);
        request.putLong(Constants.GOOGLE_PLAY_BUNDLE_KEY_NONCE,
                GooglePlayBillingSecurity.generateNonce());

        return sendBillingRequest(request);
    }

    public Bundle makeGooglePlayRestoreTransactionsRequest() throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle(Constants.GOOGLE_PLAY_REQUEST_METHOD_RESTORE_TRANSACTIONS);
        request.putLong(Constants.GOOGLE_PLAY_BUNDLE_KEY_NONCE,
                GooglePlayBillingSecurity.generateNonce());

        return sendBillingRequest(request);
    }

    public Bundle makeGooglePlayConfirmNotificationsRequest(String[] notificationIds)
            throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle(Constants.GOOGLE_PLAY_REQUEST_METHOD_CONFIRM_NOTIFICATIONS);
        request.putStringArray(Constants.GOOGLE_PLAY_BUNDLE_KEY_NOTIFY_IDS, notificationIds);

        return sendBillingRequest(request);
    }

    public interface OnGooglePlayBillingSupportResultListener {
        public void onGooglePlayBillingSupportResultFound(boolean billingSupported);
    }

}
