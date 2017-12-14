package com.android.camera.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.Scroller;
import com.android.camera.ActivityBase;
import com.android.camera.Util;
import java.util.LinkedList;
import java.util.Queue;

public class HorizontalListView extends AdapterView<ListAdapter> {
    private static final String TAG = HorizontalListView.class.getSimpleName();
    protected ListAdapter mAdapter;
    private boolean mBlockNotification;
    protected int mCurrentX;
    private boolean mDataChanged = false;
    private DataSetObserver mDataObserver = new C01331();
    private int mDisplayOffset = 0;
    private GestureDetector mGesture;
    private boolean mIsScrollingPerformed;
    private int mItemWidth = 160;
    private View mLastSelectImageListItem;
    private int mLeftViewIndex = -1;
    private int mMaxX = Integer.MAX_VALUE;
    protected int mNextX;
    private OnGestureListener mOnGesture = new C01342();
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private OnItemSelectedListener mOnItemSelected;
    private int mPaddingWidth;
    private int mPresetWidth = 0;
    private int mPreviousSelectViewIndex = 0;
    private Queue<View> mRemovedViewQueue = new LinkedList();
    private int mRightViewIndex = 0;
    protected Scroller mScroller;
    private boolean mSelectCenter = true;
    private int mSelectViewIndex = 0;
    private boolean mTouchDown;

    class C01331 extends DataSetObserver {
        C01331() {
        }

        public void onChanged() {
            synchronized (HorizontalListView.this) {
                HorizontalListView.this.mDataChanged = true;
            }
            HorizontalListView.this.invalidate();
            HorizontalListView.this.requestLayout();
        }

        public void onInvalidated() {
            HorizontalListView.this.reset();
            HorizontalListView.this.invalidate();
            HorizontalListView.this.requestLayout();
        }
    }

    class C01342 extends SimpleOnGestureListener {
        C01342() {
        }

        private boolean isEventWithinView(MotionEvent motionEvent, View view) {
            Rect rect = new Rect();
            int[] iArr = new int[2];
            view.getLocationOnScreen(iArr);
            int i = iArr[0];
            int width = i + view.getWidth();
            int i2 = iArr[1];
            rect.set(i, i2, width, i2 + view.getHeight());
            return rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
        }

        public boolean onDown(MotionEvent motionEvent) {
            return HorizontalListView.this.onDown(motionEvent);
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            return HorizontalListView.this.onFling(motionEvent, motionEvent2, f, f2);
        }

