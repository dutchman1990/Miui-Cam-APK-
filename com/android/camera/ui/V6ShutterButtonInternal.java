package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.camera.CameraSettings;
import com.android.camera.Log;
import com.android.camera.Util;

public class V6ShutterButtonInternal extends V6BottomAnimationImageView {
    private static int LONG_PRESSED_TRIGGER_TIME = 500;
    private static final int OUTER_CIRCLE_WIDTH = Util.dpToPixel(1.0f);
    private static final String TAG = V6ShutterButtonInternal.class.getSimpleName();
    private int FLING_DISTANCE_THRESHOLD = (Util.dpToPixel(400.0f) * Util.dpToPixel(400.0f));
    private int FLING_VELOCITY_THRESHOLD = (Util.dpToPixel(21.0f) * Util.dpToPixel(21.0f));
    private boolean mActionDown;
    private int mAnimationType = 0;
    private float mBigRadius;
    private boolean mCameraOpened;
    private float mCenterMaxRadius;
    private float mCenterMinRadius;
    private Paint mCenterPaint;
    private Path mCenterPath;
    private float mCenterRadius;
    private float mCenterThresholdRadius;
    private int mCenterX;
    private int mCenterY;
    private long mDuration;
    private Handler mHandler = new C01731();
    private boolean mInShutterButton;
    private boolean mIncreaseFlag;
    private boolean mIsVideo = false;
    private boolean mLongClickable = true;
    private long mOutTime = -1;
    private Paint mOuterPaint;
    private Rect mShutterRect;
    private long mStartTime;
    private int mTargetImage;

    class C01731 extends Handler {
        C01731() {
        }

        public void dispatchMessage(Message message) {
            switch (message.what) {
                case 0:
                    V6ShutterButtonInternal.this.onLongPress();
                    break;
                case 4:
                    V6ShutterButtonInternal.this.invalidate();
                    long uptimeMillis = SystemClock.uptimeMillis() - V6ShutterButtonInternal.this.mStartTime;
                    if (uptimeMillis <= V6ShutterButtonInternal.this.mDuration) {
                        float -get2 = ((float) uptimeMillis) / ((float) V6ShutterButtonInternal.this.mDuration);
                        V6ShutterButtonInternal v6ShutterButtonInternal = V6ShutterButtonInternal.this;
                        float -get1 = V6ShutterButtonInternal.this.mCenterMinRadius;
                        float -get0 = V6ShutterButtonInternal.this.mCenterMaxRadius - V6ShutterButtonInternal.this.mCenterMinRadius;
                        if (!V6ShutterButtonInternal.this.mIncreaseFlag) {
                            -get2 = 1.0f - -get2;
                        }
                        v6ShutterButtonInternal.mCenterRadius = -get1 + (-get0 * -get2);
                        V6ShutterButtonInternal.this.mHandler.sendEmptyMessageDelayed(4, 20);
                        break;
                    }
                    V6ShutterButtonInternal.this.animationDone();
                    return;
            }
        }
    }

    public V6ShutterButtonInternal(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        enablePressFilter(false);
    }

    private void animationDone() {
        switch (this.mAnimationType) {
            case 1:
                setPressed(false);
                break;
            case 2:
                setPressed(true);
                break;
            case 3:
                setImageResource(this.mTargetImage);
                break;
        }
        this.mAnimationType = 0;
    }

    private void checkGesture(int i, int i2) {
        if (this.mOutTime != -1) {
            if (this.mShutterRect == null) {
                this.mShutterRect = new Rect();
                getGlobalVisibleRect(this.mShutterRect);
            }
            int centerX = i - this.mShutterRect.centerX();
            int centerY = i2 - this.mShutterRect.centerY();
            int i3 = (centerX * centerX) + (centerY * centerY);
            int currentTimeMillis = (int) (System.currentTimeMillis() - this.mOutTime);
            Log.m5v(TAG, "gesture d2(d*d)=" + i3 + " duration=" + currentTimeMillis);
            if (currentTimeMillis <= 0 || this.FLING_VELOCITY_THRESHOLD > i3 / currentTimeMillis) {
                if (this.FLING_DISTANCE_THRESHOLD >= i3) {
                    return;
                }
            }
            onFling(new Point(i, i2), new Point(this.mShutterRect.centerX(), this.mShutterRect.centerY()));
        }
    }

