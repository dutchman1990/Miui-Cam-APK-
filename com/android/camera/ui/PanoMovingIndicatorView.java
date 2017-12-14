package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import com.android.camera.CameraAppImpl;
import com.android.camera.Log;
import com.android.camera.Util;
import java.util.HashMap;

public class PanoMovingIndicatorView extends View {
    private static final int MAX_GAP = Util.dpToPixel(6.0f);
    private static final int SPEED_DEVIATION = (2904 / MAX_GAP);
    private static final int STONE_WIDTH = Util.dpToPixel(10.67f);
    public static final String TAG = PanoMovingIndicatorView.class.getSimpleName();
    private static int[] sBlockWidth = new int[]{Util.dpToPixel(0.67f), Util.dpToPixel(2.0f), Util.dpToPixel(3.34f)};
    private static int[] sGapWidth = new int[]{Util.dpToPixel(2.67f), Util.dpToPixel(2.0f), Util.dpToPixel(1.34f)};
    private static HashMap<Boolean, Integer> sTimesMap = new HashMap(2);
    private int mArrowMargin = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelOffset(C0049R.dimen.pano_arrow_margin);
    private Point mCurrentFramePos = new Point();
    private int mDirection;
    private boolean mFast;
    private int mFilterMoveSpeed;
    private int mHalfStoneHeight;
    private Handler mHandler = new C01421();
    private int mLastestSpeed;
    private Drawable mMovingDirectionIc = getResources().getDrawable(C0049R.drawable.ic_pano_direction_right);
    private int mOffsetX;
    private float mPointGap = -1.0f;
    private int mPreivewCenterY;
    private StateChangeTrigger<Boolean> mStateChangeTrigger = new StateChangeTrigger(Boolean.valueOf(false), sTimesMap);
    private Paint mTailPaint = new Paint();

    class C01421 extends Handler {
        C01421() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    if (((float) PanoMovingIndicatorView.this.getPointGap(PanoMovingIndicatorView.this.mLastestSpeed)) != PanoMovingIndicatorView.this.mPointGap) {
                        PanoMovingIndicatorView.this.filterSpeed(PanoMovingIndicatorView.this.mLastestSpeed);
                        PanoMovingIndicatorView.this.applyNewGap();
                        sendEmptyMessageDelayed(1, 10);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    class StateChangeTrigger<T> {
        private T mCurrentState;
        private T mLastestState;
        private int mLastestTimes = 0;
        private HashMap<T, Integer> mMaxTimesMap;

        public StateChangeTrigger(T t, HashMap<T, Integer> hashMap) {
            this.mLastestState = t;
            this.mCurrentState = t;
            this.mMaxTimesMap = hashMap;
        }

        public void setCurrentState(T t) {
            this.mCurrentState = t;
        }
    }

    static {
        sTimesMap.put(Boolean.valueOf(true), Integer.valueOf(1));
        sTimesMap.put(Boolean.valueOf(false), Integer.valueOf(4));
    }

    public PanoMovingIndicatorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTailPaint.setColor(-1);
        this.mHalfStoneHeight = ((int) (((float) this.mMovingDirectionIc.getIntrinsicHeight()) * 0.5625f)) / 2;
    }

    private void applyNewGap() {
        this.mPointGap = (float) getPointGap(this.mFilterMoveSpeed);
        invalidate();
    }

    private void filterSpeed(int i) {
        this.mFilterMoveSpeed = (int) ((((float) this.mFilterMoveSpeed) * 0.9f) + (((float) i) * 0.1f));
    }

