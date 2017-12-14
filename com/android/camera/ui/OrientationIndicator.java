package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import com.android.camera.Util;

public class OrientationIndicator extends View implements V6FunctionUI {
    private static final int TRIANGLE_BASE_DIS = Util.dpToPixel(5.0f);
    private static final int TRIANGLE_BASE_HEIGHT = Util.dpToPixel(5.0f);
    private static final int TRIANGLE_BASE_LEN = Util.dpToPixel(8.0f);
    private Drawable mCaptureBitmap;
    private Paint mIndicatorPaint;
    private Path mIndicatorPath;
    private boolean mVisible;

    class C01411 implements Runnable {
        C01411() {
        }

        public void run() {
            OrientationIndicator.this.setVisibility(8);
        }
    }

    public OrientationIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void getIndicatorPath() {
        if (this.mIndicatorPath == null) {
            this.mIndicatorPath = new Path();
            int i = (-TRIANGLE_BASE_LEN) / 2;
            int i2 = ((-this.mCaptureBitmap.getIntrinsicHeight()) / 2) - (TRIANGLE_BASE_DIS / 2);
            this.mIndicatorPath.moveTo((float) i, (float) i2);
            this.mIndicatorPath.lineTo((float) (TRIANGLE_BASE_LEN + i), (float) i2);
            this.mIndicatorPath.lineTo((float) ((TRIANGLE_BASE_LEN / 2) + i), (float) (i2 - TRIANGLE_BASE_HEIGHT));
            this.mIndicatorPath.lineTo((float) i, (float) i2);
        }
    }

    public void enableControls(boolean z) {
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate((float) (getWidth() / 2), (float) (getHeight() / 2));
        this.mCaptureBitmap.draw(canvas);
        getIndicatorPath();
        canvas.drawPath(this.mIndicatorPath, this.mIndicatorPaint);
        canvas.translate((float) ((-getWidth()) / 2), (float) ((-getHeight()) / 2));
        canvas.restore();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCaptureBitmap = getResources().getDrawable(C0049R.drawable.bg_capture);
        this.mCaptureBitmap.setFilterBitmap(true);
        this.mCaptureBitmap.setBounds((-this.mCaptureBitmap.getIntrinsicWidth()) / 2, (-this.mCaptureBitmap.getIntrinsicHeight()) / 2, this.mCaptureBitmap.getIntrinsicWidth() / 2, this.mCaptureBitmap.getIntrinsicHeight() / 2);
        this.mIndicatorPaint = new Paint();
        this.mIndicatorPaint.setColor(-1);
        this.mIndicatorPaint.setStyle(Style.FILL);
        this.mIndicatorPaint.setAntiAlias(true);
    }

    public void onPause() {
        updateVisible(false);
    }

    public void onResume() {
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
    }

    public void updateVisible(boolean z) {
        float f = 0.0f;
        if (this.mVisible != z) {
            this.mVisible = z;
            Runnable runnable = null;
            if (!this.mVisible) {
                runnable = new C01411();
            } else if (getVisibility() != 0) {
                setVisibility(0);
                setAlpha(0.0f);
            }
            animate().cancel();
            ViewPropertyAnimator animate = animate();
            if (this.mVisible) {
                f = 1.0f;
            }
            animate.alpha(f).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(runnable).start();
        }
    }
}
