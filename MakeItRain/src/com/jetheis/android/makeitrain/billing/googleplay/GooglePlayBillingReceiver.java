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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jetheis.android.makeitrain.Constants;
import com.jetheis.android.makeitrain.billing.googleplay.GooglePlayBillingWrapper.GooglePlayResponseCode;

public class GooglePlayBillingReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        Log.v(Constants.TAG, "Received Google Play intent: " + action);

        // RESPONSE_CODE
        if (action.equals("com.android.vending.billing.RESPONSE_CODE")) {
            int responseCode = intent.getIntExtra("response_code", -1);
            long requestId = intent.getLongExtra("request_id", -1);

            if (responseCode == GooglePlayResponseCode.RESULT_OK.ordinal()) {
                Log.v(Constants.TAG, "Google Play RESPONSE_CODE (request " + requestId + "): "
                        + GooglePlayResponseCode.fromInt(responseCode));
            } else {
                Log.e(Constants.TAG, "Google Play RESPONSE_CODE (request " + requestId + "): "
                        + GooglePlayResponseCode.fromInt(responseCode));
                return;
            }
            
        // IN_APP_NOTIFY
        } else if (action.equals("com.android.vending.billing.IN_APP_NOTIFY")) {
            String notifyId = intent.getStringExtra("notification_id");

            Log.v(Constants.TAG, "Requesting purchase state for IN_APP_NOTIFY notification: " + notifyId);

            GooglePlayBillingWrapper.getInstance().requestPurchaseInfo(new String[] { notifyId });
         
        // PURCHASE_STATE_CHANGED
        } else if (action.equals("com.android.vending.billing.PURCHASE_STATE_CHANGED")) {
            String signedData = intent.getStringExtra("inapp_signed_data");
            String signature = intent.getStringExtra("inapp_signature");

            GooglePlayBillingWrapper.getInstance().handleJsonResponse(signedData, signature);
           
        // Unknown
        } else {
            Log.e(Constants.TAG, "Unexpected response from Google Play: " + action);
        }
    }

}