    private int getPointGap(int i) {
        return i > 4096 ? (MAX_GAP * ((i - 4096) + SPEED_DEVIATION)) / 2904 : -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isFar() {
        /*
        r4 = this;
        r3 = 0;
        r0 = r4.mCurrentFramePos;
        r0 = r0.y;
        r1 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r0 == r1) goto L_0x000d;
    L_0x0009:
        r0 = r4.mPreivewCenterY;
        if (r0 != 0) goto L_0x000e;
    L_0x000d:
        return r3;
    L_0x000e:
        r0 = r4.mCurrentFramePos;
        r0 = r0.y;
        r1 = r4.mPreivewCenterY;
        r0 = r0 - r1;
        r0 = java.lang.Math.abs(r0);
        r0 = (float) r0;
        r1 = r4.mPreivewCenterY;
        r1 = (float) r1;
        r2 = 1048576000; // 0x3e800000 float:0.25 double:5.180653787E-315;
        r1 = r1 * r2;
        r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r0 < 0) goto L_0x0050;
    L_0x0024:
        r0 = TAG;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "too far current relative y is ";
        r1 = r1.append(r2);
        r2 = r4.mCurrentFramePos;
        r2 = r2.y;
        r1 = r1.append(r2);
        r2 = " refy is ";
        r1 = r1.append(r2);
        r2 = r4.mPreivewCenterY;
        r1 = r1.append(r2);
        r1 = r1.toString();
        android.util.Log.e(r0, r1);
        r0 = 1;
        return r0;
    L_0x0050:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.ui.PanoMovingIndicatorView.isFar():boolean");
    }

    public boolean isTooFast() {
        return this.mPointGap > 0.0f;
    }

    public void onDraw(Canvas canvas) {
        Log.m5v(TAG, "onDraw mPointGap=" + this.mPointGap);
        if (this.mCurrentFramePos.x != Integer.MIN_VALUE && this.mCurrentFramePos.y != Integer.MIN_VALUE) {
            int i = this.mCurrentFramePos.x;
            int i2 = this.mArrowMargin;
            Drawable drawable = this.mMovingDirectionIc;
            if (this.mDirection == 0) {
                i += (this.mOffsetX + i2) + drawable.getIntrinsicWidth();
            } else if (1 == this.mDirection) {
                i -= (this.mOffsetX + i2) + drawable.getIntrinsicWidth();
            }
            int height = ((getHeight() / 2) + this.mCurrentFramePos.y) - this.mPreivewCenterY;
            canvas.save();
            canvas.translate((float) i, (float) height);
            if (1 == this.mDirection) {
                canvas.rotate(180.0f);
            }
            int i3 = -drawable.getIntrinsicWidth();
            drawable.setBounds(i3, (-drawable.getIntrinsicHeight()) / 2, 0, drawable.getIntrinsicHeight() / 2);
            drawable.draw(canvas);
            i3 = (int) (((float) i3) - (((float) STONE_WIDTH) + this.mPointGap));
            int i4 = (int) this.mPointGap;
            for (int i5 = 0; i5 < sGapWidth.length && i4 > 0; i5++) {
                canvas.drawRect((float) i3, (float) (-this.mHalfStoneHeight), (float) (sBlockWidth[i5] + i3), (float) this.mHalfStoneHeight, this.mTailPaint);
                i3 += sBlockWidth[i5];
                if (i4 >= sGapWidth[i5]) {
                    i3 += 8;
                    i4 -= 8;
                } else {
                    i3 += i4;
                    i4 = 0;
                }
            }
            canvas.drawRect((float) i3, (float) (-this.mHalfStoneHeight), (float) (-drawable.getIntrinsicWidth()), (float) this.mHalfStoneHeight, this.mTailPaint);
            if (1 == this.mDirection) {
                canvas.rotate(-180.0f);
            }
            canvas.translate((float) (-i), (float) (-height));
            canvas.restore();
        }
    }

    public void setMovingAttibute(int i, int i2, int i3) {
        this.mDirection = i & 1;
        this.mOffsetX = i2;
        this.mFast = false;
        this.mFilterMoveSpeed = 4096;
        this.mStateChangeTrigger.setCurrentState(Boolean.valueOf(this.mFast));
        this.mCurrentFramePos.set(Integer.MIN_VALUE, Integer.MIN_VALUE);
        this.mPointGap = -1.0f;
    }

    public void setPosition(Point point, int i) {
        this.mCurrentFramePos.set(point.x, point.y);
        this.mPreivewCenterY = i;
        invalidate();
    }

    public void setToofast(boolean z, int i) {
        android.util.Log.i(TAG, "setToofast moveSpeed=" + i + " fastFlag:" + z);
        if (i > 7000) {
            i = 7000;
        }
        this.mLastestSpeed = i;
        if (((float) getPointGap(this.mLastestSpeed)) != this.mPointGap && !this.mHandler.hasMessages(1)) {
            this.mHandler.sendEmptyMessage(1);
        }
    }
}
