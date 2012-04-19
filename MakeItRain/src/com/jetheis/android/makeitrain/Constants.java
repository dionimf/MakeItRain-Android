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

package com.jetheis.android.makeitrain;

public class Constants {

    public static final String TAG = "MakeItRain";

    // Google Play product id
    public static final String GOOGLE_PLAY_PRODUCT_ID = "vip_status";

    // Google Play API version
    public static final int GOOGLE_PLAY_API_VERSION = 1;
    public static final String GOOGLE_PLAY_BIND_INTENT = "com.android.vending.billing.MarketBillingService.BIND";

    // Google Play bundle keys
    public static final String GOOGLE_PLAY_BUNDLE_KEY_BILLING_REQUEST = "BILLING_REQUEST";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_API_VERSION = "API_VERSION";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_PACKAGE_NAME = "PACKAGE_NAME";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_RESPONSE_CODE = "RESPONSE_CODE";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_REQUEST_ID = "REQUEST_ID";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_NOTIFY_IDS = "NOTIFY_IDS";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_ITEM_ID = "ITEM_ID";
    public static final String GOOGLE_PLAY_BUNDLE_KEY_NONCE = "NONCE";

    // Google Play billing request methods
    public static final String GOOGLE_PLAY_REQUEST_METHOD_CHECK_BILLING_SUPPORTED = "CHECK_BILLING_SUPPORTED";
    public static final String GOOGLE_PLAY_REQUEST_METHOD_REQUEST_PURCHASE = "REQUEST_PURCHASE";
    public static final String GOOGLE_PLAY_REQUEST_METHOD_GET_PURCHASE_INFORMATION = "GET_PURCHASE_INFORMATION";
    public static final String GOOGLE_PLAY_REQUEST_METHOD_RESTORE_TRANSACTIONS = "RESTORE_TRANSACTIONS";
    public static final String GOOGLE_PLAY_REQUEST_METHOD_CONFIRM_NOTIFICATIONS = "CONFIRM_NOTIFICATIONS";

    // Google Play intent keys

    // Google Play JSON keys

}
