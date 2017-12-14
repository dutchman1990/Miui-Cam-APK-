package com.android.camera.ui;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/* compiled from: SettingScreenView */
class StaticGridView extends ViewGroup {
    private int mChildHeight;
    private int mChildWidth;
    private int mColumnCount;
    private int mRowCount;

    public StaticGridView(Context context, int i, int i2, int i3, int i4) {
        super(context);
        set(i, i2, i3, i4);
        setDrawingCacheEnabled(true);
        setWillNotDraw(false);
    }

    public void addView(View view, int i, LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
    }

    protected void doLayout(int i, int i2, int i3, int i4) {
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            layoutChildByIndex(i5);
        }
    }

    protected void layoutChildByIndex(int i) {
        int i2 = i / this.mColumnCount;
        int i3 = i % this.mColumnCount;
        if (1 == getLayoutDirection()) {
            i3 = (this.mColumnCount - 1) - i3;
        }
        getChildAt(i).layout((this.mChildWidth * i3) + 1, (this.mChildHeight * i2) + 1, (this.mChildWidth * (i3 + 1)) - 1, (this.mChildHeight * (i2 + 1)) - 1);
    }

    int measureDimension(int i, int i2) {
        switch (MeasureSpec.getMode(i)) {
            case Integer.MIN_VALUE:
                return Math.min(i2, MeasureSpec.getSize(i));
            case 0:
                return i2;
            case 1073741824:
                return MeasureSpec.getSize(i);
            default:
                return 0;
        }
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        doLayout(i, i2, i3, i4);
    }

    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(measureDimension(i, this.mChildWidth * this.mColumnCount), measureDimension(i2, this.mChildHeight * this.mRowCount));
        measureChildren(MeasureSpec.makeMeasureSpec(this.mChildWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mChildHeight, 1073741824));
    }

    public void set(int i, int i2, int i3, int i4) {
        this.mRowCount = Math.max(1, i);
        this.mColumnCount = Math.max(1, i2);
        this.mChildHeight = Math.max(1, i4);
        this.mChildWidth = Math.max(1, i3);
    }
}
