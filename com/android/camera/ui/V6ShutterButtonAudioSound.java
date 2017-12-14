package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.camera.Device;
import com.android.camera.Util;

public class V6ShutterButtonAudioSound extends ImageView implements V6FunctionUI {
    private static final int LINE_WIDTH = Util.dpToPixel((float) (Device.isPad() ? 2 : 1));
    private int mAlpha = 255;
    private int mCurrentRadius;
    private int mDelta;
    private Handler mHandler = new C01721();
    private Interpolator mInterpolator = new BounceInterpolator();
    private int mMaxRadius;
    private int mMinRadius;
    private Paint mPaint;
    private int mProgress;
    private int mStartRadius;
    private long mStartTime;

    class C01721 extends Handler {
        C01721() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    long uptimeMillis = SystemClock.uptimeMillis() - V6ShutterButtonAudioSound.this.mStartTime;
                    if (uptimeMillis <= 500) {
                        float f = ((float) uptimeMillis) / 500.0f;
                        V6ShutterButtonAudioSound.this.mCurrentRadius = V6ShutterButtonAudioSound.this.mStartRadius + ((int) (((float) (V6ShutterButtonAudioSound.this.mMaxRadius - V6ShutterButtonAudioSound.this.mStartRadius)) * V6ShutterButtonAudioSound.this.getInterpolation(f)));
                        V6ShutterButtonAudioSound.this.mAlpha = ((int) (-255.0f * f)) + 255;
                        V6ShutterButtonAudioSound.this.mHandler.sendEmptyMessageDelayed(0, 20);
                        V6ShutterButtonAudioSound.this.invalidate();
                        break;
                    }
                    return;
                case 1:
                    V6ShutterButtonAudioSound.this.invalidate();
                    break;
            }
        }
    }

    public V6ShutterButtonAudioSound(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private float getInterpolation(float f) {
        return this.mInterpolator.getInterpolation(f);
    }

    public void enableControls(boolean z) {
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
        setVisibility(8);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mProgress > 0) {
            if (this.mPaint == null) {
                this.mPaint = new Paint();
                this.mPaint.setAntiAlias(true);
                this.mPaint.setStrokeWidth((float) LINE_WIDTH);
                this.mPaint.setStyle(Style.STROKE);
            }
            this.mPaint.setColor(Color.argb(this.mAlpha, 255, 255, 255));
            canvas.drawCircle((float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2), (float) this.mCurrentRadius, this.mPaint);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void onPause() {
        this.mProgress = -1;
        setVisibility(8);
    }

    public void onResume() {
        this.mProgress = -1;
        if (V6ModulePicker.isCameraModule()) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }

    public void setAudioProgress(float f) {
        this.mProgress = (int) (100.0f * f);
        if (this.mProgress < 0) {
            this.mProgress = -1;
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(0);
            this.mHandler.sendEmptyMessageDelayed(1, 10);
            return;
        }
        if (!this.mHandler.hasMessages(0)) {
            this.mStartRadius = this.mMinRadius + ((int) ((((float) this.mDelta) * f) * 0.8f));
            this.mCurrentRadius = this.mStartRadius;
            this.mAlpha = 255;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mHandler.sendEmptyMessage(0);
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
    }

    public void setRadius(int i, int i2) {
        this.mMinRadius = i;
        this.mMaxRadius = (int) (((float) i2) * 0.85f);
        this.mDelta = this.mMaxRadius - this.mMinRadius;
    }
}
