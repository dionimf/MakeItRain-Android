package com.jetheis.android.makeitrain.billing;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;

public class BillingService extends Service implements ServiceConnection {

    private static final String TAG = "MakeItRain.BillingService";

    private Context mContext;
    private IMarketBillingService mService;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            boolean bindResult = mContext.bindService(new Intent(
                    "com.android.vending.billing.MarketBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
            if (bindResult) {
                Log.d(TAG, "Service bind successful.");
            } else {
                Log.d(TAG, "Could not bind to the MarketBillingService.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "MarketBillingService connected.");
        mService = IMarketBillingService.Stub.asInterface(service);
    }

    public void onServiceDisconnected(ComponentName name) {
        // TODO Auto-generated method stub

    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
