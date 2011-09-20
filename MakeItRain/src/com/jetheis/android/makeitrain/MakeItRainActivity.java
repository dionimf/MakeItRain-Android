//
//  Copyright 2011 Jimmy Theis
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package com.jetheis.android.makeitrain;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MakeItRainActivity extends Activity {

    private static final int DIALOG_ABOUT = 1;
    private static final int DIALOG_DENOMINATION = 2;
    private static final int DIALOG_ORIENTATION = 3;
    private static final int DIALOG_REPORT = 4;
    private static final int DIALOG_VIP = 5;

    private AdView mAdView;
    private BillView mBillView;
    private Resources mResources;
    private SharedPreferences mPrefs;

    private Map<String, Integer> mLeftMap;
    private Map<String, Integer> mRightMap;
    private Map<String, Integer> mDenominationValues;

    private String mOrientation;
    private String mDenomination;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mResources = getResources();

        mLeftMap = new HashMap<String, Integer>();
        mLeftMap.put(mResources.getString(R.string.denomination_1), R.drawable.bill_1_left);
        mLeftMap.put(mResources.getString(R.string.denomination_5), R.drawable.bill_5_left);
        mLeftMap.put(mResources.getString(R.string.denomination_10), R.drawable.bill_10_left);
        mLeftMap.put(mResources.getString(R.string.denomination_20), R.drawable.bill_20_left);
        mLeftMap.put(mResources.getString(R.string.denomination_50_vip), R.drawable.bill_50_left);
        mLeftMap.put(mResources.getString(R.string.denomination_100_vip), R.drawable.bill_100_left);

        mRightMap = new HashMap<String, Integer>();
        mRightMap.put(mResources.getString(R.string.denomination_1), R.drawable.bill_1_right);
        mRightMap.put(mResources.getString(R.string.denomination_5), R.drawable.bill_5_right);
        mRightMap.put(mResources.getString(R.string.denomination_10), R.drawable.bill_10_right);
        mRightMap.put(mResources.getString(R.string.denomination_20), R.drawable.bill_20_right);
        mRightMap.put(mResources.getString(R.string.denomination_50_vip), R.drawable.bill_50_right);
        mRightMap.put(mResources.getString(R.string.denomination_100_vip), R.drawable.bill_100_right);

        mDenominationValues = new HashMap<String, Integer>();
        mDenominationValues.put(mResources.getString(R.string.denomination_1), 1);
        mDenominationValues.put(mResources.getString(R.string.denomination_5), 5);
        mDenominationValues.put(mResources.getString(R.string.denomination_10), 10);
        mDenominationValues.put(mResources.getString(R.string.denomination_20), 20);
        mDenominationValues.put(mResources.getString(R.string.denomination_50_vip), 50);
        mDenominationValues.put(mResources.getString(R.string.denomination_100_vip), 100);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mOrientation = mPrefs.getString(getString(R.string.prefs_orientation),
                mResources.getString(R.string.orientation_left));
        mDenomination = mPrefs.getString(getString(R.string.prefs_denomination),
                mResources.getString(R.string.denomination_1));

        mAdView = (AdView) findViewById(R.id.adView);
        mBillView = (BillView) findViewById(R.id.bills);
        reloadBillAndSave();

        AdRequest request = new AdRequest();

        request.addTestDevice(AdRequest.TEST_EMULATOR);
        request.addTestDevice("D7C5C55307D200C174CDFD03D70E281C"); // Jimmy's
                                                                   // HTC Aria
        mAdView.loadAd(request);
    }

    public void reloadBillAndSave() {
        if (mOrientation.equals(mResources.getString(R.string.orientation_left))) {
            mBillView.setImageResource(mLeftMap.get(mDenomination));
        } else {
            mBillView.setImageResource(mRightMap.get(mDenomination));
        }

        mBillView.setDenomination(mDenominationValues.get(mDenomination));

        Editor editor = mPrefs.edit();
        editor.putString(getString(R.string.prefs_orientation), mOrientation);
        editor.putString(getString(R.string.prefs_denomination), mDenomination);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_denomination:
                showDialog(DIALOG_DENOMINATION);
                return true;
            case R.id.menu_orientation:
                showDialog(DIALOG_ORIENTATION);
                return true;
            case R.id.menu_report:
                showDialog(DIALOG_REPORT);
                return true;
            case R.id.menu_about:
                showDialog(DIALOG_ABOUT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case DIALOG_ABOUT:
                AlertDialog.Builder builder;

                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, (ViewGroup) findViewById(R.id.about_layout));

                TextView text = (TextView) layout.findViewById(R.id.about_text);
                text.setText(R.string.about_text);
                builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                builder.setTitle(R.string.about_make_it_rain);
                builder.setPositiveButton(R.string.cool_thanks, null);
                dialog = builder.create();
                break;

            case DIALOG_DENOMINATION:
                final CharSequence[] items = { mResources.getString(R.string.denomination_1),
                        mResources.getString(R.string.denomination_5), mResources.getString(R.string.denomination_10),
                        mResources.getString(R.string.denomination_20),
                        mResources.getString(R.string.denomination_50_vip),
                        mResources.getString(R.string.denomination_100_vip) };

                int currentIndex = Arrays.asList(items).indexOf(mDenomination);

                AlertDialog.Builder denominationBuilder = new AlertDialog.Builder(this);
                denominationBuilder.setTitle(R.string.choose_a_denomination);
                denominationBuilder.setSingleChoiceItems(items, currentIndex, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (items[item].equals(R.string.denomination_50_vip)
                                || items[item].equals(R.string.denomination_100_vip)) {
                            showDialog(DIALOG_VIP);
                        } else {
                            mDenomination = items[item].toString();
                            reloadBillAndSave();
                        }
                    }
                });
                dialog = denominationBuilder.create();
                break;

            case DIALOG_ORIENTATION:
                final CharSequence[] orientations = { mResources.getString(R.string.orientation_left),
                        mResources.getString(R.string.orientation_right) };

                int currentOrientation = Arrays.asList(orientations).indexOf(mOrientation);

                AlertDialog.Builder orientationBuilder = new AlertDialog.Builder(this);
                orientationBuilder.setTitle(R.string.choose_an_orientation);
                orientationBuilder.setSingleChoiceItems(orientations, currentOrientation,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                mOrientation = orientations[item].toString();
                                reloadBillAndSave();
                                dialog.dismiss();
                            }
                        });
                dialog = orientationBuilder.create();
                break;

            case DIALOG_REPORT:
                AlertDialog.Builder reportBuilder;

                LayoutInflater reportInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View reportLayout = reportInflater.inflate(R.layout.about_dialog,
                        (ViewGroup) findViewById(R.id.about_layout));

                int spent = mPrefs.getInt(mResources.getString(R.string.pref_total_spent), 0);

                NumberFormat nf = NumberFormat.getCurrencyInstance();
                String spentDisplay = nf.format(spent);

                TextView reportText = (TextView) reportLayout.findViewById(R.id.about_text);
                reportText.setText(mResources.getString(R.string.total_spent, spentDisplay));
                reportBuilder = new AlertDialog.Builder(this);
                reportBuilder.setView(reportLayout);
                reportBuilder.setTitle(R.string.your_spending_report);
                reportBuilder.setPositiveButton(R.string.im_so_cool, null);
                reportBuilder.setNegativeButton(R.string.reset, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        Editor editor = mPrefs.edit();
                        editor.putInt(mResources.getString(R.string.pref_total_spent), 0);
                        editor.commit();
                    }
                });
                dialog = reportBuilder.create();
                break;

            default:
                dialog = null;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_REPORT:
                int spent = mPrefs.getInt(mResources.getString(R.string.pref_total_spent), 0);
                NumberFormat nf = NumberFormat.getCurrencyInstance();
                String spentDisplay = nf.format(spent);
                ((TextView) ((AlertDialog) dialog).findViewById(R.id.about_text)).setText(mResources.getString(
                        R.string.total_spent, spentDisplay));
        }
        super.onPrepareDialog(id, dialog);
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }
}