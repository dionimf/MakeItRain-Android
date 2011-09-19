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
        private SharedPreferences mPrefs;

        private Bitmap mBillImage;

        private boolean mRunning = false;
        private boolean mFlinging = false;

        private int mBillY;
        private int mBillHeight;
        private int mFlingVelocity;

        private int mTotalSpent;

        public BillThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            mSurfaceHolder = surfaceHolder;
            mContext = context;

            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            mTotalSpent = mPrefs.getInt("totalSpent", 0);

            Resources res = context.getResources();
            mBillImage = BitmapFactory.decodeResource(res,
                    R.drawable.bill_100_right);
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

        private void drawOn(Canvas canvas) {
            if (isFlinging() && mBillY > -mBillHeight) {
                mBillY -= mFlingVelocity;
            } else if (isFlinging()) {
                mBillY = 0;
                setIsFlinging(false);
                mTotalSpent += 1;

                Editor editor = mPrefs.edit();
                editor.putInt("totalSpent", mTotalSpent);
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

        thread = new BillThread(holder, context, null);

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
                    mVelocity = Math.min(Math.max(mLastMoveY - newY, 30), 100);
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

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
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