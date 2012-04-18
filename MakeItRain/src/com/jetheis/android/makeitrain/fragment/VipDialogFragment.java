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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jetheis.android.makeitrain.R;

public class VipDialogFragment extends DialogFragment {
    
    
    public VipDialogFragment(boolean rejection) {
        
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        
        AlertDialog.Builder vipBuilder;

        LayoutInflater vipInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vipLayout = vipInflater.inflate(R.layout.vip_dialog_fragment, null);

        TextView vipText = (TextView) vipLayout
                .findViewById(R.id.vip_dialog_fragment_text_view);
        vipText.setText("You need to be a VIP to do that!\n\n - No ads\n -$50 and $100 bills\n - Makes you cooler\n - Only costs a dollar!");
        vipBuilder = new AlertDialog.Builder(activity);
        vipBuilder.setView(vipLayout);
        vipBuilder.setTitle("You're Not Cool Enough Yet!");
        vipBuilder.setPositiveButton("Yeah, I'm cool!", null);
        vipBuilder.setNegativeButton("No, I'm cheap.", null);
        
        return vipBuilder.create();
    }
}
