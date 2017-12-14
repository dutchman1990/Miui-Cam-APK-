package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import com.android.camera.Util;

public class HorizontalSlideView extends View {
    private HorizontalDrawAdapter mDrawAdapter;
    private GestureDetector mGestureDetector;
    private OnGestureListener mGestureListener = new C01371();
    private boolean mJustifyEnabled = true;
    private int mMaxX = 0;
    private int mMinX = 0;
    private boolean mNeedJustify;
    private OnItemSelectListener mOnItemSelectListener;
    private OnPositionSelectListener mOnPositionSelectListener;
    private float mOriginX;
    private int mPositionX = 0;
    private Scroller mScroller;
    private int mSelectedItemIndex;
    private boolean mSelectionFromSelf = false;

    class C01371 extends SimpleOnGestureListener {
        C01371() {
        }

        public boolean onDown(MotionEvent motionEvent) {
            HorizontalSlideView.this.mScroller.forceFinished(true);
            HorizontalSlideView.this.mNeedJustify = false;
            return true;
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            HorizontalSlideView.this.flingX(-((int) f));
            return true;
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (HorizontalSlideView.this.mPositionX == HorizontalSlideView.this.mMinX && f < 0.0f) {
                return false;
            }
            if (HorizontalSlideView.this.mPositionX == HorizontalSlideView.this.mMaxX && f > 0.0f) {
                return false;
            }
            HorizontalSlideView.this.setPositionX((int) (((float) HorizontalSlideView.this.mPositionX) + f));
            return true;
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            HorizontalSlideView.this.scroll((int) (motionEvent.getX() - HorizontalSlideView.this.mOriginX));
            return true;
        }
    }

    public static abstract class HorizontalDrawAdapter {
        public abstract void draw(int i, Canvas canvas, boolean z);

        public abstract Align getAlign(int i);

        public abstract int getCount();

        public abstract float measureGap(int i);

        public abstract float measureWidth(int i);
    }

    public interface OnItemSelectListener {
        void onItemSelect(HorizontalSlideView horizontalSlideView, int i);
    }

    public interface OnPositionSelectListener {
        void onPositionSelect(HorizontalSlideView horizontalSlideView, float f);
    }

    public HorizontalSlideView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalSlideView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public HorizontalSlideView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private float calculateLength(int i, int i2) {
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = this.mOriginX;
        float f4 = 0.0f;
        float f5 = f3;
        if (this.mDrawAdapter != null) {
            boolean isLayoutRTL = Util.isLayoutRTL(getContext());
            int count = isLayoutRTL ? this.mDrawAdapter.getCount() - 1 : 0;
            int count2 = isLayoutRTL ? 0 : this.mDrawAdapter.getCount() - 1;
            int i3 = isLayoutRTL ? -1 : 1;
            for (int i4 = 0; i4 < this.mDrawAdapter.getCount(); i4++) {
                int i5 = count + (i4 * i3);
                Object obj = i5 == count ? 1 : null;
                Object obj2 = i5 == count2 ? 1 : null;
                float itemWidth = getItemWidth(i5);
                float f6 = itemWidth / 2.0f;
                if (obj != null) {
                    f4 = this.mOriginX - f6;
                }
                f5 = obj != null ? f3 : f4 + f6;
                f4 += obj2 != null ? 0.0f : getItemGap(i4) + itemWidth;
                if (i5 == i) {
                    f = f5;
                } else if (i5 == i2) {
                    f2 = f5;
                }
            }
        }
        return Math.abs(f2 - f);
    }

    private void flingX(int i) {
        this.mScroller.fling(this.mPositionX, 0, i, 0, this.mMinX, this.mMaxX, 0, 0);
        invalidate();
    }

    private float getItemGap(int i) {
        return this.mDrawAdapter.measureGap(i);
    }

    private float getItemWidth(int i) {
        return this.mDrawAdapter.measureWidth(i);
    }

    private void scroll(int i) {
        if (i != 0) {
            if (this.mPositionX + i < this.mMinX) {
                i = this.mMinX - this.mPositionX;
            } else if (this.mPositionX + i > this.mMaxX) {
                i = this.mMaxX - this.mPositionX;
            }
            this.mScroller.startScroll(this.mPositionX, 0, i, 0);
            invalidate();
        }
    }

    private void select(int i) {
        this.mSelectionFromSelf = true;
        if (this.mSelectedItemIndex != i) {
            this.mSelectedItemIndex = i;
            if (this.mOnItemSelectListener != null) {
                this.mOnItemSelectListener.onItemSelect(this, this.mSelectedItemIndex);
            }
        }
        if (this.mOnPositionSelectListener != null) {
            float f = ((float) this.mPositionX) / ((float) (this.mMaxX - this.mMinX));
            OnPositionSelectListener onPositionSelectListener = this.mOnPositionSelectListener;
            if (Util.isLayoutRTL(getContext())) {
                f = 1.0f - f;
            }
            onPositionSelectListener.onPositionSelect(this, f);
        }
    }

    private void setPositionX(int i) {
        this.mPositionX = i;
        if (this.mPositionX < this.mMinX) {
            this.mPositionX = this.mMinX;
        } else if (this.mPositionX > this.mMaxX) {
            this.mPositionX = this.mMaxX;
        }
        invalidate();
    }

