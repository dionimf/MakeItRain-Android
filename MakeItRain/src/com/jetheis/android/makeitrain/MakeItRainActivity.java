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

    private AdView mAdView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
                return true;
            case R.id.menu_orientation:
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
                View layout = inflater.inflate(R.layout.about_dialog,
                                               (ViewGroup) findViewById(R.id.about_layout));

                TextView text = (TextView) layout.findViewById(R.id.about_text);
                text.setText("Make It Rain is a just-for-fun Android app.\n\nIt's released under the Apache License 2.0, making it completely free and open source.\n\nThis version is ad-supported, but you're welcome to modify, build, and distribute it however you'd like.\n\nSpecial thanks for the idea to Eric Overton and Jeremy Davis.");
                builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                builder.setTitle("About Make It Rain");
                builder.setPositiveButton("Cool, thanks!", null);
                dialog = builder.create();
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