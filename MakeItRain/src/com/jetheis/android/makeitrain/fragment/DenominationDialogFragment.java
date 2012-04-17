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

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jetheis.android.makeitrain.R;

public class DenominationDialogFragment extends DialogFragment {

    private CharSequence mCurrentDenomination;
    private OnDenominationChosenListener mOnDenominationChosenListener;

    public DenominationDialogFragment(CharSequence currentDenomination,
            OnDenominationChosenListener onDenominationChosenListener) {
        mCurrentDenomination = currentDenomination;
        mOnDenominationChosenListener = onDenominationChosenListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final CharSequence[] items = { activity.getString(R.string.denomination_1),
                activity.getString(R.string.denomination_5),
                activity.getString(R.string.denomination_10),
                activity.getString(R.string.denomination_20),
                activity.getString(R.string.denomination_50_vip),
                activity.getString(R.string.denomination_100_vip) };

        AlertDialog.Builder denominationBuilder = new AlertDialog.Builder(activity);
        denominationBuilder.setTitle(R.string.choose_a_denomination);
        denominationBuilder.setSingleChoiceItems(items,
                Arrays.asList(items).indexOf(mCurrentDenomination),
                new DialogInterface.OnClickListener() {
            
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        // TODO: Add VIP check
                        mOnDenominationChosenListener.onDenominationChosen(items[item].toString());
                    }
                    
                });
        return denominationBuilder.create();
    }

    public interface OnDenominationChosenListener {
        public void onDenominationChosen(String denomination);
    }
}
