package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;

public class RotateTextView extends TwoStateTextView implements Rotatable {
    private long mAnimationEndTime = 0;
    private long mAnimationStartTime = 0;
    private boolean mClockwise = false;
    private int mCurrentDegree = 0;
    private int mStartDegree = 0;
    private int mTargetDegree = 0;

    public RotateTextView(Context context) {
        super(context);
    }

    public RotateTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void onDraw(Canvas canvas) {
        if (!TextUtils.isEmpty(getText())) {
            if (this.mCurrentDegree != this.mTargetDegree) {
                long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
                if (currentAnimationTimeMillis < this.mAnimationEndTime) {
                    int i = (int) (currentAnimationTimeMillis - this.mAnimationStartTime);
                    int i2 = this.mStartDegree;
                    if (!this.mClockwise) {
                        i = -i;
                    }
                    int i3 = i2 + ((i * 270) / 1000);
                    this.mCurrentDegree = i3 >= 0 ? i3 % 360 : (i3 % 360) + 360;
                    invalidate();
                } else {
                    this.mCurrentDegree = this.mTargetDegree;
                }
            }
            int saveCount = canvas.getSaveCount();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            canvas.translate((float) ((((getWidth() - paddingLeft) - getPaddingRight()) / 2) + paddingLeft), (float) ((((getHeight() - paddingTop) - getPaddingBottom()) / 2) + paddingTop));
            canvas.rotate((float) (-this.mCurrentDegree));
            canvas.translate((float) ((-getWidth()) / 2), (float) ((-getHeight()) / 2));
            super.onDraw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth != measuredHeight) {
            int i3 = measuredWidth > measuredHeight ? measuredWidth : measuredHeight;
            setMeasuredDimension(i3, i3);
        }
    }

    public void setOrientation(int i, boolean z) {
        boolean z2 = false;
        i = i >= 0 ? i % 360 : (i % 360) + 360;
        if (i != this.mTargetDegree) {
            this.mTargetDegree = i;
            if (z) {
                this.mStartDegree = this.mCurrentDegree;
                this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
                int i2 = this.mTargetDegree - this.mCurrentDegree;
                if (i2 < 0) {
                    i2 += 360;
                }
                if (i2 > 180) {
                    i2 -= 360;
                }
                if (i2 >= 0) {
                    z2 = true;
                }
                this.mClockwise = z2;
                this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((Math.abs(i2) * 1000) / 270));
            } else {
                this.mCurrentDegree = this.mTargetDegree;
            }
            invalidate();
        }
    }
}
