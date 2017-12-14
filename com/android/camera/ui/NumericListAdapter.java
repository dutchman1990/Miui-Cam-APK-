package com.android.camera.ui;

public class NumericListAdapter {
    private int mGapValue;
    private int mMaxValue;
    private int mMinValue;

    public NumericListAdapter() {
        this(0, 9, 1);
    }

    public NumericListAdapter(int i, int i2, int i3) {
        this.mGapValue = 1;
        this.mMinValue = i;
        this.mMaxValue = i2;
        this.mGapValue = i3;
    }

    public String getItem(int i) {
        return (i < 0 || i >= getItemsCount()) ? null : Integer.toString(this.mMinValue + (this.mGapValue * i));
    }

    public int getItemIndexByValue(Object obj) {
        int i = 0;
        if (obj instanceof Integer) {
            i = ((Integer) obj).intValue();
        }
        return (i > this.mMaxValue || i < this.mMinValue) ? -1 : (int) ((((float) (i - this.mMinValue)) / ((float) this.mGapValue)) + 0.5f);
    }

    public int getItemValue(int i) {
        return (i < 0 || i >= getItemsCount()) ? -1 : this.mMinValue + (this.mGapValue * i);
    }

    public int getItemsCount() {
        return ((this.mMaxValue - this.mMinValue) / this.mGapValue) + 1;
    }
}