    protected void init(Context context) {
        this.mGestureDetector = new GestureDetector(context, this.mGestureListener);
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mScroller = new Scroller(context);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mScroller.computeScrollOffset()) {
            this.mPositionX = this.mScroller.getCurrX();
            invalidate();
        }
        float f = this.mOriginX - ((float) this.mPositionX);
        float f2 = 0.0f;
        float f3 = f;
        float height = ((float) getHeight()) / 2.0f;
        Object obj = 1;
        float f4 = 0.0f;
        float f5 = 0.0f;
        if (this.mDrawAdapter != null) {
            int i;
            int i2;
            Object obj2;
            Object obj3;
            float itemWidth;
            float f6;
            boolean isLayoutRTL = Util.isLayoutRTL(getContext());
            int count = isLayoutRTL ? this.mDrawAdapter.getCount() - 1 : 0;
            int count2 = isLayoutRTL ? 0 : this.mDrawAdapter.getCount() - 1;
            int i3 = isLayoutRTL ? -1 : 1;
            for (i = 0; i < this.mDrawAdapter.getCount(); i++) {
                i2 = count + (i * i3);
                obj2 = i2 == count ? 1 : null;
                obj3 = i2 == count2 ? 1 : null;
                itemWidth = getItemWidth(i2);
                f6 = itemWidth / 2.0f;
                float f7 = obj2 != null ? 0.0f : f5;
                f5 = obj3 != null ? 0.0f : getItemGap(i2) / 2.0f;
                if (obj2 != null) {
                    f2 = f - f6;
                }
                f3 = obj2 != null ? f : f2 + f6;
                if (obj != null) {
                    float f8 = f3 - this.mOriginX;
                    if (f8 > 0.0f || (-f8) > f6 + f5) {
                        if (f8 > 0.0f && f8 <= f6 + f7) {
                        }
                    }
                    select(i2);
                    obj = null;
                    f4 = f8;
                }
                f2 += obj3 != null ? 0.0f : getItemGap(i2) + itemWidth;
            }
            this.mMaxX = (int) (f3 - f);
            for (i = 0; i < this.mDrawAdapter.getCount(); i++) {
                i2 = count + (i * i3);
                obj2 = i2 == count ? 1 : null;
                obj3 = i2 == count2 ? 1 : null;
                itemWidth = getItemWidth(i2);
                f6 = itemWidth / 2.0f;
                if (obj2 != null) {
                    f2 = f - f6;
                }
                f3 = obj2 != null ? f : f2 + f6;
                if (f2 + itemWidth >= 0.0f && f2 <= ((float) getWidth())) {
                    canvas.save();
                    if (this.mDrawAdapter.getAlign(i2) == Align.LEFT) {
                        canvas.translate(f2, height);
                    } else {
                        if (this.mDrawAdapter.getAlign(i2) == Align.CENTER) {
                            canvas.translate(f3, height);
                        } else {
                            canvas.translate(f2 + itemWidth, height);
                        }
                    }
                    this.mDrawAdapter.draw(i2, canvas, this.mSelectedItemIndex == i2);
                    canvas.restore();
                }
                f2 += obj3 != null ? 0.0f : getItemGap(i2) + itemWidth;
            }
        }
        if (this.mJustifyEnabled && this.mNeedJustify && this.mScroller.isFinished()) {
            this.mNeedJustify = false;
            scroll((int) f4);
        }
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mOriginX = ((float) i) / 2.0f;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = this.mGestureDetector.onTouchEvent(motionEvent);
        if (motionEvent.getAction() == 1) {
            this.mNeedJustify = true;
            invalidate();
        }
        return onTouchEvent;
    }

    public void setDrawAdapter(HorizontalDrawAdapter horizontalDrawAdapter) {
        this.mDrawAdapter = horizontalDrawAdapter;
        this.mNeedJustify = false;
        this.mSelectedItemIndex = 0;
        this.mScroller.forceFinished(true);
        if (this.mDrawAdapter != null) {
            this.mMaxX = this.mMinX + ((int) calculateLength(0, this.mDrawAdapter.getCount() - 1));
        }
        if (Util.isLayoutRTL(getContext())) {
            this.mPositionX = this.mMaxX;
        } else {
            this.mPositionX = this.mMinX;
        }
        invalidate();
    }

    public void setJustifyEnabled(boolean z) {
        this.mJustifyEnabled = z;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.mOnItemSelectListener = onItemSelectListener;
    }

    public void setOnPositionSelectListener(OnPositionSelectListener onPositionSelectListener) {
        this.mOnPositionSelectListener = onPositionSelectListener;
    }

    public void setSelection(float f) {
        if (Util.isLayoutRTL(getContext()) && this.mDrawAdapter != null) {
            f = 1.0f - f;
        }
        this.mNeedJustify = false;
        this.mScroller.forceFinished(true);
        this.mPositionX = (int) (((float) (this.mMaxX - this.mMinX)) * f);
        invalidate();
    }

    public void setSelection(int i) {
        if (this.mSelectedItemIndex != i) {
            this.mNeedJustify = false;
            this.mScroller.forceFinished(true);
            if (this.mDrawAdapter != null) {
                if (i >= this.mDrawAdapter.getCount()) {
                    i = this.mDrawAdapter.getCount() - 1;
                }
                if (Util.isLayoutRTL(getContext())) {
                    this.mPositionX = this.mMaxX - ((int) calculateLength(0, i));
                } else {
                    this.mPositionX = this.mMinX + ((int) calculateLength(0, i));
                }
            }
            select(i);
            invalidate();
        }
    }
}
