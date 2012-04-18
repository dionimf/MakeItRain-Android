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

public class OrientationDialogFragment extends DialogFragment {

    private CharSequence mCurrentOrientation;
    private OnOrientationChosenListener mOnOrientationChosenListener;

    public OrientationDialogFragment(CharSequence currentOrientation,
            OnOrientationChosenListener onOrientationChosenListener) {
        mCurrentOrientation = currentOrientation;
        mOnOrientationChosenListener = onOrientationChosenListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        final CharSequence[] orientations = { activity.getString(R.string.orientation_left),
                activity.getString(R.string.orientation_right) };

        int currentOrientation = Arrays.asList(orientations).indexOf(mCurrentOrientation);

        AlertDialog.Builder orientationBuilder = new AlertDialog.Builder(activity);
        orientationBuilder.setTitle(R.string.choose_an_orientation);
        orientationBuilder.setSingleChoiceItems(orientations, currentOrientation,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        mOnOrientationChosenListener.onOrientationChosen(orientations[item]
                                .toString());
                        dialog.dismiss();
                    }

                });

        return orientationBuilder.create();
    }

    public interface OnOrientationChosenListener {
        public void onOrientationChosen(String orientation);
    }

}
