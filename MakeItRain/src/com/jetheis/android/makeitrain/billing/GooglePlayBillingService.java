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

package com.jetheis.android.makeitrain.billing;

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

public class GooglePlayBillingService extends Service implements ServiceConnection {

    private IMarketBillingService mService;
    private final IBinder mBinder = new GooglePlayBillingBinder();

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
            boolean bindResult = bindService(new Intent(
                    "com.android.vending.billing.MarketBillingService.BIND"), this,
                    BIND_AUTO_CREATE);
            if (!bindResult) {
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
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    protected Bundle makeGooglePlayRequestBundle(String method) {
        Bundle request = new Bundle();
        request.putString("BILLING_REQUEST", method);
        request.putInt("API_VERSION", 1);
        request.putString("PACKAGE_NAME", getPackageName());

        return request;
    }

    public Bundle makeGooglePlayRequest(String method) throws RemoteException {
        if (mService == null) {
            return null;
        }
        
        return mService.sendBillingRequest(makeGooglePlayRequestBundle(method));
    }

    public Bundle makeGooglePlayPurchaseRequest(String productId) throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle("REQUEST_PURCHASE");
        request.putString("ITEM_ID", productId);
        return mService.sendBillingRequest(request);
    }

    public Bundle makeGooglePlayPurchaseInformationRequest(String[] notifyIds)
            throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle("GET_PURCHASE_INFORMATION");
        request.putStringArray("NOTIFY_IDS", notifyIds);
        request.putLong("NONCE", GooglePlayBillingSecurity.generateNonce());

        return mService.sendBillingRequest(request);
    }

    public Bundle makeGooglePlayRestoreTransactionsRequest() throws RemoteException {
        Bundle request = makeGooglePlayRequestBundle("RESTORE_TRANSACTIONS");
        request.putLong("NONCE", GooglePlayBillingSecurity.generateNonce());

        return mService.sendBillingRequest(request);
    }

}
