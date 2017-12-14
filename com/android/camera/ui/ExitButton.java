package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.support.v7.recyclerview.C0049R;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class ExitButton extends View {
    private boolean mExpand;
    private int mExpandLeft;
    private int mExpandRight;
    private String mText;
    private TextPaint mTextPaint = new TextPaint(1);

    public ExitButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTextPaint.setTextSize((float) context.getResources().getDimensionPixelSize(C0049R.dimen.camera_mode_exit_button_text_size));
        this.mTextPaint.setColor(-1);
    }

    public void draw(Canvas canvas) {
        if (this.mExpand) {
            setLeft(this.mExpandLeft);
            setRight(this.mExpandRight);
        } else {
            this.mTextPaint.setAlpha(255);
        }
        super.draw(canvas);
        if (this.mText != null && getWidth() > getPaddingLeft() * 2) {
            FontMetricsInt fontMetricsInt = this.mTextPaint.getFontMetricsInt();
            float height = (float) (((canvas.getHeight() - fontMetricsInt.bottom) - fontMetricsInt.top) / 2);
            int measureText = (int) this.mTextPaint.measureText(this.mText, 0, this.mText.length());
            int width = getWidth() - (getPaddingLeft() * 2);
            int width2 = ((getWidth() - (getPaddingLeft() * 2)) - measureText) / 2;
            canvas.save();
            if (width2 < 0) {
                canvas.clipRect(getPaddingLeft(), 0, getWidth() - getPaddingLeft(), canvas.getHeight());
                if (this.mExpand) {
                    this.mTextPaint.setAlpha((width * 255) / measureText);
                }
            }
            canvas.drawText(this.mText, (float) (getPaddingLeft() + width2), height, this.mTextPaint);
            canvas.restore();
        }
    }

    public TextPaint getPaint() {
        return this.mTextPaint;
    }

    public String getText() {
        return this.mText;
    }

    protected void onMeasure(int i, int i2) {
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        if (this.mText != null) {
            i3 = (int) this.mTextPaint.measureText(this.mText, 0, this.mText.length());
        }
        FontMetricsInt fontMetricsInt = this.mTextPaint.getFontMetricsInt();
        i3 += getPaddingLeft() + getPaddingRight();
        int paddingTop = (fontMetricsInt.descent - fontMetricsInt.ascent) + (getPaddingTop() + getPaddingBottom());
        Drawable background = getBackground();
        if (background != null) {
            i4 = background.getIntrinsicWidth();
            i5 = background.getIntrinsicHeight();
        }
        setMeasuredDimension(Math.max(i3, i4), Math.max(paddingTop, i5));
    }

    public void setExpandedAnimation(boolean z) {
        this.mExpand = z;
    }

    public void setExpandingSize(int i, int i2) {
        this.mExpandLeft = i;
        this.mExpandRight = i2;
    }

    public void setText(String str) {
        if (!TextUtils.equals(str, this.mText)) {
            this.mExpand = false;
            requestLayout();
            invalidate();
        }
        this.mText = str;
    }
}
