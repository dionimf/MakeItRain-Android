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

package com.jetheis.android.makeitrain.fragment;

import java.text.NumberFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jetheis.android.makeitrain.R;

public class ReportDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(activity);

        AlertDialog.Builder reportBuilder;

        LayoutInflater reportInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View reportLayout = reportInflater.inflate(R.layout.report_dialog_fragment, null);

        int spent = preferences.getInt(activity.getString(R.string.pref_total_spent), 0);

        NumberFormat nf = NumberFormat.getCurrencyInstance();
        String spentDisplay = nf.format(spent);

        TextView reportText = (TextView) reportLayout
                .findViewById(R.id.report_dialog_fragment_text_view);
        reportText.setText(activity.getString(R.string.total_spent, spentDisplay));

        reportBuilder = new AlertDialog.Builder(activity);
        reportBuilder.setView(reportLayout);
        reportBuilder.setTitle(R.string.your_spending_report);
        reportBuilder.setPositiveButton(R.string.im_so_cool, null);
        reportBuilder.setNegativeButton(R.string.reset, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                Editor editor = preferences.edit();
                editor.putInt(activity.getString(R.string.pref_total_spent), 0);
                editor.commit();
            }

        });

        return reportBuilder.create();
    }
}
