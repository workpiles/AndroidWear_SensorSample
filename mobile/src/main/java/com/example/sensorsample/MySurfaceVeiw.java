package com.example.sensorsample;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.TimeUnit;

/**
 * Created by koike on 2014/09/15.
 */
public class MySurfaceVeiw extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final String TAG = MySurfaceVeiw.class.getName();
    private final int PUNCH_FRAME = 10;
    private final int UPPER_FRAME = 10;
    private final int HOOK_FRAME = 10;

    private SurfaceHolder mHolder;
    private RectF mDamageRect;
    private int mState;
    private int mAct;
    private int mStateTimer;
    private Thread mThread;
    private Paint mPaint;
    private int mDamage;

    public MySurfaceVeiw(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
    }

    public MySurfaceVeiw(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mDamageRect = new RectF();
        mDamageRect.set(width/2 + 100, 10, width - 10, height/8);

        mPaint = new Paint();
        mPaint.setColor(Color.MAGENTA);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(100);

        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread = null;
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        long frequency = (long)(Math.floor((double) TimeUnit.SECONDS.toNanos(1L)/60.0f));

        while(mThread != null) {
            long current = System.nanoTime();
            long elapsedTime = current - last;
            if (elapsedTime > frequency) {
                last = current;
                changeState();
                drawView();

            } else {
                long interval = frequency - elapsedTime;
                try {
                    TimeUnit.NANOSECONDS.sleep(interval);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }


    }

    private void changeState() {
        Log.d(TAG, "changeState : " + mState);
        if (mAct != 0) {
            mDamage += 1;
        }

        switch (mState) {
            case 1 ://punch
                if (mAct == 1) {
                    mStateTimer = PUNCH_FRAME;
                } else if (mAct == 2) {
                    mState = 2;
                    mStateTimer = UPPER_FRAME;
                } else if (mAct == 3) {
                    mState = 3;
                    mStateTimer = HOOK_FRAME;
                } else {
                    mStateTimer--;
                    if (mStateTimer <= 0) {
                        mState = 0;
                    }
                }
                break;
            case 2 ://upper
                if (mAct == 1) {
                    mState = 1;
                    mStateTimer = PUNCH_FRAME;
                } else if (mAct == 2) {
                    mStateTimer = UPPER_FRAME;
                } else if (mAct == 3) {
                    mState = 3;
                    mStateTimer = HOOK_FRAME;
                } else {
                    mStateTimer--;
                    if (mStateTimer <= 0) {
                        mState = 0;
                    }
                }
                break;
            case 3 ://hook
                if (mAct == 1) {
                    mState = 1;
                    mStateTimer = PUNCH_FRAME;
                } else if (mAct == 2) {
                    mState = 2;
                    mStateTimer = UPPER_FRAME;
                } else if (mAct == 3) {
                    mStateTimer = HOOK_FRAME;
                } else {
                    mStateTimer--;
                    if (mStateTimer <= 0) {
                        mState = 0;
                    }
                }
            default :
                if (mAct == 1) {
                    mState = 1;
                    mStateTimer = PUNCH_FRAME;
                } else if (mAct == 2) {
                    mState = 2;
                    mStateTimer = UPPER_FRAME;
                } else if (mAct == 3) {
                    mState = 3;
                    mStateTimer = HOOK_FRAME;
                }
                break;
        }
        mAct = 0;
    }

    private void drawView() {
        try {
            Canvas canvas = mHolder.lockCanvas();

            switch (mState) {
                case 1 :
                    if (mStateTimer > PUNCH_FRAME - 3 ) {
                        canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p1),0,0,null);
                    } else {
                        canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p0),0,0,null);
                    }
                    break;
                case 2 :
                    if (mStateTimer > UPPER_FRAME - 3) {
                        canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p2),0,0,null);
                    } else {
                        canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p0),0,0,null);
                    }
                    break;
                case 3 :
                    if (mStateTimer > HOOK_FRAME - 3) {
                        canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p3),0,0,null);
                    } else {
                        canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p0),0,0,null);
                    }
                    break;
                default :
                    canvas.drawBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.p0),0,0,null);
            }

            canvas.drawText("ダメージ："+ mDamage, mDamageRect.left, mDamageRect.bottom ,mPaint);
            mHolder.unlockCanvasAndPost(canvas);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void punch() {
        mAct = 1;
    }

    public void upper() {
        mAct = 2;
    }

    public void hook() { mAct = 3; }
}
