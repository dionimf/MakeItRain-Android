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

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jetheis.android.makeitrain.fragment.AboutDialogFragment;
import com.jetheis.android.makeitrain.fragment.DenominationDialogFragment;
import com.jetheis.android.makeitrain.fragment.DenominationDialogFragment.OnDenominationChosenListener;
import com.jetheis.android.makeitrain.fragment.OrientationDialogFragment;
import com.jetheis.android.makeitrain.fragment.OrientationDialogFragment.OnOrientationChosenListener;

public class MakeItRainActivity extends FragmentActivity {

    private static final int DIALOG_REPORT = 4;
    private static final int DIALOG_VIP = 5;

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
        mRightMap.put(mResources.getString(R.string.denomination_100_vip),
                R.drawable.bill_100_right);

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

        mBillView = (BillView) findViewById(R.id.bills);
        reloadBillAndSave();
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
            DenominationDialogFragment denominationDialog = new DenominationDialogFragment(
                    mDenomination, new OnDenominationChosenListener() {

                        @Override
                        public void onDenominationChosen(String denomination) {
                            mDenomination = denomination;
                            reloadBillAndSave();
                        }
                    });

            denominationDialog.show(getSupportFragmentManager(), "denonimation"); // TODO:
                                                                                  // Tag
            return true;
        case R.id.menu_orientation:
            OrientationDialogFragment orientationDialog = new OrientationDialogFragment(
                    mOrientation, new OnOrientationChosenListener() {

                        @Override
                        public void onOrientationChosen(String orientation) {
                            mOrientation = orientation;
                            reloadBillAndSave();
                        }

                    });

            orientationDialog.show(getSupportFragmentManager(), "orientation"); // TODO:
                                                                                // Tag
            return true;
        case R.id.menu_report:
            showDialog(DIALOG_REPORT);
            return true;
        case R.id.menu_about:
            new AboutDialogFragment().show(getSupportFragmentManager(), "about"); // TODO:
                                                                                  // Tag
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {

        case DIALOG_REPORT:
            AlertDialog.Builder reportBuilder;

            LayoutInflater reportInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
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

        case DIALOG_VIP:
            AlertDialog.Builder vipBuilder;

            LayoutInflater vipInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View vipLayout = vipInflater.inflate(R.layout.about_dialog,
                    (ViewGroup) findViewById(R.id.about_layout));

            TextView vipText = (TextView) vipLayout.findViewById(R.id.about_text);
            vipText.setText("You need to be a VIP to do that!\n\n - No ads\n -$50 and $100 bills\n - Makes you cooler\n - Only costs a dollar!");
            vipBuilder = new AlertDialog.Builder(this);
            vipBuilder.setView(vipLayout);
            vipBuilder.setTitle("You're Not Cool Enough Yet!");
            vipBuilder.setPositiveButton("Yeah, I'm cool!", null);
            vipBuilder.setNegativeButton("No, I'm cheap.", null);
            dialog = vipBuilder.create();
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
            ((TextView) ((AlertDialog) dialog).findViewById(R.id.about_text)).setText(mResources
                    .getString(R.string.total_spent, spentDisplay));
        }
        super.onPrepareDialog(id, dialog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}