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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
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
    private static final int DIALOG_VIP = 4;

    private AdView mAdView;
    private Resources mResources;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mResources = getResources();

        mAdView = (AdView) findViewById(R.id.adView);

        AdRequest request = new AdRequest();

        request.addTestDevice(AdRequest.TEST_EMULATOR);
        request.addTestDevice("D7C5C55307D200C174CDFD03D70E281C"); // Aria

        mAdView.loadAd(request);
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

                AlertDialog.Builder denominationBuilder = new AlertDialog.Builder(this);
                denominationBuilder.setTitle(R.string.choose_a_denomination);
                denominationBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (items[item].equals(R.string.denomination_50_vip)
                                || items[item].equals(R.string.denomination_100_vip)) {
                            showDialog(DIALOG_VIP);
                        }
                    }
                });
                dialog = denominationBuilder.create();
                break;
            case DIALOG_ORIENTATION:
                final CharSequence[] orientations = { mResources.getString(R.string.orientation_left),
                        mResources.getString(R.string.orientation_right) };

                AlertDialog.Builder orientationBuilder = new AlertDialog.Builder(this);
                orientationBuilder.setTitle(R.string.choose_an_orientation);
                orientationBuilder.setSingleChoiceItems(orientations, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                });
                dialog = orientationBuilder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }
}