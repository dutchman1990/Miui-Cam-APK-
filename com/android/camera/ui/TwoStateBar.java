package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class TwoStateBar extends View {
    private int mFutureEnd;
    private int mFutureStart;
    private int mPastEnd;
    private int mPastStart;

    public TwoStateBar(Context context) {
        super(context);
    }

    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        if (this.mPastStart < this.mPastEnd) {
            paint.setColor(-16733953);
            canvas.drawRect((float) this.mPastStart, 0.0f, (float) this.mPastEnd, (float) getHeight(), paint);
        }
        if (this.mFutureStart < this.mFutureEnd) {
            paint.setColor(-1711276033);
            canvas.drawRect((float) this.mFutureStart, 0.0f, (float) this.mFutureEnd, (float) getHeight(), paint);
        }
    }

    public void setStatePosition(int i, int i2, int i3, int i4) {
        this.mPastStart = Math.max(0, i - this.mLeft);
        this.mPastEnd = Math.min(getWidth(), i2 - this.mLeft);
        this.mFutureStart = Math.max(0, i3 - this.mLeft);
        this.mFutureEnd = Math.min(getWidth(), i4 - this.mLeft);
        invalidate();
    }
}
