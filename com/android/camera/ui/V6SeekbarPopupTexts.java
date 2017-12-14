package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.camera.preferences.IconListPreference;

public class V6SeekbarPopupTexts extends RelativeLayout implements Rotatable {
    private float mGap;
    private int mHeight;
    private int mPadding;
    private int mWidth;

    public V6SeekbarPopupTexts(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPadding = context.getResources().getDimensionPixelSize(C0049R.dimen.v6_seek_bar_padding) + context.getResources().getDimensionPixelSize(C0049R.dimen.half_of_cursor);
    }

    private void addTextView(CharSequence charSequence) {
        View textView = new TextView(this.mContext);
        textView.setLayoutParams(new LayoutParams(-2, -2));
        textView.setTextSize(12.0f);
        textView.setText(charSequence);
        addView(textView);
    }

    public void initialize(IconListPreference iconListPreference) {
        if (iconListPreference.getEntries() != null) {
            for (CharSequence addTextView : iconListPreference.getEntries()) {
                addTextView(addTextView);
            }
            setValue(iconListPreference.findIndexOfValue(iconListPreference.getValue()));
            requestLayout();
        }
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mGap = ((float) (this.mWidth - (this.mPadding * 2))) / ((float) (getChildCount() - 1));
        int childCount = getChildCount();
        int i5 = 0;
        while (i5 < childCount) {
            TextView textView = (TextView) getChildAt(i5);
            int measureText = ((int) textView.getPaint().measureText(textView.getText().toString())) + 1;
            int i6 = (int) (((double) (((float) (1 == getLayoutDirection() ? (childCount - 1) - i5 : i5)) * this.mGap)) + 0.5d);
            textView.layout((this.mPadding + i6) - (measureText / 2), 0, (this.mPadding + i6) + (measureText / 2), this.mHeight);
            i5++;
        }
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mWidth = i;
        this.mHeight = i2;
    }

    public void setOrientation(int i, boolean z) {
    }

    public void setValue(int i) {
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            TextView textView = (TextView) getChildAt(i2);
            if (i == i2) {
                textView.setTextColor(-1);
            } else {
                textView.setTextColor(-1275068417);
            }
        }
    }
}