    private void doAnimate(int i, long j) {
        boolean z = true;
        if (this.mCenterPaint != null) {
            if (this.mAnimationType != 0) {
                animationDone();
            }
            this.mAnimationType = i;
            switch (this.mAnimationType) {
                case 1:
                case 2:
                    this.mCenterMaxRadius = this.mBigRadius * 0.9053f;
                    this.mCenterMinRadius = this.mBigRadius * 0.81477f;
                    if (this.mAnimationType != 1) {
                        z = false;
                    }
                    this.mIncreaseFlag = z;
                    break;
                case 3:
                    this.mCenterMaxRadius = this.mBigRadius * 0.9053f;
                    this.mCenterMinRadius = this.mBigRadius * 0.4713f;
                    this.mCenterThresholdRadius = this.mCenterMinRadius + ((this.mCenterMaxRadius - this.mCenterMinRadius) * 0.7f);
                    break;
            }
            this.mStartTime = SystemClock.uptimeMillis();
            this.mDuration = j;
            this.mCenterRadius = this.mIncreaseFlag ? this.mCenterMinRadius : this.mCenterMaxRadius;
            this.mHandler.removeMessages(4);
            this.mHandler.sendEmptyMessage(4);
        }
    }

    private void onClick() {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_shutter_button, 2, null, null);
        }
    }

    private void onFling(Point point, Point point2) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(2, C0049R.id.v6_shutter_button, 2, point, point2);
        }
    }

    private void onFocused(boolean z) {
        Log.m5v(TAG, "onFocused  mMessageDispacher+" + this.mMessageDispacher);
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(3, C0049R.id.v6_shutter_button, 2, Boolean.valueOf(z), null);
        }
    }

    private void onLongPress() {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_shutter_button, 2, null, null);
        }
    }

    private void prepareAnimation() {
        int i = -1;
        if (this.mCenterPaint == null) {
            this.mCenterPath = new Path();
            this.mCenterPaint = new Paint();
            this.mCenterPaint.setAntiAlias(true);
            this.mCenterPaint.setStyle(Style.FILL);
            this.mOuterPaint = new Paint();
            this.mOuterPaint.setAntiAlias(true);
            this.mOuterPaint.setStyle(Style.STROKE);
            this.mOuterPaint.setStrokeWidth((float) OUTER_CIRCLE_WIDTH);
        }
        this.mCenterPaint.setColor(this.mIsVideo ? -1032447 : -1);
        Paint paint = this.mOuterPaint;
        if (this.mIsVideo) {
            i = -1862270977;
        }
        paint.setColor(i);
        this.mBigRadius = (float) (getDrawable().getIntrinsicWidth() / 2);
    }

    public void changeImageWithAnimation(int i, long j) {
        if (i == C0049R.drawable.video_shutter_button_stop_bg || i == C0049R.drawable.video_shutter_button_start_bg || i == C0049R.drawable.pano_shutter_button_stop_bg || i == C0049R.drawable.camera_shutter_button_bg) {
            this.mTargetImage = i;
            if (i == C0049R.drawable.video_shutter_button_start_bg || i == C0049R.drawable.camera_shutter_button_bg) {
                this.mIncreaseFlag = true;
            } else {
                this.mIncreaseFlag = false;
            }
            doAnimate(3, j);
            return;
        }
        setImageResource(i);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mCameraOpened) {
            int actionMasked = motionEvent.getActionMasked();
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            switch (actionMasked) {
                case 0:
                    if (isEnabled()) {
                        if (this.mIsVideo || CameraSettings.isAudioCaptureOpen() || V6ModulePicker.isPanoramaModule()) {
                            setPressed(true);
                        } else {
                            doAnimate(2, 200);
                        }
                        this.mActionDown = true;
                        onFocused(true);
                        if (isEnabled() && CameraSettings.isPressDownCapture()) {
                            onClick();
                        }
                        if (this.mLongClickable) {
                            this.mHandler.removeMessages(0);
                            this.mHandler.sendEmptyMessageDelayed(0, (long) LONG_PRESSED_TRIGGER_TIME);
                            break;
                        }
                    }
                    break;
                case 1:
                case 3:
                case 6:
                    if (this.mActionDown && motionEvent.getActionIndex() == 0) {
                        boolean hasMessages = this.mLongClickable ? this.mHandler.hasMessages(0) : true;
                        this.mHandler.removeMessages(0);
                        this.mInShutterButton = Util.pointInView(rawX, rawY, this);
                        if (hasMessages && this.mInShutterButton && isEnabled() && !CameraSettings.isPressDownCapture()) {
                            onClick();
                        } else if (hasMessages && !this.mInShutterButton) {
                            checkGesture((int) rawX, (int) rawY);
                        }
                        if (this.mIsVideo || CameraSettings.isAudioCaptureOpen() || V6ModulePicker.isPanoramaModule()) {
                            setPressed(false);
                        } else {
                            doAnimate(1, 200);
                        }
                        onFocused(false);
                        this.mOutTime = -1;
                        this.mActionDown = false;
                        break;
                    }
                    break;
                case 2:
                    if (!Util.pointInView(rawX, rawY, this)) {
                        if (this.mOutTime == -1) {
                            this.mOutTime = System.currentTimeMillis();
                            break;
                        }
                    }
                    this.mOutTime = -1;
                    break;
                    break;
            }
            return true;
        }
        Log.m0d(TAG, "dispatchTouchEvent: drop event " + motionEvent);
        return false;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public boolean isCanceled() {
        return !this.mInShutterButton;
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        this.mCameraOpened = true;
    }

    public void onCreate() {
        super.onCreate();
        this.mIsVideo = V6ModulePicker.isVideoModule();
        setImageResource(this.mIsVideo ? C0049R.drawable.video_shutter_button_start_bg : C0049R.drawable.camera_shutter_button_bg);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mAnimationType == 0) {
            super.onDraw(canvas);
            return;
        }
        if (this.mCenterX == 0) {
            this.mCenterX = (this.mRight - this.mLeft) / 2;
            this.mCenterY = (this.mBottom - this.mTop) / 2;
        }
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mBigRadius - 2.0f, this.mOuterPaint);
        if (this.mAnimationType != 3 || this.mCenterRadius > this.mCenterThresholdRadius) {
            canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mCenterRadius, this.mCenterPaint);
            return;
        }
        float f = this.mCenterRadius * 0.71f;
        float f2 = ((float) this.mCenterX) - f;
        float f3 = ((float) this.mCenterX) + f;
        float f4 = ((float) this.mCenterY) - f;
        float f5 = ((float) this.mCenterY) + f;
        float f6 = f * ((((this.mCenterRadius - this.mCenterMinRadius) * 0.8f) / (this.mCenterThresholdRadius - this.mCenterMinRadius)) + 1.0f);
        this.mCenterPath.reset();
        this.mCenterPath.moveTo(f2, f4);
        this.mCenterPath.quadTo((float) this.mCenterX, ((float) this.mCenterY) - f6, f3, f4);
        this.mCenterPath.quadTo(((float) this.mCenterX) + f6, (float) this.mCenterY, f3, f5);
        this.mCenterPath.quadTo((float) this.mCenterX, ((float) this.mCenterY) + f6, f2, f5);
        this.mCenterPath.quadTo(((float) this.mCenterX) - f6, (float) this.mCenterY, f2, f4);
        this.mCenterPath.close();
        canvas.drawPath(this.mCenterPath, this.mCenterPaint);
    }

    public void onPause() {
        super.onPause();
        this.mCameraOpened = false;
        this.mHandler.removeMessages(0);
    }

    public void onResume() {
        super.onResume();
        prepareAnimation();
    }

    public void setLongClickable(boolean z) {
        this.mLongClickable = z;
    }
}
