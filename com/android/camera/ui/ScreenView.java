package com.android.camera.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Scroller;
import java.security.InvalidParameterException;

public class ScreenView extends ViewGroup {
    protected static final int INDICATOR_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, 0);
    protected static final LayoutParams SEEK_POINT_LAYOUT_PARAMS = new LayoutParams(-1, -1, 1.0f);
    private static final float SMOOTHING_CONSTANT = ((float) (0.016d / Math.log(0.75d)));
    protected final float DEFAULT_CAMERA_DISTANCE;
    private boolean isFromcomputeScroll;
    protected int mActivePointerId;
    private boolean mAllowLongPress;
    private ArrowIndicator mArrowLeft;
    private int mArrowLeftOffResId;
    private int mArrowLeftOnResId;
    private ArrowIndicator mArrowRight;
    private int mArrowRightOffResId;
    private int mArrowRightOnResId;
    protected int mChildScreenWidth;
    private float mConfirmHorizontalScrollRatio;
    private boolean mCurrentGestureFinished;
    protected int mCurrentScreen;
    protected boolean mFirstLayout;
    GestureVelocityTracker mGestureVelocityTracker;
    protected int mHeightMeasureSpec;
    private int mIndicatorCount;
    protected float mLastMotionX;
    protected float mLastMotionY;
    protected OnLongClickListener mLongClickListener;
    private int mMaximumVelocity;
    protected int mNextScreen;
    protected float mOverScrollRatio;
    private float mOvershootTension;
    private ScaleGestureDetector mScaleDetector;
    protected int mScreenAlignment;
    private int mScreenCounter;
    protected int mScreenOffset;
    protected int mScreenPaddingBottom;
    protected int mScreenPaddingTop;
    protected SeekBarIndicator mScreenSeekBar;
    private int mScreenSnapDuration;
    private int mScreenTransitionType;
    protected int mScreenWidth;
    private ScreenViewOvershootInterpolator mScrollInterpolator;
    protected int mScrollLeftBound;
    protected int mScrollOffset;
    protected int mScrollRightBound;
    protected boolean mScrollWholeScreen;
    protected Scroller mScroller;
    private int mSeekPointResId;
    protected SlideBar mSlideBar;
    private float mSmoothingTime;
    private boolean mTouchIntercepted;
    private int mTouchSlop;
    private int mTouchState;
    private float mTouchX;
    protected int mVisibleRange;
    protected int mWidthMeasureSpec;

    private interface Indicator {
        boolean fastOffset(int i);
    }

    protected class ArrowIndicator extends ImageView implements Indicator {
        public boolean fastOffset(int i) {
            if (this.mLeft == i) {
                return false;
            }
            this.mRight = (this.mRight + i) - this.mLeft;
            this.mLeft = i;
            return true;
        }
    }

    private class GestureVelocityTracker {
        private float mFoldX;
        private int mPointerId;
        private float mPrevX;
        private float mStartX;
        private VelocityTracker mVelocityTracker;

        private GestureVelocityTracker() {
            this.mPointerId = -1;
            this.mStartX = -1.0f;
            this.mFoldX = -1.0f;
            this.mPrevX = -1.0f;
        }

        private void reset() {
            this.mPointerId = -1;
            this.mStartX = -1.0f;
            this.mFoldX = -1.0f;
            this.mPrevX = -1.0f;
        }

        public void addMovement(MotionEvent motionEvent) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(motionEvent);
            float x = motionEvent.getX();
            if (this.mPointerId != -1) {
                int findPointerIndex = motionEvent.findPointerIndex(this.mPointerId);
                if (findPointerIndex != -1) {
                    x = motionEvent.getX(findPointerIndex);
                } else {
                    this.mPointerId = -1;
                }
            }
            if (this.mStartX < 0.0f) {
                this.mStartX = x;
            } else if (this.mPrevX < 0.0f) {
                this.mPrevX = x;
            } else {
                if (this.mFoldX < 0.0f) {
                    if (this.mPrevX <= this.mStartX || x >= this.mPrevX) {
                        if (this.mPrevX < this.mStartX && x > this.mPrevX) {
                        }
                    }
                    if (Math.abs(x - this.mStartX) > 3.0f) {
                        this.mFoldX = this.mPrevX;
                    }
                } else if (this.mFoldX != this.mPrevX) {
                    if (this.mPrevX <= this.mFoldX || x >= this.mPrevX) {
                        if (this.mPrevX < this.mFoldX && x > this.mPrevX) {
                        }
                    }
                    if (Math.abs(x - this.mFoldX) > 3.0f) {
                        this.mStartX = this.mFoldX;
                        this.mFoldX = this.mPrevX;
                    }
                }
                this.mPrevX = x;
            }
        }

        public int getFlingDirection(float f) {
            int i = 1;
            if (f <= 300.0f) {
                return 4;
            }
            if (this.mFoldX >= 0.0f) {
                return this.mPrevX < this.mFoldX ? ScreenView.this.mScrollX < ScreenView.this.getCurrentScreen().getLeft() ? 3 : 2 : (this.mPrevX <= this.mFoldX || ScreenView.this.mScrollX > ScreenView.this.getCurrentScreen().getLeft()) ? 3 : 1;
            } else {
                if (this.mPrevX <= this.mStartX) {
                    i = 2;
                }
                return i;
            }
        }

        public float getXVelocity(int i, int i2, int i3) {
            this.mVelocityTracker.computeCurrentVelocity(i, (float) i2);
            return this.mVelocityTracker.getXVelocity(i3);
        }

        public void init(int i) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            } else {
                this.mVelocityTracker.clear();
            }
            reset();
            this.mPointerId = i;
        }

        public void recycle() {
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
            reset();
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new C01521();
        int currentScreen;

        static class C01521 implements Creator<SavedState> {
            C01521() {
            }

            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.currentScreen = -1;
            this.currentScreen = parcel.readInt();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
            this.currentScreen = -1;
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.currentScreen);
        }
    }

    private class ScaleDetectorListener implements OnScaleGestureListener {
        private ScaleDetectorListener() {
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float scaleFactor = scaleGestureDetector.getScaleFactor();
            if (ScreenView.this.mTouchState == 0) {
                if (((float) scaleGestureDetector.getTimeDelta()) <= 200.0f && scaleFactor >= 0.95f) {
                    if (scaleFactor > 1.0526316f) {
                    }
                }
                ScreenView.this.setTouchState(null, 4);
            }
            if (scaleFactor < 0.8f) {
                ScreenView.this.onPinchIn(scaleGestureDetector);
                return true;
            } else if (scaleFactor <= 1.2f) {
                return false;
            } else {
                ScreenView.this.onPinchOut(scaleGestureDetector);
                return true;
            }
        }

        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return ScreenView.this.mTouchState == 0;
        }

        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            ScreenView.this.finishCurrentGesture();
        }
    }

    private class ScreenViewOvershootInterpolator implements Interpolator {
        private float mTension;

        public ScreenViewOvershootInterpolator() {
            this.mTension = ScreenView.this.mOvershootTension;
        }

        public void disableSettle() {
            this.mTension = 0.0f;
        }

        public float getInterpolation(float f) {
            f -= 1.0f;
            return ((f * f) * (((this.mTension + 1.0f) * f) + this.mTension)) + 1.0f;
        }

        public void setDistance(int i, int i2) {
            this.mTension = i > 0 ? ScreenView.this.mOvershootTension / ((float) i) : ScreenView.this.mOvershootTension;
        }
    }

    protected class SeekBarIndicator extends LinearLayout implements Indicator {
        public SeekBarIndicator(Context context) {
            super(context);
            setDrawingCacheEnabled(true);
        }

        public boolean fastOffset(int i) {
            if (this.mLeft == i) {
                return false;
            }
            this.mRight = (this.mRight + i) - this.mLeft;
            this.mLeft = i;
            return true;
        }
    }

    protected class SlideBar extends FrameLayout implements Indicator {
        private Rect mPadding;
        private Rect mPos;
        private NinePatch mSlidePoint;

        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (this.mSlidePoint != null) {
                this.mSlidePoint.draw(canvas, this.mPos);
            }
        }

        public boolean fastOffset(int i) {
            if (this.mLeft == i) {
                return false;
            }
            this.mRight = (this.mRight + i) - this.mLeft;
            this.mLeft = i;
            return true;
        }

        public int getSlideWidth() {
            return (getMeasuredWidth() - this.mPadding.left) - this.mPadding.right;
        }

        protected boolean setFrame(int i, int i2, int i3, int i4) {
            boolean frame = super.setFrame(i, i2, i3, i4);
            if (this.mSlidePoint != null) {
                this.mPos.bottom = (i4 - i2) - this.mPadding.bottom;
                this.mPos.top = this.mPos.bottom - this.mSlidePoint.getHeight();
            }
            return frame;
        }

        public void setPosition(int i, int i2) {
            this.mPos.left = this.mPadding.left + i;
            this.mPos.right = this.mPadding.left + i2;
        }
    }

    public ScreenView(Context context) {
        super(context);
        this.mFirstLayout = true;
        this.mArrowLeftOnResId = C0049R.drawable.screen_view_arrow_left;
        this.mArrowLeftOffResId = C0049R.drawable.screen_view_arrow_left_gray;
        this.mArrowRightOnResId = C0049R.drawable.screen_view_arrow_right;
        this.mArrowRightOffResId = C0049R.drawable.screen_view_arrow_right_gray;
        this.mSeekPointResId = C0049R.drawable.screen_view_seek_point_selector;
        this.mVisibleRange = 1;
        this.mScreenWidth = 0;
        this.mNextScreen = -1;
        this.mOverScrollRatio = 0.33333334f;
        this.mScrollWholeScreen = true;
        this.mScreenCounter = 0;
        this.mTouchState = 0;
        this.isFromcomputeScroll = false;
        this.mAllowLongPress = true;
        this.mActivePointerId = -1;
        this.mConfirmHorizontalScrollRatio = 0.5f;
        this.mScreenSnapDuration = 300;
        this.mScreenTransitionType = 0;
        this.mOvershootTension = 1.3f;
        this.mGestureVelocityTracker = new GestureVelocityTracker();
        this.DEFAULT_CAMERA_DISTANCE = Resources.getSystem().getDisplayMetrics().density * 1280.0f;
        initScreenView();
    }

    public ScreenView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScreenView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFirstLayout = true;
        this.mArrowLeftOnResId = C0049R.drawable.screen_view_arrow_left;
        this.mArrowLeftOffResId = C0049R.drawable.screen_view_arrow_left_gray;
        this.mArrowRightOnResId = C0049R.drawable.screen_view_arrow_right;
        this.mArrowRightOffResId = C0049R.drawable.screen_view_arrow_right_gray;
        this.mSeekPointResId = C0049R.drawable.screen_view_seek_point_selector;
        this.mVisibleRange = 1;
        this.mScreenWidth = 0;
        this.mNextScreen = -1;
        this.mOverScrollRatio = 0.33333334f;
        this.mScrollWholeScreen = true;
        this.mScreenCounter = 0;
        this.mTouchState = 0;
        this.isFromcomputeScroll = false;
        this.mAllowLongPress = true;
        this.mActivePointerId = -1;
        this.mConfirmHorizontalScrollRatio = 0.5f;
        this.mScreenSnapDuration = 300;
        this.mScreenTransitionType = 0;
        this.mOvershootTension = 1.3f;
        this.mGestureVelocityTracker = new GestureVelocityTracker();
        this.DEFAULT_CAMERA_DISTANCE = Resources.getSystem().getDisplayMetrics().density * 1280.0f;
        initScreenView();
    }

    private ImageView createSeekPoint() {
        ImageView imageView = new ImageView(this.mContext);
        imageView.setScaleType(ScaleType.CENTER);
        imageView.setImageResource(this.mSeekPointResId);
        imageView.setPadding(4, 0, 4, 0);
        return imageView;
    }

    private void initScreenView() {
        setAlwaysDrawnWithCacheEnabled(true);
        setClipToPadding(true);
        this.mScrollInterpolator = new ScreenViewOvershootInterpolator();
        this.mScroller = new Scroller(this.mContext, this.mScrollInterpolator);
        setCurrentScreenInner(0);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        setMaximumSnapVelocity(viewConfiguration.getScaledMaximumFlingVelocity());
        this.mScaleDetector = new ScaleGestureDetector(this.mContext, new ScaleDetectorListener());
    }

    private void onTouchEventUnique(MotionEvent motionEvent) {
        this.mGestureVelocityTracker.addMovement(motionEvent);
        if (this.mTouchState == 0 || 4 == this.mTouchState) {
            this.mScaleDetector.onTouchEvent(motionEvent);
        }
    }

    private void refreshScrollBound() {
        this.mScrollLeftBound = ((int) (((float) (-this.mChildScreenWidth)) * this.mOverScrollRatio)) - this.mScrollOffset;
        if (this.mScrollWholeScreen) {
            this.mScrollRightBound = (int) (((float) (((getScreenCount() - 1) / this.mVisibleRange) * this.mScreenWidth)) + (((float) this.mChildScreenWidth) * this.mOverScrollRatio));
        } else {
            this.mScrollRightBound = ((int) ((((float) this.mChildScreenWidth) * (((float) getScreenCount()) + this.mOverScrollRatio)) - ((float) this.mScreenWidth))) + this.mScrollOffset;
        }
    }

    private boolean scrolledFarEnough(MotionEvent motionEvent) {
        float abs = Math.abs(motionEvent.getX(0) - this.mLastMotionX);
        return abs > this.mConfirmHorizontalScrollRatio * Math.abs(motionEvent.getY(0) - this.mLastMotionY) && abs > ((float) (this.mTouchSlop * motionEvent.getPointerCount()));
    }

    private void snapByVelocity(int i) {
        if (this.mChildScreenWidth > 0 && getCurrentScreen() != null) {
            int xVelocity = (int) this.mGestureVelocityTracker.getXVelocity(1000, this.mMaximumVelocity, i);
            int flingDirection = this.mGestureVelocityTracker.getFlingDirection((float) Math.abs(xVelocity));
            if (flingDirection == 1 && this.mCurrentScreen > 0) {
                snapToScreen(this.mCurrentScreen - this.mVisibleRange, xVelocity, true);
            } else if (flingDirection == 2 && this.mCurrentScreen < getScreenCount() - 1) {
                snapToScreen(this.mCurrentScreen + this.mVisibleRange, xVelocity, true);
            } else if (flingDirection == 3) {
                snapToScreen(this.mCurrentScreen, xVelocity, true);
            } else {
                snapToScreen((this.mScrollX + ((this.mChildScreenWidth * (this.mScrollWholeScreen ? this.mVisibleRange : 1)) >> 1)) / this.mChildScreenWidth, 0, true);
            }
        }
    }

    private void updateArrowIndicatorResource(int i) {
        if (this.mArrowLeft != null) {
            this.mArrowLeft.setImageResource(i <= 0 ? this.mArrowLeftOffResId : this.mArrowLeftOnResId);
            this.mArrowRight.setImageResource(i >= ((getScreenCount() * this.mChildScreenWidth) - this.mScreenWidth) - this.mScrollOffset ? this.mArrowRightOffResId : this.mArrowRightOnResId);
        }
    }

    private void updateIndicatorPositions(int i) {
        if (getWidth() > 0) {
            int screenCount = getScreenCount();
            int width = getWidth();
            int height = getHeight();
            for (int i2 = 0; i2 < this.mIndicatorCount; i2++) {
                View childAt = getChildAt(i2 + screenCount);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i3 = 0;
                int i4 = 0;
                int i5 = layoutParams.gravity;
                if (i5 != -1) {
                    int i6 = i5 & 112;
                    switch (i5 & 7) {
                        case 1:
                            i3 = (((width - measuredWidth) / 2) + layoutParams.leftMargin) - layoutParams.rightMargin;
                            break;
                        case 3:
                            i3 = layoutParams.leftMargin;
                            break;
                        case 5:
                            i3 = (width - measuredWidth) - layoutParams.rightMargin;
                            break;
                        default:
                            i3 = layoutParams.leftMargin;
                            break;
                    }
                    switch (i6) {
                        case 16:
                            i4 = (((height - measuredHeight) / 2) + layoutParams.topMargin) - layoutParams.bottomMargin;
                            break;
                        case 48:
                            i4 = layoutParams.topMargin;
                            break;
                        case 80:
                            i4 = (height - measuredHeight) - layoutParams.bottomMargin;
                            break;
                        default:
                            i4 = layoutParams.topMargin;
                            break;
                    }
                }
                if ((childAt.isLayoutRequested() || childAt.getHeight() <= 0 || childAt.getWidth() <= 0) && !this.isFromcomputeScroll) {
                    if (VERSION.SDK_INT > 16) {
                        i = 0;
                    }
                    childAt.layout(i + i3, i4, (i + i3) + measuredWidth, i4 + measuredHeight);
                } else if (VERSION.SDK_INT > 16) {
                    childAt.setTranslationX((float) i);
                } else if (((Indicator) childAt).fastOffset(i + i3)) {
                    childAt.invalidate();
                }
            }
        }
    }

    private void updateScreenOffset() {
        switch (this.mScreenAlignment) {
            case 0:
                this.mScrollOffset = this.mScreenOffset;
                break;
            case 1:
                this.mScrollOffset = 0;
                break;
            case 2:
                this.mScrollOffset = (this.mScreenWidth - this.mChildScreenWidth) / 2;
                break;
            case 3:
                this.mScrollOffset = this.mScreenWidth - this.mChildScreenWidth;
                break;
        }
        this.mScrollOffset += this.mPaddingLeft;
    }

    private void updateSeekPoints(int i, int i2) {
        if (this.mScreenSeekBar != null) {
            int screenCount = getScreenCount();
            if (screenCount <= 1) {
                this.mScreenSeekBar.setVisibility(8);
                return;
            }
            int i3 = 0;
            while (i3 < this.mVisibleRange && i + i3 < screenCount) {
                this.mScreenSeekBar.getChildAt(i + i3).setSelected(false);
                i3++;
            }
            i3 = 0;
            while (i3 < this.mVisibleRange && i2 + i3 < screenCount) {
                this.mScreenSeekBar.getChildAt(i2 + i3).setSelected(true);
                i3++;
            }
        }
    }

    private void updateSlidePointPosition(int i) {
        int screenCount = getScreenCount();
        if (this.mSlideBar != null && screenCount > 0) {
            int slideWidth = this.mSlideBar.getSlideWidth();
            int max = Math.max((slideWidth / screenCount) * this.mVisibleRange, 48);
            int i2 = this.mChildScreenWidth * screenCount;
            int i3 = i2 <= slideWidth ? 0 : ((slideWidth - max) * i) / (i2 - slideWidth);
            this.mSlideBar.setPosition(i3, i3 + max);
            if (isHardwareAccelerated()) {
                this.mSlideBar.invalidate();
            }
        }
    }

    public void addIndicator(View view, FrameLayout.LayoutParams layoutParams) {
        this.mIndicatorCount++;
        super.addView(view, -1, layoutParams);
    }

    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        int screenCount = getScreenCount();
        i = i < 0 ? screenCount : Math.min(i, screenCount);
        this.mScreenCounter++;
        if (this.mScreenSeekBar != null) {
            this.mScreenSeekBar.addView(createSeekPoint(), i, SEEK_POINT_LAYOUT_PARAMS);
            if (getScreenCount() > 1) {
                this.mScreenSeekBar.setVisibility(0);
            }
        }
        refreshScrollBound();
        super.addView(view, i, layoutParams);
    }

    public void computeScroll() {
        this.isFromcomputeScroll = true;
        if (this.mScroller.computeScrollOffset()) {
            int currX = this.mScroller.getCurrX();
            this.mScrollX = currX;
            this.mTouchX = (float) currX;
            this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
            this.mScrollY = this.mScroller.getCurrY();
            postInvalidate();
        } else if (this.mNextScreen != -1) {
            setCurrentScreenInner(Math.max(0, Math.min(this.mNextScreen, getScreenCount() - 1)));
            this.mNextScreen = -1;
        } else if (this.mTouchState == 1) {
            float nanoTime = ((float) System.nanoTime()) / 1.0E9f;
            float f = this.mTouchX - ((float) this.mScrollX);
            this.mScrollX = (int) (((float) this.mScrollX) + (f * ((float) Math.exp((double) ((nanoTime - this.mSmoothingTime) / SMOOTHING_CONSTANT)))));
            this.mSmoothingTime = nanoTime;
            if (f > 1.0f || f < -1.0f) {
                postInvalidate();
            }
        }
        updateIndicatorPositions(this.mScrollX);
        updateSlidePointPosition(this.mScrollX);
        updateArrowIndicatorResource(this.mScrollX);
        this.isFromcomputeScroll = false;
    }

    public boolean dispatchUnhandledMove(View view, int i) {
        if (i == 17) {
            if (this.mCurrentScreen > 0) {
                snapToScreen(this.mCurrentScreen - 1);
                return true;
            }
        } else if (i == 66 && this.mCurrentScreen < getScreenCount() - 1) {
            snapToScreen(this.mCurrentScreen + 1);
            return true;
        }
        return super.dispatchUnhandledMove(view, i);
    }

    protected boolean drawChild(Canvas canvas, View view, long j) {
        updateChildStaticTransformation(view);
        return super.drawChild(canvas, view, j);
    }

    protected void finishCurrentGesture() {
        this.mCurrentGestureFinished = true;
        setTouchState(null, 0);
    }

    public View getCurrentScreen() {
        return getScreen(this.mCurrentScreen);
    }

    public View getScreen(int i) {
        return (i < 0 || i >= getScreenCount()) ? null : getChildAt(i);
    }

    public final int getScreenCount() {
        return this.mScreenCounter;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & 255) {
            case 0:
                motionEvent.setAction(3);
                this.mScaleDetector.onTouchEvent(motionEvent);
                motionEvent.setAction(0);
                this.mCurrentGestureFinished = false;
                this.mTouchIntercepted = false;
                this.mLastMotionX = motionEvent.getX();
                this.mLastMotionY = motionEvent.getY();
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    setTouchState(motionEvent, 1);
                    break;
                }
                this.mAllowLongPress = true;
                break;
            case 1:
            case 3:
                setTouchState(motionEvent, 0);
                break;
            case 2:
                onTouchEventUnique(motionEvent);
                if (this.mTouchState == 0 && scrolledFarEnough(motionEvent)) {
                    setTouchState(motionEvent, 1);
                    break;
                }
        }
        if (2 != (motionEvent.getAction() & 255)) {
            onTouchEventUnique(motionEvent);
        }
        return !this.mCurrentGestureFinished ? (this.mTouchState == 0 || this.mTouchState == 3) ? false : true : true;
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        setFrame(i, i2, i3, i4);
        i += this.mPaddingLeft;
        i3 -= this.mPaddingRight;
        updateIndicatorPositions(this.mScrollX);
        int screenCount = getScreenCount();
        int i5 = 0;
        for (int i6 = 0; i6 < screenCount; i6++) {
            View childAt = getChildAt(i6);
            if (childAt.getVisibility() != 8) {
                childAt.layout(i5, this.mPaddingTop + this.mScreenPaddingTop, childAt.getMeasuredWidth() + i5, (this.mPaddingTop + this.mScreenPaddingTop) + childAt.getMeasuredHeight());
                i5 += childAt.getMeasuredWidth();
            }
        }
        if (this.mScrollWholeScreen && this.mCurrentScreen % this.mVisibleRange > 0) {
            setCurrentScreen(this.mCurrentScreen - (this.mCurrentScreen % this.mVisibleRange));
        }
    }

    protected void onMeasure(int i, int i2) {
        int i3;
        this.mWidthMeasureSpec = i;
        this.mHeightMeasureSpec = i2;
        int i4 = 0;
        int i5 = 0;
        int screenCount = getScreenCount();
        for (i3 = 0; i3 < this.mIndicatorCount; i3++) {
            View childAt = getChildAt(i3 + screenCount);
            ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
            childAt.measure(getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight, layoutParams.width), getChildMeasureSpec(i2, ((this.mPaddingTop + this.mScreenPaddingTop) + this.mPaddingBottom) + this.mScreenPaddingBottom, layoutParams.height));
            i5 = Math.max(i5, childAt.getMeasuredWidth());
            i4 = Math.max(i4, childAt.getMeasuredHeight());
        }
        int i6 = 0;
        int i7 = 0;
        for (i3 = 0; i3 < screenCount; i3++) {
            childAt = getChildAt(i3);
            layoutParams = childAt.getLayoutParams();
            childAt.measure(getChildMeasureSpec(i, this.mPaddingLeft + this.mPaddingRight, layoutParams.width), getChildMeasureSpec(i2, ((this.mPaddingTop + this.mScreenPaddingTop) + this.mPaddingBottom) + this.mScreenPaddingBottom, layoutParams.height));
            i7 = Math.max(i7, childAt.getMeasuredWidth());
            i6 = Math.max(i6, childAt.getMeasuredHeight());
        }
        i5 = Math.max(i7, i5);
        setMeasuredDimension(resolveSize(i5 + (this.mPaddingLeft + this.mPaddingRight), i), resolveSize(Math.max(i6, i4) + (((this.mPaddingTop + this.mScreenPaddingTop) + this.mPaddingBottom) + this.mScreenPaddingBottom), i2));
        if (screenCount > 0) {
            this.mChildScreenWidth = i7;
            this.mScreenWidth = (MeasureSpec.getSize(i) - this.mPaddingLeft) - this.mPaddingRight;
            updateScreenOffset();
            setOverScrollRatio(this.mOverScrollRatio);
            if (this.mChildScreenWidth > 0) {
                this.mVisibleRange = Math.max(1, (this.mScreenWidth + (this.mChildScreenWidth / 2)) / this.mChildScreenWidth);
            }
        }
        if (this.mFirstLayout && this.mVisibleRange > 0) {
            this.mFirstLayout = false;
            setHorizontalScrollBarEnabled(false);
            setCurrentScreen(this.mCurrentScreen);
            setHorizontalScrollBarEnabled(true);
        }
    }

    protected void onPinchIn(ScaleGestureDetector scaleGestureDetector) {
    }

    protected void onPinchOut(ScaleGestureDetector scaleGestureDetector) {
    }

    protected void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            setCurrentScreen(savedState.currentScreen);
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable savedState = new SavedState(super.onSaveInstanceState());
        savedState.currentScreen = this.mCurrentScreen;
        return savedState;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int i = 0;
        if (this.mCurrentGestureFinished) {
            return true;
        }
        if (this.mTouchIntercepted) {
            onTouchEventUnique(motionEvent);
        }
        int findPointerIndex;
        switch (motionEvent.getAction() & 255) {
            case 1:
            case 3:
                if (this.mTouchState == 1) {
                    snapByVelocity(this.mActivePointerId);
                }
                setTouchState(motionEvent, 0);
                break;
            case 2:
                if (this.mTouchState == 0 && scrolledFarEnough(motionEvent)) {
                    setTouchState(motionEvent, 1);
                }
                if (this.mTouchState == 1) {
                    findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex == -1) {
                        setTouchState(motionEvent, 1);
                        findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    }
                    float x = motionEvent.getX(findPointerIndex);
                    float f = this.mLastMotionX - x;
                    this.mLastMotionX = x;
                    if (f == 0.0f) {
                        awakenScrollBars();
                        break;
                    }
                    scrollTo(Math.round(this.mTouchX + f), 0);
                    break;
                }
                break;
            case 6:
                findPointerIndex = (motionEvent.getAction() & 65280) >> 8;
                if (motionEvent.getPointerId(findPointerIndex) == this.mActivePointerId) {
                    if (findPointerIndex == 0) {
                        i = 1;
                    }
                    this.mLastMotionX = motionEvent.getX(i);
                    this.mActivePointerId = motionEvent.getPointerId(i);
                    this.mGestureVelocityTracker.init(this.mActivePointerId);
                    break;
                }
                break;
        }
        this.mTouchIntercepted = true;
        return true;
    }

    public void removeAllScreens() {
        for (int i = 0; i < getScreenCount(); i++) {
            ((ViewGroup) getScreen(i)).removeAllViews();
        }
        removeScreensInLayout(0, getScreenCount());
        requestLayout();
        invalidate();
    }

    public void removeAllViewsInLayout() {
        this.mIndicatorCount = 0;
        this.mScreenCounter = 0;
        super.removeAllViewsInLayout();
    }

    public void removeIndicator(View view) {
        int indexOfChild = indexOfChild(view);
        if (indexOfChild < getScreenCount()) {
            throw new InvalidParameterException("The view passed through the parameter must be indicator.");
        }
        this.mIndicatorCount--;
        super.removeViewAt(indexOfChild);
    }

    public void removeScreensInLayout(int i, int i2) {
        if (i >= 0 && i < getScreenCount()) {
            i2 = Math.min(i2, getScreenCount() - i);
            if (this.mScreenSeekBar != null) {
                this.mScreenSeekBar.removeViewsInLayout(i, i2);
            }
            this.mScreenCounter = 0;
            super.removeViewsInLayout(i, i2);
        }
    }

    public void removeView(View view) {
        throw new UnsupportedOperationException("ScreenView doesn't support remove view directly.");
    }

    public void removeViewAt(int i) {
        throw new UnsupportedOperationException("ScreenView doesn't support remove view directly.");
    }

    public void removeViewInLayout(View view) {
        throw new UnsupportedOperationException("ScreenView doesn't support remove view directly.");
    }

    public void removeViews(int i, int i2) {
        throw new UnsupportedOperationException("ScreenView doesn't support remove view directly.");
    }

    public void removeViewsInLayout(int i, int i2) {
        throw new UnsupportedOperationException("ScreenView doesn't support remove view directly.");
    }

    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        int indexOfChild = indexOfChild(view);
        if (indexOfChild >= getScreenCount()) {
            return super.requestChildRectangleOnScreen(view, rect, z);
        }
        if (indexOfChild == this.mCurrentScreen && this.mScroller.isFinished()) {
            return false;
        }
        snapToScreen(indexOfChild);
        return true;
    }

    protected void resetTransformation(View view) {
        view.setAlpha(1.0f);
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        view.setPivotX(0.0f);
        view.setPivotY(0.0f);
        view.setRotation(0.0f);
        view.setRotationX(0.0f);
        view.setRotationY(0.0f);
        view.setCameraDistance(this.DEFAULT_CAMERA_DISTANCE);
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
    }

    public void scrollTo(int i, int i2) {
        this.mTouchX = (float) Math.max(this.mScrollLeftBound, Math.min(i, this.mScrollRightBound));
        this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
        super.scrollTo((int) this.mTouchX, i2);
    }

    public void scrollToScreen(int i) {
        if (this.mScrollWholeScreen) {
            i -= i % this.mVisibleRange;
        }
        measure(this.mWidthMeasureSpec, this.mHeightMeasureSpec);
        scrollTo((this.mChildScreenWidth * i) - this.mScrollOffset, 0);
    }

    public void setCurrentScreen(int i) {
        if (this.mScrollWholeScreen) {
            i = Math.max(0, Math.min(i, getScreenCount() - 1));
            i -= i % this.mVisibleRange;
        } else {
            i = Math.max(0, Math.min(i, getScreenCount() - this.mVisibleRange));
        }
        setCurrentScreenInner(i);
        if (!this.mFirstLayout) {
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
            }
            scrollToScreen(this.mCurrentScreen);
            invalidate();
        }
    }

    protected void setCurrentScreenInner(int i) {
        updateSeekPoints(this.mCurrentScreen, i);
        this.mCurrentScreen = i;
        this.mNextScreen = -1;
    }

    public void setMaximumSnapVelocity(int i) {
        this.mMaximumVelocity = i;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.mLongClickListener = onLongClickListener;
        int screenCount = getScreenCount();
        for (int i = 0; i < screenCount; i++) {
            getChildAt(i).setOnLongClickListener(onLongClickListener);
        }
    }

    public void setOverScrollRatio(float f) {
        this.mOverScrollRatio = f;
        refreshScrollBound();
    }

    public void setSeekBarPosition(FrameLayout.LayoutParams layoutParams) {
        if (layoutParams != null) {
            if (this.mScreenSeekBar == null) {
                this.mScreenSeekBar = new SeekBarIndicator(this.mContext);
                this.mScreenSeekBar.setGravity(16);
                this.mScreenSeekBar.setAnimationCacheEnabled(false);
                addIndicator(this.mScreenSeekBar, layoutParams);
                return;
            }
            this.mScreenSeekBar.setLayoutParams(layoutParams);
        } else if (this.mScreenSeekBar != null) {
            removeIndicator(this.mScreenSeekBar);
            this.mScreenSeekBar = null;
        }
    }

    public void setSeekPointResource(int i) {
        this.mSeekPointResId = i;
    }

    protected void setTouchState(MotionEvent motionEvent, int i) {
        this.mTouchState = i;
        getParent().requestDisallowInterceptTouchEvent(this.mTouchState != 0);
        if (this.mTouchState == 0) {
            this.mActivePointerId = -1;
            this.mAllowLongPress = false;
            this.mGestureVelocityTracker.recycle();
            return;
        }
        if (motionEvent != null) {
            this.mActivePointerId = motionEvent.getPointerId(0);
        }
        if (this.mAllowLongPress) {
            this.mAllowLongPress = false;
            View childAt = getChildAt(this.mCurrentScreen);
            if (childAt != null) {
                childAt.cancelLongPress();
            }
        }
        if (this.mTouchState == 1) {
            this.mLastMotionX = motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId));
            this.mTouchX = (float) this.mScrollX;
            this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
        }
    }

    public void snapToScreen(int i) {
        snapToScreen(i, 0, false);
    }

    protected void snapToScreen(int i, int i2, boolean z) {
        if (this.mScreenWidth > 0) {
            if (this.mScrollWholeScreen) {
                this.mNextScreen = Math.max(0, Math.min(i, getScreenCount() - 1));
                this.mNextScreen -= this.mNextScreen % this.mVisibleRange;
            } else {
                this.mNextScreen = Math.max(0, Math.min(i, getScreenCount() - this.mVisibleRange));
            }
            int max = Math.max(1, Math.abs(this.mNextScreen - this.mCurrentScreen));
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
            }
            i2 = Math.abs(i2);
            if (z) {
                this.mScrollInterpolator.setDistance(max, i2);
            } else {
                this.mScrollInterpolator.disableSettle();
            }
            int i3 = ((this.mNextScreen * this.mChildScreenWidth) - this.mScrollOffset) - this.mScrollX;
            int abs = (Math.abs(i3) * this.mScreenSnapDuration) / this.mScreenWidth;
            if (i2 > 0) {
                abs += (int) ((((float) abs) / (((float) i2) / 2500.0f)) * 0.4f);
            }
            abs = Math.max(this.mScreenSnapDuration, abs);
            if (max <= 1) {
                abs = Math.min(abs, this.mScreenSnapDuration * 2);
            }
            this.mScroller.startScroll(this.mScrollX, 0, i3, 0, abs);
            invalidate();
        }
    }

    protected void updateChildStaticTransformation(View view) {
        if (!(view instanceof Indicator)) {
            float measuredWidth = (float) view.getMeasuredWidth();
            float measuredHeight = (float) view.getMeasuredHeight();
            float f = measuredWidth / 2.0f;
            float f2 = measuredHeight / 2.0f;
            float measuredWidth2 = (((((float) this.mScrollX) + (((float) getMeasuredWidth()) / 2.0f)) - ((float) view.getLeft())) - f) / measuredWidth;
            switch (this.mScreenTransitionType) {
                case 0:
                    resetTransformation(view);
                    break;
                case 1:
                    resetTransformation(view);
                    break;
                case 2:
                    if (measuredWidth2 != 0.0f && Math.abs(measuredWidth2) <= 1.0f) {
                        view.setAlpha(((1.0f - Math.abs(measuredWidth2)) * 0.7f) + 0.3f);
                        view.setTranslationX(0.0f);
                        view.setTranslationY(0.0f);
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);
                        view.setPivotX(0.0f);
                        view.setPivotY(0.0f);
                        view.setRotation(0.0f);
                        view.setRotationX(0.0f);
                        view.setRotationY(0.0f);
                        view.setCameraDistance(this.DEFAULT_CAMERA_DISTANCE);
                        break;
                    }
                    resetTransformation(view);
                    break;
                    break;
                case 3:
                    if (measuredWidth2 != 0.0f && Math.abs(measuredWidth2) <= 1.0f) {
                        view.setAlpha(1.0f);
                        view.setTranslationX(0.0f);
                        view.setTranslationY(0.0f);
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);
                        view.setPivotX(f);
                        view.setPivotY(measuredHeight);
                        view.setRotation((-measuredWidth2) * 30.0f);
                        view.setRotationX(0.0f);
                        view.setRotationY(0.0f);
                        view.setCameraDistance(this.DEFAULT_CAMERA_DISTANCE);
                        break;
                    }
                    resetTransformation(view);
                    break;
                    break;
                case 4:
                    if (measuredWidth2 != 0.0f && Math.abs(measuredWidth2) <= 1.0f) {
                        view.setAlpha(1.0f);
                        view.setTranslationX(0.0f);
                        view.setTranslationY(0.0f);
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);
                        if (measuredWidth2 < 0.0f) {
                            measuredWidth = 0.0f;
                        }
                        view.setPivotX(measuredWidth);
                        view.setPivotY(f2);
                        view.setRotation(0.0f);
                        view.setRotationX(0.0f);
                        view.setRotationY(-90.0f * measuredWidth2);
                        view.setCameraDistance(5000.0f);
                        break;
                    }
                    resetTransformation(view);
                    break;
                    break;
                case 5:
                    if (measuredWidth2 != 0.0f && Math.abs(measuredWidth2) <= 1.0f) {
                        view.setAlpha(1.0f - Math.abs(measuredWidth2));
                        view.setTranslationY(0.0f);
                        view.setTranslationX((measuredWidth * measuredWidth2) - ((Math.abs(measuredWidth2) * measuredWidth) * 0.3f));
                        float f3 = 1.0f + (0.3f * measuredWidth2);
                        view.setScaleX(f3);
                        view.setScaleY(f3);
                        view.setPivotX(0.0f);
                        view.setPivotY(f2);
                        view.setRotation(0.0f);
                        view.setRotationX(0.0f);
                        view.setRotationY((-measuredWidth2) * 45.0f);
                        view.setCameraDistance(5000.0f);
                        break;
                    }
                    resetTransformation(view);
                    break;
                    break;
                case 7:
                    if (measuredWidth2 > 0.0f) {
                        view.setAlpha(1.0f - measuredWidth2);
                        float f4 = 0.6f + ((1.0f - measuredWidth2) * 0.4f);
                        view.setTranslationX(((1.0f - f4) * measuredWidth) * 3.0f);
                        view.setTranslationY(((1.0f - f4) * measuredHeight) * 0.5f);
                        view.setScaleX(f4);
                        view.setScaleY(f4);
                        view.setPivotX(0.0f);
                        view.setPivotY(0.0f);
                        view.setRotation(0.0f);
                        view.setRotationX(0.0f);
                        view.setRotationY(0.0f);
                        view.setCameraDistance(this.DEFAULT_CAMERA_DISTANCE);
                        break;
                    }
                    resetTransformation(view);
                    break;
                case 8:
                    if (measuredWidth2 != 0.0f && Math.abs(measuredWidth2) <= 1.0f) {
                        view.setAlpha(1.0f - Math.abs(measuredWidth2));
                        view.setTranslationX(measuredWidth * measuredWidth2);
                        view.setTranslationY(0.0f);
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);
                        view.setPivotX(f);
                        view.setPivotY(f2);
                        view.setRotation(0.0f);
                        view.setRotationX(0.0f);
                        view.setRotationY((-measuredWidth2) * 90.0f);
                        view.setCameraDistance(5000.0f);
                        break;
                    }
                    resetTransformation(view);
                    break;
                case 9:
                    updateChildStaticTransformationByScreen(view, measuredWidth2);
                    break;
            }
        }
    }

    protected void updateChildStaticTransformationByScreen(View view, float f) {
    }
}
