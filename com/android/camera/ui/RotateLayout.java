package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class RotateLayout extends ViewGroup implements Rotatable {
    protected View mChild;
    private int mOrientation;

    public RotateLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(17170445);
    }

    protected void onFinishInflate() {
        this.mChild = getChildAt(0);
        this.mChild.setPivotX(0.0f);
        this.mChild.setPivotY(0.0f);
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int i6 = i4 - i2;
        switch (this.mOrientation) {
            case 0:
            case 180:
                this.mChild.layout(0, 0, i5, i6);
                return;
            case 90:
            case 270:
                this.mChild.layout(0, 0, i6, i5);
                return;
            default:
                return;
        }
    }

    protected void onMeasure(int i, int i2) {
        int i3 = 0;
        int i4 = 0;
        switch (this.mOrientation) {
            case 0:
            case 180:
                measureChild(this.mChild, i, i2);
                i3 = this.mChild.getMeasuredWidth();
                i4 = this.mChild.getMeasuredHeight();
                break;
            case 90:
            case 270:
                measureChild(this.mChild, i2, i);
                i3 = this.mChild.getMeasuredHeight();
                i4 = this.mChild.getMeasuredWidth();
                break;
        }
        setMeasuredDimension(i3, i4);
        switch (this.mOrientation) {
            case 0:
                this.mChild.setTranslationX(0.0f);
                this.mChild.setTranslationY(0.0f);
                break;
            case 90:
                this.mChild.setTranslationX(0.0f);
                this.mChild.setTranslationY((float) i4);
                break;
            case 180:
                this.mChild.setTranslationX((float) i3);
                this.mChild.setTranslationY((float) i4);
                break;
            case 270:
                this.mChild.setTranslationX((float) i3);
                this.mChild.setTranslationY(0.0f);
                break;
        }
        this.mChild.setRotation((float) (-this.mOrientation));
    }

    public void setOrientation(int i, boolean z) {
        i %= 360;
        if (this.mOrientation != i) {
            this.mOrientation = i;
            requestLayout();
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
