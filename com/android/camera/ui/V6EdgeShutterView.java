package com.android.camera.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import com.android.camera.Camera;
import com.android.camera.CameraAppImpl;
import com.android.camera.Util;
import com.android.camera.aosp_porting.animation.CubicEaseOutInterpolator;

public class V6EdgeShutterView extends View implements V6FunctionUI {
    private static final int CENTER_RADIUS = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.v6_edge_shutter_center_radius);
    private static final int NEAR_THRESHOLD = Util.dpToPixel(66.67f);
    private static int NORMAL_TAP_MAXY = ((Util.sWindowHeight * SystemProperties.getInt("camera_edge_max", 75)) / 100);
    private static int NORMAL_TOUCH_MAXY = (NORMAL_TAP_MAXY + (NEAR_THRESHOLD / 2));
    private static final int OUTER_CIRCLE_WIDTH = Util.dpToPixel(1.0f);
    private static final int OUT_RADIUS = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.v6_edge_shutter_out_radius);
    private static final int VIEW_WIDTH = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.v6_edge_shutter_width);
    private AnimatorListener mAnimatorListener;
    private Paint mCenterPaint;
    private ValueAnimator mClickAnim;
    private ValueAnimator mFlyOutAnim;
    private Interpolator mFlyinInterpolator = new OvershootInterpolator();
    private Handler mHandler = new C01631();
    private MessageDispacher mMessageDispacher;
    private ValueAnimator mMoveAnim;
    private Interpolator mMoveInterpolator = new CubicEaseOutInterpolator();
    private Paint mOuterPaint;
    private Interpolator mPressInterpolator = new ReverseInterpolator();
    private Rect mVisableBount = new Rect(0, 0, Util.sWindowWidth, Util.sWindowHeight);
    private int mVisibleState;

    class C01631 extends Handler {
        C01631() {
        }

        public void dispatchMessage(Message message) {
            switch (message.what) {
                case 0:
                case 2:
                    V6EdgeShutterView.this.hideShutterView();
                    return;
                case 1:
                    V6EdgeShutterView.this.checkPosture();
                    return;
                default:
                    return;
            }
        }
    }

    private class CustomAnimatorListener extends AnimatorListenerAdapter {
        private CustomAnimatorListener() {
        }

        public void onAnimationCancel(Animator animator) {
            Log.v("CameraEdgeShutterView", "onAnimationCancel animation=" + animator);
            V6EdgeShutterView.this.mVisibleState = 1;
        }

        public void onAnimationEnd(Animator animator) {
            Log.v("CameraEdgeShutterView", "onAnimationEnd animation=" + animator);
            if (animator == V6EdgeShutterView.this.mFlyOutAnim && V6EdgeShutterView.this.mVisibleState == 2) {
                V6EdgeShutterView.this.setRelateVisible(4);
                V6EdgeShutterView.this.mVisibleState = 4;
            } else if (animator == V6EdgeShutterView.this.mMoveAnim && V6EdgeShutterView.this.mVisibleState == 3) {
                V6EdgeShutterView.this.mVisibleState = 1;
            }
            V6EdgeShutterView.this.setX((float) V6EdgeShutterView.this.mLeft);
        }

        public void onAnimationStart(Animator animator) {
            if (animator == V6EdgeShutterView.this.mClickAnim) {
                V6EdgeShutterView.this.setX((float) V6EdgeShutterView.this.mLeft);
            }
        }
    }

    private class ReverseInterpolator implements Interpolator {
        private final Interpolator mInterpolator;

        private ReverseInterpolator(V6EdgeShutterView v6EdgeShutterView) {
            this(new AccelerateDecelerateInterpolator());
        }

        private ReverseInterpolator(Interpolator interpolator) {
            if (interpolator == null) {
                interpolator = new AccelerateDecelerateInterpolator();
            }
            this.mInterpolator = interpolator;
        }

        public float getInterpolation(float f) {
            return ((double) f) <= 0.5d ? this.mInterpolator.getInterpolation(f * 2.0f) : this.mInterpolator.getInterpolation(Math.abs(f - 1.0f) * 2.0f);
        }
    }

    public V6EdgeShutterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void checkPosture() {
        int capturePosture = ((Camera) this.mContext).getCapturePosture();
        if ((capturePosture != 1 || this.mLeft == 0) && !(capturePosture == 2 && this.mLeft == 0)) {
            if (capturePosture != 0 || this.mTop <= NORMAL_TAP_MAXY) {
                return;
            }
        }
        hideShutterView();
    }

    private ValueAnimator createFlyOutAnimation() {
        int i;
        int i2;
        if (this.mLeft < VIEW_WIDTH) {
            i = 0;
            i2 = -VIEW_WIDTH;
        } else {
            i = 0;
            i2 = VIEW_WIDTH;
        }
        ValueAnimator ofFloat = ObjectAnimator.ofFloat(this, "translationX", new float[]{(float) i, (float) i2});
        ofFloat.setInterpolator(this.mMoveInterpolator);
        ofFloat.setDuration(250);
        ofFloat.addListener(this.mAnimatorListener);
        return ofFloat;
    }

    private void hideShutterView() {
        if (this.mVisibleState == 1) {
            this.mFlyOutAnim = createFlyOutAnimation();
            this.mFlyOutAnim.start();
            this.mVisibleState = 2;
        }
    }

    private void setRelateVisible(int i) {
        int i2 = 8;
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        if (relativeLayout != null) {
            if (8 != i) {
                i2 = 0;
            }
            relativeLayout.setVisibility(i2);
        }
        setVisibility(i);
    }

    public void cancelAnimation() {
        animate().cancel();
        setX((float) this.mLeft);
        setY((float) this.mTop);
    }

    public void enableControls(boolean z) {
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
        if (V6ModulePicker.isVideoModule()) {
            this.mCenterPaint.setColor(-1032447);
            this.mOuterPaint.setColor(-1862270977);
            return;
        }
        this.mCenterPaint.setColor(-1);
        this.mOuterPaint.setColor(-1);
    }

    public void onDeviceMoving() {
        this.mHandler.sendEmptyMessage(2);
    }

    public void onDevicePostureChanged() {
        this.mHandler.sendEmptyMessage(1);
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawCircle((float) (VIEW_WIDTH / 2), (float) (VIEW_WIDTH / 2), (float) (OUT_RADIUS - 2), this.mOuterPaint);
        canvas.drawCircle((float) (VIEW_WIDTH / 2), (float) (VIEW_WIDTH / 2), (float) CENTER_RADIUS, this.mCenterPaint);
    }

    protected void onFinishInflate() {
        this.mCenterPaint = new Paint();
        this.mCenterPaint.setAntiAlias(true);
        this.mCenterPaint.setColor(-1);
        this.mCenterPaint.setStyle(Style.FILL);
        this.mOuterPaint = new Paint();
        this.mOuterPaint.setAntiAlias(true);
        this.mOuterPaint.setColor(-1);
        this.mOuterPaint.setStyle(Style.STROKE);
        this.mOuterPaint.setStrokeWidth((float) OUTER_CIRCLE_WIDTH);
        this.mAnimatorListener = new CustomAnimatorListener();
    }

    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(VIEW_WIDTH, VIEW_WIDTH);
    }

    public void onPause() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        setRelateVisible(8);
    }

    public void onResume() {
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }
}