        public void onLongPress(MotionEvent motionEvent) {
            int childCount = HorizontalListView.this.getChildCount();
            int i = 0;
            while (i < childCount) {
                View childAt = HorizontalListView.this.getChildAt(i);
                if (!isEventWithinView(motionEvent, childAt)) {
                    i++;
                } else if (HorizontalListView.this.mOnItemLongClicked != null) {
                    int -wrap0 = HorizontalListView.this.toDataIndex((HorizontalListView.this.mLeftViewIndex + 1) + i);
                    HorizontalListView.this.mOnItemLongClicked.onItemLongClick(HorizontalListView.this, childAt, -wrap0, HorizontalListView.this.mAdapter.getItemId(-wrap0));
                    return;
                } else {
                    return;
                }
            }
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            synchronized (HorizontalListView.this) {
                HorizontalListView horizontalListView = HorizontalListView.this;
                horizontalListView.mNextX += (int) f;
            }
            HorizontalListView.this.mIsScrollingPerformed = true;
            HorizontalListView.this.requestLayout();
            return true;
        }

        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            HorizontalListView.this.mBlockNotification = true;
            for (int i = 0; i < HorizontalListView.this.getChildCount(); i++) {
                View childAt = HorizontalListView.this.getChildAt(i);
                if (isEventWithinView(motionEvent, childAt)) {
                    int -wrap0 = HorizontalListView.this.toDataIndex((HorizontalListView.this.mLeftViewIndex + 1) + i);
                    if (HorizontalListView.this.mOnItemClicked != null) {
                        HorizontalListView.this.mOnItemClicked.onItemClick(HorizontalListView.this, childAt, -wrap0, HorizontalListView.this.mAdapter.getItemId(-wrap0));
                    }
                    if (HorizontalListView.this.mOnItemSelected != null) {
                        HorizontalListView.this.mOnItemSelected.onItemSelected(HorizontalListView.this, childAt, -wrap0, HorizontalListView.this.mAdapter.getItemId(-wrap0));
                    }
                    return true;
                }
            }
            return true;
        }
    }

    class C01353 implements Runnable {
        C01353() {
        }

        public void run() {
            HorizontalListView.this.requestLayout();
        }
    }

    class C01364 implements Runnable {
        C01364() {
        }

        public void run() {
            HorizontalListView.this.justify();
        }
    }

    public HorizontalListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initView();
    }

    private void addAndMeasureChild(View view, int i) {
        LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(-1, -1);
        }
        addViewInLayout(view, i, layoutParams, true);
        view.measure(MeasureSpec.makeMeasureSpec(getWidth(), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(getHeight(), Integer.MIN_VALUE));
    }

    private void cacheChildItem(View view) {
        if (this.mRemovedViewQueue.size() < 10) {
            this.mRemovedViewQueue.offer(view);
        }
    }

    private void fillList(int i) {
        int i2 = 0;
        View childAt = getChildAt(getChildCount() - 1);
        if (childAt != null) {
            i2 = childAt.getRight();
        }
        fillListRight(i2, i);
        i2 = 0;
        childAt = getChildAt(0);
        if (childAt != null) {
            i2 = childAt.getLeft();
        }
        fillListLeft(i2, i);
    }

    private void fillListLeft(int i, int i2) {
        while (i + i2 > 0 && this.mLeftViewIndex >= 0) {
            View view = this.mAdapter.getView(toDataIndex(this.mLeftViewIndex), (View) this.mRemovedViewQueue.poll(), this);
            if (this.mSelectCenter || this.mLeftViewIndex != this.mSelectViewIndex) {
                view.setActivated(false);
            } else {
                this.mLastSelectImageListItem = view;
                view.setActivated(true);
            }
            addAndMeasureChild(view, 0);
            i -= getChildWidth();
            this.mLeftViewIndex--;
            this.mDisplayOffset -= getChildWidth();
        }
    }

    private void fillListRight(int i, int i2) {
        while (i + i2 < getWidth() && this.mRightViewIndex < this.mAdapter.getCount()) {
            View view = this.mAdapter.getView(toDataIndex(this.mRightViewIndex), (View) this.mRemovedViewQueue.poll(), this);
            if (this.mSelectCenter || this.mRightViewIndex != this.mSelectViewIndex) {
                view.setActivated(false);
            } else {
                this.mLastSelectImageListItem = view;
                view.setActivated(true);
            }
            addAndMeasureChild(view, -1);
            i += getChildWidth();
            if (this.mRightViewIndex == this.mAdapter.getCount() - 1) {
                this.mMaxX = ((this.mPaddingWidth * 2) + (getChildWidth() * this.mAdapter.getCount())) - getWidth();
            }
            if (this.mMaxX < 0) {
                this.mMaxX = 0;
            }
            this.mRightViewIndex++;
        }
    }

    private int getChildWidth() {
        return this.mItemWidth;
    }

    private synchronized void initView() {
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.mPresetWidth = displayMetrics.widthPixels;
        this.mLeftViewIndex = -1;
        this.mRightViewIndex = 0;
        this.mCurrentX = 0;
        this.mNextX = 0;
        this.mMaxX = Integer.MAX_VALUE;
        if (this.mSelectCenter) {
            this.mPaddingWidth = (this.mPresetWidth - this.mItemWidth) / 2;
            this.mDisplayOffset = this.mPaddingWidth;
        } else {
            this.mDisplayOffset = 0;
        }
        this.mScroller = new Scroller(getContext());
        this.mGesture = new GestureDetector(getContext(), this.mOnGesture);
        if (this.mLastSelectImageListItem != null) {
            this.mLastSelectImageListItem.setActivated(false);
            this.mLastSelectImageListItem = null;
        }
        ((ActivityBase) this.mContext).loadCameraSound(6);
    }

    private void justify() {
        Object obj = 1;
        if (this.mSelectViewIndex > this.mLeftViewIndex && this.mSelectViewIndex < this.mRightViewIndex) {
            obj = Math.abs((getChildAt((this.mSelectViewIndex - this.mLeftViewIndex) + -1).getLeft() + (this.mItemWidth / 2)) - (this.mPresetWidth / 2)) > 10 ? 1 : null;
        }
        if (obj != null) {
            int i = ((this.mPaddingWidth + (this.mItemWidth * this.mSelectViewIndex)) + (this.mItemWidth / 2)) - (this.mPresetWidth / 2);
            this.mMaxX = ((this.mPaddingWidth * 2) + (this.mItemWidth * this.mAdapter.getCount())) - this.mPresetWidth;
            if (i > this.mMaxX) {
                i = this.mMaxX;
            }
            if (i != this.mCurrentX) {
                if (isShown()) {
                    scrollTo(i);
                } else {
                    this.mNextX = i;
                    requestLayout();
                }
            }
        }
    }

    private void loadItems() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i);
        }
    }

    private void notifyItemSelect(View view, int i, long j) {
        if (view != null) {
            if (!this.mBlockNotification) {
                if (this.mOnItemClicked != null) {
                    this.mOnItemClicked.onItemClick(this, view, i, j);
                }
                if (this.mOnItemSelected != null) {
                    this.mOnItemSelected.onItemSelected(this, view, i, j);
                }
            }
            if (this.mLastSelectImageListItem != null) {
                this.mLastSelectImageListItem.setActivated(false);
            }
            this.mLastSelectImageListItem = view;
            view.setActivated(true);
        }
    }

    private void positionItems(int i) {
        if (getChildCount() > 0) {
            this.mDisplayOffset += i;
            int i2 = this.mDisplayOffset;
            int childWidth = getChildWidth();
            int height = getHeight();
            int i3 = this.mPresetWidth / 2;
            int i4 = this.mLeftViewIndex + 1;
            for (int i5 = 0; i5 < getChildCount(); i5++) {
                View childAt = getChildAt(i5);
                Object obj = (childAt.getLeft() >= i3 || childAt.getRight() <= i3) ? null : 1;
                childAt.layout(i2, 0, i2 + childWidth, height);
                if (this.mSelectCenter && i2 < i3 && i2 + childWidth > i3 && obj == null) {
                    int toDataIndex = toDataIndex(i4);
                    notifyItemSelect(childAt, toDataIndex, this.mAdapter.getItemId(toDataIndex));
                }
                i4++;
                i2 += childWidth;
            }
        }
    }

    private void removeNonVisibleItems(int i) {
        View childAt = getChildAt(0);
        int i2 = 0;
        while (childAt != null && childAt.getRight() + i <= 0) {
            this.mDisplayOffset += getChildWidth();
            cacheChildItem(childAt);
            this.mLeftViewIndex++;
            i2++;
            childAt = getChildAt(i2);
        }
        if (i2 > 0) {
            removeViewsInLayout(0, i2 + 0);
        }
        i2 = getChildCount() - 1;
        int i3 = i2;
        childAt = getChildAt(getChildCount() - 1);
        while (childAt != null && childAt.getLeft() + i >= getWidth()) {
            cacheChildItem(childAt);
            this.mRightViewIndex--;
            i3--;
            childAt = getChildAt(i3);
        }
        if (i2 > i3) {
            removeViewsInLayout(i3 + 1, i2 - i3);
        }
    }

    private synchronized void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    private int toDataIndex(int i) {
        return Util.isLayoutRTL(getContext()) ? (this.mAdapter.getCount() - 1) - i : i;
    }

    private int toViewIndex(int i) {
        return Util.isLayoutRTL(getContext()) ? (this.mAdapter.getCount() - 1) - i : i;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        boolean dispatchTouchEvent = super.dispatchTouchEvent(motionEvent) | this.mGesture.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                this.mTouchDown = true;
                this.mBlockNotification = false;
                break;
            case 1:
            case 3:
                if (this.mScroller.isFinished()) {
                    this.mIsScrollingPerformed = false;
                    justify();
                }
                this.mTouchDown = false;
                break;
        }
        return dispatchTouchEvent;
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    public View getSelectedView() {
        return null;
    }

    public boolean isScrolling() {
        return this.mIsScrollingPerformed;
    }

    protected boolean onDown(MotionEvent motionEvent) {
        this.mScroller.forceFinished(true);
        return true;
    }

    protected boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        synchronized (this) {
            this.mScroller.fling(this.mNextX, 0, (int) (-f), 0, 0, this.mMaxX, 0, 0);
        }
        requestLayout();
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void onLayout(boolean r9, int r10, int r11, int r12, int r13) {
        /*
        r8 = this;
        monitor-enter(r8);
        super.onLayout(r9, r10, r11, r12, r13);	 Catch:{ all -> 0x00c0 }
        r6 = r8.mAdapter;	 Catch:{ all -> 0x00c0 }
        if (r6 != 0) goto L_0x000a;
    L_0x0008:
        monitor-exit(r8);
        return;
    L_0x000a:
        r1 = 0;
        r6 = r8.mDataChanged;	 Catch:{ all -> 0x00c0 }
        if (r6 == 0) goto L_0x001d;
    L_0x000f:
        r4 = r8.mCurrentX;	 Catch:{ all -> 0x00c0 }
        r8.initView();	 Catch:{ all -> 0x00c0 }
        r8.removeAllViewsInLayout();	 Catch:{ all -> 0x00c0 }
        r8.mNextX = r4;	 Catch:{ all -> 0x00c0 }
        r6 = 0;
        r8.mDataChanged = r6;	 Catch:{ all -> 0x00c0 }
        r1 = 1;
    L_0x001d:
        r6 = r8.mScroller;	 Catch:{ all -> 0x00c0 }
        r6 = r6.computeScrollOffset();	 Catch:{ all -> 0x00c0 }
        if (r6 == 0) goto L_0x002d;
    L_0x0025:
        r6 = r8.mScroller;	 Catch:{ all -> 0x00c0 }
        r5 = r6.getCurrX();	 Catch:{ all -> 0x00c0 }
        r8.mNextX = r5;	 Catch:{ all -> 0x00c0 }
    L_0x002d:
        r6 = r8.mNextX;	 Catch:{ all -> 0x00c0 }
        if (r6 > 0) goto L_0x003a;
    L_0x0031:
        r6 = 0;
        r8.mNextX = r6;	 Catch:{ all -> 0x00c0 }
        r6 = r8.mScroller;	 Catch:{ all -> 0x00c0 }
        r7 = 1;
        r6.forceFinished(r7);	 Catch:{ all -> 0x00c0 }
    L_0x003a:
        r6 = r8.mNextX;	 Catch:{ all -> 0x00c0 }
        r7 = r8.mMaxX;	 Catch:{ all -> 0x00c0 }
        if (r6 < r7) goto L_0x004a;
    L_0x0040:
        r6 = r8.mMaxX;	 Catch:{ all -> 0x00c0 }
        r8.mNextX = r6;	 Catch:{ all -> 0x00c0 }
        r6 = r8.mScroller;	 Catch:{ all -> 0x00c0 }
        r7 = 1;
        r6.forceFinished(r7);	 Catch:{ all -> 0x00c0 }
    L_0x004a:
        r6 = r8.mCurrentX;	 Catch:{ all -> 0x00c0 }
        r7 = r8.mNextX;	 Catch:{ all -> 0x00c0 }
        r3 = r6 - r7;
        r6 = r8.mNextX;	 Catch:{ all -> 0x00c0 }
        r8.mCurrentX = r6;	 Catch:{ all -> 0x00c0 }
        r8.removeNonVisibleItems(r3);	 Catch:{ all -> 0x00c0 }
        r8.fillList(r3);	 Catch:{ all -> 0x00c0 }
        r8.positionItems(r3);	 Catch:{ all -> 0x00c0 }
        r6 = r8.mScroller;	 Catch:{ all -> 0x00c0 }
        r6 = r6.isFinished();	 Catch:{ all -> 0x00c0 }
        if (r6 == 0) goto L_0x0067;
    L_0x0065:
        if (r1 == 0) goto L_0x0071;
    L_0x0067:
        r6 = new com.android.camera.ui.HorizontalListView$3;	 Catch:{ all -> 0x00c0 }
        r6.<init>();	 Catch:{ all -> 0x00c0 }
        r8.post(r6);	 Catch:{ all -> 0x00c0 }
    L_0x006f:
        monitor-exit(r8);
        return;
    L_0x0071:
        r8.loadItems();	 Catch:{ all -> 0x00c0 }
        r6 = r8.mScroller;	 Catch:{ all -> 0x00c0 }
        r6 = r6.isFinished();	 Catch:{ all -> 0x00c0 }
        if (r6 == 0) goto L_0x006f;
    L_0x007c:
        r6 = r8.mTouchDown;	 Catch:{ all -> 0x00c0 }
        if (r6 != 0) goto L_0x006f;
    L_0x0080:
        r6 = 0;
        r8.mIsScrollingPerformed = r6;	 Catch:{ all -> 0x00c0 }
        r6 = r8.mSelectCenter;	 Catch:{ all -> 0x00c0 }
        if (r6 == 0) goto L_0x008f;
    L_0x0087:
        r6 = new com.android.camera.ui.HorizontalListView$4;	 Catch:{ all -> 0x00c0 }
        r6.<init>();	 Catch:{ all -> 0x00c0 }
        r8.post(r6);	 Catch:{ all -> 0x00c0 }
    L_0x008f:
        r6 = r8.mSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        r7 = r8.mPreviousSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        if (r6 == r7) goto L_0x006f;
    L_0x0095:
        r6 = r8.mSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        r7 = r8.mLeftViewIndex;	 Catch:{ all -> 0x00c0 }
        if (r6 <= r7) goto L_0x00bb;
    L_0x009b:
        r6 = r8.mSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        r7 = r8.mRightViewIndex;	 Catch:{ all -> 0x00c0 }
        if (r6 > r7) goto L_0x00bb;
    L_0x00a1:
        r6 = r8.mSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        r2 = r8.toDataIndex(r6);	 Catch:{ all -> 0x00c0 }
        r6 = r8.mSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        r7 = r8.mLeftViewIndex;	 Catch:{ all -> 0x00c0 }
        r6 = r6 - r7;
        r6 = r6 + -1;
        r0 = r8.getChildAt(r6);	 Catch:{ all -> 0x00c0 }
        r6 = r8.mAdapter;	 Catch:{ all -> 0x00c0 }
        r6 = r6.getItemId(r2);	 Catch:{ all -> 0x00c0 }
        r8.notifyItemSelect(r0, r2, r6);	 Catch:{ all -> 0x00c0 }
    L_0x00bb:
        r6 = r8.mSelectViewIndex;	 Catch:{ all -> 0x00c0 }
        r8.mPreviousSelectViewIndex = r6;	 Catch:{ all -> 0x00c0 }
        goto L_0x006f;
    L_0x00c0:
        r6 = move-exception;
        monitor-exit(r8);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.ui.HorizontalListView.onLayout(boolean, int, int, int, int):void");
    }

    public synchronized void scrollTo(int i) {
        this.mIsScrollingPerformed = true;
        this.mScroller.startScroll(this.mNextX, 0, i - this.mNextX, 0);
        requestLayout();
    }

    public void setAdapter(ListAdapter listAdapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = listAdapter;
        this.mAdapter.registerDataSetObserver(this.mDataObserver);
        reset();
    }

    public void setItemWidth(int i) {
        this.mItemWidth = i;
        if (this.mSelectCenter) {
            this.mPaddingWidth = (this.mPresetWidth - this.mItemWidth) / 2;
            this.mDisplayOffset = this.mPaddingWidth;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClicked = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClicked = onItemLongClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelected = onItemSelectedListener;
    }

    public void setSelection(int i) {
        i = toViewIndex(i);
        if (this.mSelectViewIndex != i) {
            this.mPreviousSelectViewIndex = this.mSelectViewIndex;
            this.mSelectViewIndex = i;
            if (isShown()) {
                ((ActivityBase) this.mContext).playCameraSound(6);
            }
            if (i > this.mLeftViewIndex && i < this.mRightViewIndex) {
                View childAt = getChildAt((i - this.mLeftViewIndex) - 1);
                int toDataIndex = toDataIndex(i);
                notifyItemSelect(childAt, toDataIndex, this.mAdapter.getItemId(toDataIndex));
            }
            if (!this.mIsScrollingPerformed) {
                justify();
            }
        }
    }
}
