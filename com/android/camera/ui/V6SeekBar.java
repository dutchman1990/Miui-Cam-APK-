package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.Util;
import com.android.camera.preferences.IconListPreference;

public class V6SeekBar extends RelativeLayout {
    private static final int PADDING = Util.dpToPixel(0.0f);
    private TwoStateBar mBar;
    private int mBarHeight;
    private ImageView mCursor;
    private int mCursorHeight;
    private int mCursorPosition;
    private int mCursorWidth;
    private int mEndPosition;
    private float mGap;
    private int mHeight;
    private OnValueChangedListener mListener;
    private int mMaxValue = 9;
    private boolean mReLoad = false;
    private boolean mSmoothChange = true;
    private int mStartPosition;
    private int mValue = 0;
    private boolean mValueChanged = false;
    private int mWidth;

    public interface OnValueChangedListener {
        void onValueChanged(int i, boolean z);
    }

    public V6SeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBar = new TwoStateBar(context);
        this.mBar.setContentDescription(context.getResources().getString(C0049R.string.accessibility_seek_bar_line));
        addView(this.mBar, new LayoutParams(-1, -1));
        this.mCursor = new ImageView(context);
        this.mCursor.setImageResource(C0049R.drawable.v6_ic_face_beauty_cursor);
        addView(this.mCursor);
        this.mBarHeight = Util.dpToPixel(1.0f);
        this.mCursorHeight = this.mCursor.getDrawable().getIntrinsicHeight();
        this.mCursorWidth = this.mCursor.getDrawable().getIntrinsicWidth();
        this.mCursorPosition = 0;
    }

    private int clip(int i, int i2, int i3) {
        return i > i2 ? i2 : i < i3 ? i3 : i;
    }

    private int mapPositionToValue(int i) {
        return 1 == getLayoutDirection() ? clip((int) ((((float) (this.mEndPosition - i)) / this.mGap) + 0.5f), this.mMaxValue, 0) : clip((int) ((((float) (i - this.mStartPosition)) / this.mGap) + 0.5f), this.mMaxValue, 0);
    }

    private int mapValueToPosition(int i) {
        return 1 == getLayoutDirection() ? clip(this.mEndPosition - ((int) ((((float) i) * this.mGap) + 0.5f)), this.mEndPosition, this.mStartPosition) : clip(((int) ((((float) i) * this.mGap) + 0.5f)) + this.mStartPosition, this.mEndPosition, this.mStartPosition);
    }

    private void notifyChange(boolean z) {
        if (this.mListener != null) {
            this.mListener.onValueChanged(this.mValue, z);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (!isEnabled() && action != 4 && action != 3 && action != 1) {
            return false;
        }
        switch (action) {
            case 0:
                setActivated(true);
                notifyChange(false);
                break;
            case 1:
            case 3:
            case 4:
                setActivated(false);
                this.mCursorPosition = clip((int) ((((float) ((int) ((((motionEvent.getX() - ((float) this.mStartPosition)) - (((float) this.mCursorWidth) / 2.0f)) / this.mGap) + 0.5f))) * this.mGap) + ((float) this.mStartPosition)), this.mEndPosition, this.mStartPosition);
                requestLayout();
                this.mValue = mapPositionToValue(this.mCursorPosition);
                notifyChange(true);
                break;
            case 2:
                break;
        }
        int x = (int) ((((float) ((int) ((((motionEvent.getX() - ((float) this.mStartPosition)) - (((float) this.mCursorWidth) / 2.0f)) / this.mGap) + 0.5f))) * this.mGap) + ((float) this.mStartPosition));
        int x2 = (int) ((motionEvent.getX() - (((float) this.mCursorWidth) / 2.0f)) + 0.5f);
        int i = x2;
        i = ((this.mMaxValue > 3 || ((float) Math.abs(x - x2)) >= this.mGap / 4.0f) && (3 >= this.mMaxValue || this.mMaxValue > 30)) ? x2 : x;
        this.mCursorPosition = clip(i, this.mEndPosition, this.mStartPosition);
        requestLayout();
        if (this.mSmoothChange) {
            this.mValue = mapPositionToValue(this.mCursorPosition);
            notifyChange(false);
        }
        return true;
    }

    public void initialize(IconListPreference iconListPreference) {
        if (iconListPreference.getEntries() != null) {
            setMaxValue(iconListPreference.getEntries().length - 1);
            setValue(iconListPreference.findIndexOfValue(iconListPreference.getValue()));
        }
        requestLayout();
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mReLoad) {
            this.mGap = ((float) (this.mEndPosition - this.mStartPosition)) / ((float) this.mMaxValue);
            this.mCursorPosition = mapValueToPosition(this.mValue);
        }
        if (this.mValueChanged || this.mReLoad) {
            this.mCursorPosition = mapValueToPosition(this.mValue);
        }
        this.mReLoad = false;
        this.mValueChanged = false;
        int i5 = (this.mHeight - this.mBarHeight) / 2;
        this.mBar.layout(1, i5, this.mWidth - 1, this.mBarHeight + i5);
        int i6 = (this.mHeight - this.mCursorHeight) / 2;
        this.mCursor.layout(this.mCursorPosition, i6, this.mCursorPosition + this.mCursorWidth, this.mCursorHeight + i6);
        this.mBar.setStatePosition(1, this.mCursorPosition - this.mBarHeight, (this.mCursorPosition + this.mCursorWidth) + this.mBarHeight, this.mWidth - 1);
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mWidth = i;
        this.mHeight = i2;
        this.mStartPosition = PADDING;
        this.mEndPosition = (this.mWidth - PADDING) - this.mCursorWidth;
        this.mGap = ((float) (this.mEndPosition - this.mStartPosition)) / ((float) this.mMaxValue);
        this.mCursorPosition = mapValueToPosition(this.mValue);
        requestLayout();
    }

    public void setMaxValue(int i) {
        if (i > 0) {
            this.mMaxValue = i;
            this.mReLoad = true;
            requestLayout();
        }
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.mListener = onValueChangedListener;
    }

    public void setValue(int i) {
        if (this.mValue != i) {
            this.mValue = clip(i, this.mMaxValue, 0);
            requestLayout();
            this.mValueChanged = true;
        }
    }
}
