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
    
    public OrientationDialogFragment(CharSequence currentOrientation, OnOrientationChosenListener onOrientationChosenListener) {
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
                        mOnOrientationChosenListener.onOrientationChosen(orientations[item].toString());
                        dialog.dismiss();
                    }
                    
                });
        
        return orientationBuilder.create();
    }
    
    public interface OnOrientationChosenListener {
        public void onOrientationChosen(String orientation);
    }

}
