package com.jetheis.android.makeitrain.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jetheis.android.makeitrain.Constants;
import com.jetheis.android.makeitrain.billing.GooglePlayBillingWrapper.GooglePlayResponseCode;

public class GooglePlayBillingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("com.android.vending.billing.RESPONSE_CODE")) {
            int responseCode = intent.getIntExtra("response_code", -1);
            long requestId = intent.getLongExtra("request_id", -1);

            if (responseCode == GooglePlayResponseCode.RESULT_OK.ordinal()) {
                Log.d(Constants.TAG, "Google Play response (request " + requestId + "): "
                        + GooglePlayResponseCode.fromInt(responseCode));
            } else {
                Log.e(Constants.TAG, "Google Play response (request " + requestId + "): "
                        + GooglePlayResponseCode.fromInt(responseCode));
                return;
            }
        } else if (action.equals("com.android.vending.billing.IN_APP_NOTIFY")) {
            String notifyId = intent.getStringExtra("notification_id");

            Log.d(Constants.TAG, "Requesting purchase state for IN_APP_NOTIFY item: " + notifyId);

            GooglePlayBillingWrapper.getInstance().requestPurchaseInfo(new String[] { notifyId });
        } else if (action.equals("com.android.vending.billing.PURCHASE_STATE_CHANGED")) {
            Log.d(Constants.TAG, "Received PURCHASE_STATE_CHANGED pending intent");

            String signedData = intent.getStringExtra("inapp_signed_data");
            String signature = intent.getStringExtra("inapp_signature");

            Log.d(Constants.TAG, "Signed data: " + signedData);
            Log.d(Constants.TAG, "Signature: " + signature);
            
            GooglePlayBillingWrapper.getInstance().handleJsonResponse(signedData, signature);
        } else {
            Log.e(Constants.TAG, "Unexpected response from Google Play: " + action);
        }
    }

}
