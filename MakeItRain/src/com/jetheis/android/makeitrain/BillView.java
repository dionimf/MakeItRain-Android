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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BillView extends SurfaceView implements SurfaceHolder.Callback {

    class BillThread extends Thread {

        private SurfaceHolder mSurfaceHolder;
        private Context mContext;
        private Resources mResources;
        private SharedPreferences mPrefs;

        private Bitmap mBillImage;

        private boolean mRunning = false;
        private boolean mFlinging = false;

        private int mBillY;
        private int mBillHeight;
        private int mFlingVelocity;

        private int mImageResource;
        private int mDenomination;
        private int mTotalSpent;

        public BillThread(SurfaceHolder surfaceHolder, Context context, Handler handler, int imageResource, int denomination) {
            mSurfaceHolder = surfaceHolder;
            mContext = context;

            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            mTotalSpent = mPrefs.getInt(context.getString(R.string.pref_total_spent), 0);

            mResources = context.getResources();
            mBillImage = BitmapFactory.decodeResource(mResources, getImageResource());
            mBillHeight = mBillImage.getHeight();
        }

        public boolean isRunning() {
            return mRunning;
        }

        public void setIsRunning(boolean isRunning) {
            mRunning = isRunning;
        }

        public boolean isFlinging() {
            return mFlinging;
        }

        public void setIsFlinging(boolean isFlinging) {
            mFlinging = isFlinging;
        }

        public int getDenomination() {
            return mDenomination;
        }

        public void setDenomination(int denomination) {
            mDenomination = denomination;
        }

        public int getImageResource() {
            return mImageResource;
        }

        public void setImageResource(int imageResource) {
            mImageResource = imageResource;
            mBillImage = BitmapFactory.decodeResource(mResources, mImageResource);
        }

        private void drawOn(Canvas canvas) {
            if (isFlinging() && mBillY > -mBillHeight) {
                mBillY -= mFlingVelocity;
            } else if (isFlinging()) {
                mBillY = 0;
                setIsFlinging(false);
                mTotalSpent += getDenomination();

                Editor editor = mPrefs.edit();
                editor.putInt(mContext.getString(R.string.pref_total_spent), mTotalSpent);
                editor.commit();
            }
            canvas.drawBitmap(mBillImage, 0, 0, null);
            canvas.drawBitmap(mBillImage, 0, mBillY, null);
        }

        public void updateBillPostion(int y) {
            mBillY = y;
        }

        public void initiateFling(int velocity) {
            mFlingVelocity = velocity;
            mFlinging = true;
        }

        public void run() {
            Canvas canvas = null;

            while (mRunning) {
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    drawOn(canvas);
                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private BillThread thread;
    private boolean mDragging = false;

    private int mDragYOffset;
    private int mLastMoveY = 0;
    private int mVelocity = 0;

    public BillView(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        thread = new BillThread(holder, context, null, getImageResource(), getDenomination());
        thread.setDenomination(1);
        thread.setImageResource(R.drawable.bill_1_left);

        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (thread.isFlinging()) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mDragging = true;
                mDragYOffset = (int) event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mDragging) {
                    int newY = (int) event.getY() - mDragYOffset;
                    mVelocity = Math.min(Math.max(mLastMoveY - newY, 80), 150);
                    mLastMoveY = newY;
                    thread.updateBillPostion(Math.min(0, newY));
                }
                return true;

            case MotionEvent.ACTION_UP:
                mDragging = false;
                thread.initiateFling(mVelocity);
                return true;
        }

        return false;
    }
    
    public int getDenomination() {
        return thread.getDenomination();
    }
    
    public void setDenomination(int denomination) {
        thread.setDenomination(denomination);
    }
    
    public int getImageResource() {
        return thread.getImageResource();
    }
    
    public void setImageResource(int imageResource) {
        thread.setImageResource(imageResource);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    public void surfaceCreated(SurfaceHolder holder) {
        thread.setIsRunning(true);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setIsRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // Just eat it and try again
            }
        }
    }
}