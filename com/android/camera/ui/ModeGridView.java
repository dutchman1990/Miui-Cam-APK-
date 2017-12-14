package com.android.camera.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.recyclerview.C0049R;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import com.android.camera.Util;

/* compiled from: ModeView */
class ModeGridView extends ViewGroup {
    private static int INVALID_POSITION = -1;
    private long dragResponseMS = 1000;
    private boolean isDrag = false;
    private int mChildHeight;
    private int mChildWidth;
    private int mColumnCount;
    private int mDownScrollBorder;
    private int mDownX;
    private int mDownY;
    private Bitmap mDragBitmap;
    private ImageView mDragImageView;
    private int mDragPosition;
    private boolean mEnableDrag = false;
    private int mGridViewHonSpacing;
    private int mGridViewMarginTop;
    private int mGridViewMarginWidth;
    private int mGridViewVerSpacing;
    private Handler mHandler = new Handler();
    private Runnable mLongClickRunnable = new C01381();
    private int mOffset2Left;
    private int mOffset2Top;
    ScreenView mParent;
    private int mPoint2ItemLeft;
    private int mPoint2ItemTop;
    private int mRowCount;
    private int mScreenIndex;
    private Runnable mScrollRunnable = new C01392();
    private View mStartDragItemView = null;
    private int mStatusHeight;
    private Rect mTouchFrame;
    private int mUpScrollBorder;
    private Vibrator mVibrator;
    private LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;
    private int moveX;
    private int moveY;
    private OnChanageListener onChanageListener;

    /* compiled from: ModeView */
    class C01381 implements Runnable {
        C01381() {
        }

        public void run() {
            ModeGridView.this.isDrag = true;
            ModeGridView.this.mVibrator.vibrate(50);
            ModeGridView.this.mStartDragItemView.setVisibility(4);
            ModeGridView.this.createDragImage(ModeGridView.this.mDragBitmap, ModeGridView.this.mDownX, ModeGridView.this.mDownY);
        }
    }

    /* compiled from: ModeView */
    class C01392 implements Runnable {
        C01392() {
        }

        public void run() {
            if (ModeGridView.this.moveY > ModeGridView.this.mUpScrollBorder) {
                ModeGridView.this.mHandler.postDelayed(ModeGridView.this.mScrollRunnable, 25);
            } else if (ModeGridView.this.moveY < ModeGridView.this.mDownScrollBorder) {
                ModeGridView.this.mHandler.postDelayed(ModeGridView.this.mScrollRunnable, 25);
            } else {
                ModeGridView.this.mHandler.removeCallbacks(ModeGridView.this.mScrollRunnable);
            }
            ModeGridView.this.onSwapItem(ModeGridView.this.moveX, ModeGridView.this.moveY);
            View childAt = ModeGridView.this.getChildAt(ModeGridView.this.mDragPosition - ModeGridView.this.getFirstVisiblePosition());
            ModeGridView.this.mParent.snapToScreen(ModeGridView.this.mScreenIndex + 1);
        }
    }

    /* compiled from: ModeView */
    public interface OnChanageListener {
        void onChange(int i, int i2);
    }

    public ModeGridView(Context context, ScreenView screenView, int i, int i2, int i3, int i4, int i5) {
        super(context);
        set(i, i2, i3, i4);
        setDrawingCacheEnabled(true);
        setWillNotDraw(false);
        initGridViewLayout();
        initDrag(screenView, i5);
    }

    private void createDragImage(Bitmap bitmap, int i, int i2) {
        this.mWindowLayoutParams = new LayoutParams();
        this.mWindowLayoutParams.format = -3;
        this.mWindowLayoutParams.gravity = 51;
        this.mWindowLayoutParams.x = (i - this.mPoint2ItemLeft) + this.mOffset2Left;
        this.mWindowLayoutParams.y = ((i2 - this.mPoint2ItemTop) + this.mOffset2Top) - this.mStatusHeight;
        this.mWindowLayoutParams.alpha = 0.55f;
        this.mWindowLayoutParams.width = -2;
        this.mWindowLayoutParams.height = -2;
        this.mWindowLayoutParams.flags = 24;
        this.mDragImageView = new ImageView(getContext());
        this.mDragImageView.setImageBitmap(bitmap);
        this.mWindowManager.addView(this.mDragImageView, this.mWindowLayoutParams);
    }

