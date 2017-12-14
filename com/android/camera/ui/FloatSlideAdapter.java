package com.android.camera.ui;

public class FloatSlideAdapter implements RollAdapter {
    private int mCenterItem;
    private float mGapValue;
    private int mItemCount;
    private int mMaxValue;
    private int mMinValue;

    public FloatSlideAdapter() {
        this(0, 9, 1.0f);
    }

    public FloatSlideAdapter(int i, int i2, float f) {
        this.mGapValue = 1.0f;
        this.mMinValue = i;
        this.mMaxValue = i2;
        this.mGapValue = f;
        this.mItemCount = (int) ((((float) (this.mMaxValue - this.mMinValue)) / this.mGapValue) + 1.0f);
        this.mCenterItem = (this.mItemCount - 1) / 2;
    }

    private int round(float f) {
        return (int) ((f < 0.0f ? -0.5d : 0.5d) + ((double) f));
    }

    public int getCenterIndex() {
        return this.mCenterItem;
    }

    public int getItemIndexByValue(Object obj) {
        int i = 0;
        if (obj instanceof Integer) {
            i = ((Integer) obj).intValue();
        }
        if (i < this.mMinValue || i > this.mMaxValue) {
            i = (this.mMinValue + this.mMaxValue) / 2;
        }
        return (int) ((((float) (i - this.mMinValue)) / this.mGapValue) + 0.5f);
    }

    public int getItemValue(int i) {
        return (i < 0 || i >= getItemsCount()) ? -1 : round(((float) this.mMinValue) + (((float) i) * this.mGapValue));
    }

    public int getItemsCount() {
        return this.mItemCount;
    }

    public int getMaxItem() {
        return this.mItemCount - 1;
    }
}