    private int getFirstVisiblePosition() {
        return 0;
    }

    private void initDrag(ScreenView screenView, int i) {
        this.mParent = screenView;
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mScreenIndex = i;
    }

    private void initGridViewLayout() {
        this.mGridViewHonSpacing = getResources().getDimensionPixelSize(C0049R.dimen.setting_grid_horizontal_space);
        this.mGridViewVerSpacing = getResources().getDimensionPixelSize(C0049R.dimen.setting_grid_vertical_space);
        this.mGridViewMarginWidth = ((Util.sWindowWidth - (this.mChildWidth * this.mColumnCount)) - (this.mGridViewHonSpacing * (this.mColumnCount - 1))) / 2;
        this.mGridViewMarginTop = getResources().getDimensionPixelSize(C0049R.dimen.mode_settings_margin_top);
        if (this.mRowCount < 3) {
            this.mGridViewMarginTop += ((this.mChildHeight + this.mGridViewVerSpacing) * (3 - this.mRowCount)) / 2;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isTouchInItem(android.view.View r5, int r6, int r7) {
        /*
        r4 = this;
        r3 = 0;
        r0 = r5.getLeft();
        r1 = r5.getTop();
        if (r6 < r0) goto L_0x0012;
    L_0x000b:
        r2 = r5.getWidth();
        r2 = r2 + r0;
        if (r6 <= r2) goto L_0x0013;
    L_0x0012:
        return r3;
    L_0x0013:
        if (r7 < r1) goto L_0x001c;
    L_0x0015:
        r2 = r5.getHeight();
        r2 = r2 + r1;
        if (r7 <= r2) goto L_0x001d;
    L_0x001c:
        return r3;
    L_0x001d:
        r2 = 1;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.ui.ModeGridView.isTouchInItem(android.view.View, int, int):boolean");
    }

    private void onDragItem(int i, int i2) {
        this.mWindowLayoutParams.x = (i - this.mPoint2ItemLeft) + this.mOffset2Left;
        this.mWindowLayoutParams.y = ((i2 - this.mPoint2ItemTop) + this.mOffset2Top) - this.mStatusHeight;
        this.mWindowManager.updateViewLayout(this.mDragImageView, this.mWindowLayoutParams);
        onSwapItem(i, i2);
        this.mHandler.post(this.mScrollRunnable);
    }

    private void onStopDrag() {
        getChildAt(this.mDragPosition - getFirstVisiblePosition()).setVisibility(0);
        removeDragImage();
    }

    private void onSwapItem(int i, int i2) {
        int pointToPosition = pointToPosition(i, i2);
        if (pointToPosition != this.mDragPosition && pointToPosition != INVALID_POSITION) {
            getChildAt(pointToPosition - getFirstVisiblePosition()).setVisibility(4);
            getChildAt(this.mDragPosition - getFirstVisiblePosition()).setVisibility(0);
            if (this.onChanageListener != null) {
                this.onChanageListener.onChange(this.mDragPosition, pointToPosition);
            }
            this.mDragPosition = pointToPosition;
        }
    }

    private void removeDragImage() {
        if (this.mDragImageView != null) {
            this.mWindowManager.removeView(this.mDragImageView);
            this.mDragImageView = null;
        }
    }

    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!this.mEnableDrag) {
            return super.dispatchTouchEvent(motionEvent);
        }
        switch (motionEvent.getAction()) {
            case 0:
                this.mHandler.postDelayed(this.mLongClickRunnable, this.dragResponseMS);
                this.mDownX = (int) motionEvent.getX();
                this.mDownY = (int) motionEvent.getY();
                this.mDragPosition = pointToPosition(this.mDownX, this.mDownY);
                if (this.mDragPosition != INVALID_POSITION) {
                    this.mStartDragItemView = getChildAt(this.mDragPosition - getFirstVisiblePosition());
                    this.mPoint2ItemTop = this.mDownY - this.mStartDragItemView.getTop();
                    this.mPoint2ItemLeft = this.mDownX - this.mStartDragItemView.getLeft();
                    this.mOffset2Top = (int) (motionEvent.getRawY() - ((float) this.mDownY));
                    this.mOffset2Left = (int) (motionEvent.getRawX() - ((float) this.mDownX));
                    this.mDownScrollBorder = getHeight() / 4;
                    this.mUpScrollBorder = (getHeight() * 3) / 4;
                    this.mStartDragItemView.setDrawingCacheEnabled(true);
                    this.mDragBitmap = Bitmap.createBitmap(this.mStartDragItemView.getDrawingCache());
                    this.mStartDragItemView.destroyDrawingCache();
                    break;
                }
                return super.dispatchTouchEvent(motionEvent);
            case 1:
                this.mHandler.removeCallbacks(this.mLongClickRunnable);
                this.mHandler.removeCallbacks(this.mScrollRunnable);
                break;
            case 2:
                if (!isTouchInItem(this.mStartDragItemView, (int) motionEvent.getX(), (int) motionEvent.getY())) {
                    this.mHandler.removeCallbacks(this.mLongClickRunnable);
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    protected void doLayout(int i, int i2, int i3, int i4) {
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            layoutChildByIndex(i5);
        }
    }

    protected void layoutChildByIndex(int i) {
        int i2 = 0;
        int i3 = i / this.mColumnCount;
        int i4 = i % this.mColumnCount;
        int i5 = ((this.mChildWidth * i4) + this.mGridViewMarginWidth) + (i4 > 0 ? this.mGridViewHonSpacing * i4 : 0);
        int i6 = this.mGridViewMarginTop + (this.mChildHeight * i3);
        if (i3 > 0) {
            i2 = this.mGridViewVerSpacing * i3;
        }
        int i7 = i6 + i2;
        getChildAt(i).layout(i5, i7, this.mChildWidth + i5, this.mChildHeight + i7);
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
        setMeasuredDimension(measureDimension(i, ((this.mGridViewMarginWidth + (this.mChildWidth * this.mColumnCount)) + (this.mGridViewHonSpacing * (this.mColumnCount - 1))) + this.mGridViewMarginWidth), measureDimension(i2, (this.mGridViewMarginTop + (this.mChildHeight * this.mRowCount)) + (this.mGridViewVerSpacing * (this.mRowCount - 1))));
        measureChildren(MeasureSpec.makeMeasureSpec(this.mChildWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mChildHeight, 1073741824));
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.isDrag || this.mDragImageView == null) {
            return super.onTouchEvent(motionEvent);
        }
        switch (motionEvent.getAction()) {
            case 1:
                onStopDrag();
                this.isDrag = false;
                break;
            case 2:
                this.moveX = (int) motionEvent.getX();
                this.moveY = (int) motionEvent.getY();
                onDragItem(this.moveX, this.moveY);
                break;
        }
        return true;
    }

    public int pointToPosition(int i, int i2) {
        Rect rect = this.mTouchFrame;
        if (rect == null) {
            this.mTouchFrame = new Rect();
            rect = this.mTouchFrame;
        }
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (childAt.getVisibility() == 0) {
                childAt.getHitRect(rect);
                if (rect.contains(i, i2)) {
                    return childCount;
                }
            }
        }
        return -1;
    }

    public void set(int i, int i2, int i3, int i4) {
        this.mRowCount = Math.max(1, i);
        this.mColumnCount = Math.max(1, i2);
        this.mChildHeight = Math.max(1, i4);
        this.mChildWidth = Math.max(1, i3);
    }
}
