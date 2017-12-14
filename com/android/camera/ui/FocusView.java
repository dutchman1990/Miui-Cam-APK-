package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.camera.Camera;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraManager;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.aosp_porting.FeatureParser;
import com.android.camera.aosp_porting.animation.CubicEaseOutInterpolator;
import com.android.camera.preferences.CameraSettingPreferences;
import java.util.Locale;

public class FocusView extends View implements FocusIndicator, V6FunctionUI, Rotatable {
    public static final int BIG_INIT_RADIUS = Util.dpToPixel(55.0f);
    private static final int BIG_LINE_WIDTH = Util.dpToPixel(1.0f);
    private static final int BIG_MAX_RADIUS = Util.dpToPixel(80.0f);
    public static final int BIG_RADIUS = Util.dpToPixel(43.34f);
    private static final float GAP_NUM = FeatureParser.getFloat("camera_exposure_compensation_steps_num", 0.0f).floatValue();
    private static final int MARGIN = Util.dpToPixel(12.0f);
    private static final int MAX_SLIDE_DISTANCE = ((int) (((double) Util.sWindowWidth) * 0.4d));
    private static final int SMALL_LINE_WIDTH = Util.dpToPixel(1.5f);
    private static final int SMALL_MAX_RADIUS = Util.dpToPixel(7.0f);
    private static final int SMALL_MIN_RADIUS = Util.dpToPixel(3.0f);
    private static final int SMALL_RADIUS = Util.dpToPixel(6.0f);
    private static final int TRIANGLE_BASE_DIS = Util.dpToPixel(3.0f);
    private static final int TRIANGLE_BASE_HEIGHT = Util.dpToPixel(5.0f);
    private static final int TRIANGLE_BASE_LEN = Util.dpToPixel(8.0f);
    private static final int TRIANGLE_MAX_DIS = Util.dpToPixel(30.0f);
    private static final int TRIANGLE_MIN_MARGIN = Util.dpToPixel(25.0f);
    private Camera mActivity;
    private RollAdapter mAdapter;
    private long mAdjustedDoneTime;
    private int mBigAlpha = 150;
    private Paint mBigPaint;
    private int mBigRadius = BIG_RADIUS;
    private int mBottomRelative;
    private Drawable mCaptureBitmap;
    private Rect mCaptureBitmapBounds = new Rect();
    private boolean mCaptured;
    private int mCenterFlag = 0;
    private int mCenterX = (Util.sWindowWidth / 2);
    private int mCenterY = (Util.sWindowHeight / 2);
    private int mCurrentDistanceY;
    private int mCurrentItem;
    private int mCurrentMinusCircleCenter;
    private float mCurrentMinusCircleRadius;
    private int mCurrentRadius;
    private int mCurrentRayBottom;
    private int mCurrentRayHeight;
    private int mCurrentRayWidth;
    private int mCurrentViewState = 0;
    private int mCursorState = 0;
    private float mEVAnimationRatio;
    private long mEVAnimationStartTime;
    float mEVCaptureRatio = -1.0f;
    private boolean mEvAdjusted;
    private int mEvTextMargin;
    private int mEvTriangleDis = 0;
    private float mEvValue;
    private ExposureViewListener mExposureViewListener;
    private long mFailTime;
    private GestureDetector mGestureDetector;
    private Handler mHandler = new C01301();
    private int mHeight = Util.sWindowHeight;
    private Paint mIndicatorPaint;
    private Interpolator mInterpolator;
    private boolean mIsDown;
    private boolean mIsDraw;
    private boolean mIsTouchFocus;
    private int mLastItem;
    private MessageDispacher mMessageDispacher;
    private Paint mMinusMoonPaint;
    private int[] mRelativeLocation;
    private int mRotation;
    private SimpleOnGestureListener mSimpleOnGestureListener = new C01312();
    private int mSlideDistance;
    private long mSlideStartTime;
    private int mSmallAlpha = 180;
    private int mSmallLineWidth = SMALL_LINE_WIDTH;
    private Paint mSmallPaint;
    private int mSmallRadius = SMALL_RADIUS;
    private long mStartTime;
    private int mState;
    private long mSuccessTime;
    private Paint mTextPaint;
    private int mWidth = Util.sWindowWidth;

    public interface ExposureViewListener {
        boolean isMeteringAreaOnly();

        boolean isShowCaptureButton();
    }

    class C01301 extends Handler {
        C01301() {
        }

        public void handleMessage(Message message) {
            if (FocusView.this.mAdapter != null) {
                long uptimeMillis;
                float -wrap2;
                switch (message.what) {
                    case 1:
                        FocusView.this.invalidate();
                        uptimeMillis = SystemClock.uptimeMillis() - FocusView.this.mStartTime;
                        if (uptimeMillis <= 220) {
                            -wrap2 = FocusView.this.getInterpolation(((float) uptimeMillis) / 200.0f);
                            if (FocusView.this.isStableStart()) {
                                FocusView.this.mSmallRadius = FocusView.SMALL_MAX_RADIUS;
                                FocusView.this.mBigRadius = FocusView.BIG_RADIUS;
                                FocusView.this.mBigAlpha = 150;
                                FocusView.this.mEVCaptureRatio = 1.0f;
                            } else {
                                FocusView.this.mBigRadius = (int) (((float) FocusView.BIG_RADIUS) + ((1.0f - -wrap2) * ((float) (FocusView.BIG_INIT_RADIUS - FocusView.BIG_RADIUS))));
                                FocusView.this.mBigAlpha = (int) (150.0f * -wrap2);
                                FocusView.this.mEVCaptureRatio = -1.0f;
                            }
                            FocusView.this.mCenterFlag = 0;
                            FocusView.this.mEvTriangleDis = 0;
                            FocusView.this.mHandler.sendEmptyMessageDelayed(1, 20);
                            FocusView.this.processParameterIfNeeded(-wrap2);
                            break;
                        }
                        return;
                    case 2:
                        FocusView.this.invalidate();
                        if (FocusView.this.mState == 2) {
                            uptimeMillis = SystemClock.uptimeMillis() - FocusView.this.mSuccessTime;
                            if (uptimeMillis < 150) {
                                -wrap2 = FocusView.this.getInterpolation(((float) uptimeMillis) / 130.0f);
                                if (!FocusView.this.mIsTouchFocus || !FocusView.this.mExposureViewListener.isShowCaptureButton()) {
                                    FocusView.this.mCenterFlag = 0;
                                    FocusView.this.mSmallRadius = (int) (((float) FocusView.SMALL_RADIUS) + (((float) (FocusView.SMALL_MAX_RADIUS - FocusView.SMALL_RADIUS)) * -wrap2));
                                } else if (-wrap2 <= 0.5f) {
                                    FocusView.this.mSmallRadius = (int) (((float) FocusView.SMALL_RADIUS) - (((float) (FocusView.SMALL_RADIUS - FocusView.SMALL_MIN_RADIUS)) * (-wrap2 * 2.0f)));
                                    FocusView.this.mEVCaptureRatio = -1.0f;
                                    FocusView.this.mCenterFlag = 0;
                                } else {
                                    FocusView.this.mSmallRadius = 0;
                                    -wrap2 = (-wrap2 - 0.5f) * 2.0f;
                                    FocusView.this.mCenterFlag = 1;
                                    FocusView.this.mEVCaptureRatio = (0.6f * -wrap2) + 0.4f;
                                }
                                FocusView.this.mBigRadius = FocusView.BIG_RADIUS;
                                FocusView.this.mBigAlpha = 150;
                                FocusView.this.mEvTriangleDis = 0;
                                FocusView.this.mHandler.sendEmptyMessageDelayed(2, 20);
                                FocusView.this.processParameterIfNeeded(0.0f);
                                break;
                            }
                            return;
                        }
                        break;
                    case 3:
                        FocusView.this.invalidate();
                        if (FocusView.this.mState == 3) {
                            uptimeMillis = SystemClock.uptimeMillis() - FocusView.this.mFailTime;
                            if (uptimeMillis < 320) {
                                -wrap2 = FocusView.this.getInterpolation(((float) uptimeMillis) / 300.0f);
                                FocusView.this.mSmallAlpha = (int) ((1.0f - -wrap2) * 180.0f);
                                FocusView.this.mSmallLineWidth = (int) (((float) FocusView.SMALL_LINE_WIDTH) + ((((float) FocusView.SMALL_LINE_WIDTH) * -wrap2) / 2.0f));
                                FocusView.this.mBigRadius = (int) (((float) FocusView.BIG_RADIUS) + (((float) (FocusView.BIG_MAX_RADIUS - FocusView.BIG_RADIUS)) * -wrap2));
                                FocusView.this.mBigAlpha = (int) ((1.0f - -wrap2) * 150.0f);
                                FocusView.this.mEvTriangleDis = 0;
                                FocusView.this.mEVCaptureRatio = -1.0f;
                                FocusView.this.mCenterFlag = 0;
                                FocusView.this.mHandler.sendEmptyMessageDelayed(3, 20);
                                FocusView.this.processParameterIfNeeded(0.0f);
                                break;
                            }
                            return;
                        }
                        break;
                    case 4:
                    case 5:
                        if (!FocusView.this.mIsDraw || !FocusView.this.mIsDown) {
                            FocusView.this.reset();
                            break;
                        }
                        FocusView.this.clearMessages();
                        sendEmptyMessageDelayed(5, 50);
                        break;
                    case 6:
                        FocusView.this.mCurrentViewState = 0;
                        FocusView.this.mAdjustedDoneTime = System.currentTimeMillis();
                        FocusView.this.calculateAttribute();
                        FocusView.this.invalidate();
                        break;
                    case 7:
                        uptimeMillis = SystemClock.uptimeMillis() - FocusView.this.mEVAnimationStartTime;
                        if (uptimeMillis < 520) {
                            FocusView.this.mEVAnimationRatio = ((float) uptimeMillis) / 500.0f;
                            FocusView.this.calculateAttribute();
                            FocusView.this.invalidate();
                            sendEmptyMessageDelayed(7, 20);
                            break;
                        }
                        FocusView.this.mCurrentViewState = 1;
                        FocusView.this.mCursorState = 0;
                        if (!hasMessages(8)) {
                            removeMessages(6);
                            sendEmptyMessageDelayed(6, 500);
                        }
                        return;
                    case 8:
                        uptimeMillis = SystemClock.uptimeMillis() - FocusView.this.mSlideStartTime;
                        if (uptimeMillis < 320) {
                            FocusView.this.mCurrentDistanceY = (int) (((float) FocusView.this.mSlideDistance) * (1.0f - FocusView.this.getInterpolation(((float) uptimeMillis) / 300.0f)));
                            FocusView.this.invalidate();
                            sendEmptyMessageDelayed(8, 20);
                            break;
                        }
                        FocusView.this.mCursorState = 0;
                        if (!hasMessages(7)) {
                            removeMessages(6);
                            sendEmptyMessageDelayed(6, 500);
                        }
                        return;
                }
            }
        }
    }

    class C01312 extends SimpleOnGestureListener {
        C01312() {
        }

        public boolean onDown(MotionEvent motionEvent) {
            if (!FocusView.this.mIsDraw) {
                return false;
            }
            if (FocusView.this.mCurrentViewState == 0 && V6ModulePicker.isCameraModule() && FocusView.this.isInCircle(motionEvent.getX() - ((float) FocusView.this.mRelativeLocation[0]), motionEvent.getY() - ((float) FocusView.this.mRelativeLocation[1]), ((float) FocusView.this.mBigRadius) * 0.4f)) {
                if (!(FocusView.this.mMessageDispacher == null || FocusView.this.mAdapter == null)) {
                    FocusView.this.mMessageDispacher.dispacherMessage(2, C0049R.id.v6_focus_view, 3, null, null);
                }
                CameraDataAnalytics.instance().trackEvent("capture_times_focus_view");
                CameraDataAnalytics.instance().trackEvent("touch_focus_focus_view_capture_times_key");
                FocusView.this.mCaptured = true;
            } else {
                FocusView.this.mIsDown = true;
                FocusView.this.removeMessages();
                FocusView.this.setTouchDown();
            }
            return true;
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (!FocusView.this.mIsDown) {
                return false;
            }
            int gestureOrientation = V6GestureRecognizer.getInstance(FocusView.this.mActivity).getGestureOrientation();
            if ((gestureOrientation != 200 || (FocusView.this.mRotation != 0 && FocusView.this.mRotation != 180)) && (gestureOrientation != 100 || (FocusView.this.mRotation != 90 && FocusView.this.mRotation != 270))) {
                return false;
            }
            FocusView focusView;
            switch (FocusView.this.mRotation) {
                case 0:
                    focusView = FocusView.this;
                    focusView.mCurrentDistanceY = (int) (((float) focusView.mCurrentDistanceY) - f2);
                    break;
                case 90:
                    focusView = FocusView.this;
                    focusView.mCurrentDistanceY = (int) (((float) focusView.mCurrentDistanceY) - f);
                    break;
                case 180:
                    focusView = FocusView.this;
                    focusView.mCurrentDistanceY = (int) (((float) focusView.mCurrentDistanceY) + f2);
                    break;
                case 270:
                    focusView = FocusView.this;
                    focusView.mCurrentDistanceY = (int) (((float) focusView.mCurrentDistanceY) + f);
                    break;
            }
            int -wrap3 = FocusView.this.getItemByCoordinate();
            if (-wrap3 != FocusView.this.mCurrentItem) {
                if (FocusView.this.mCurrentViewState != 3 && -wrap3 < FocusView.this.mCurrentItem && FocusView.this.mCurrentItem >= FocusView.this.mAdapter.getCenterIndex() && -wrap3 < FocusView.this.mAdapter.getCenterIndex()) {
                    FocusView.this.startAnimation();
                    FocusView.this.mLastItem = FocusView.this.mCurrentItem;
                    FocusView.this.mCurrentViewState = 3;
                } else if (FocusView.this.mCurrentViewState != 4 && -wrap3 > FocusView.this.mCurrentItem && FocusView.this.mCurrentItem < FocusView.this.mAdapter.getCenterIndex() && -wrap3 >= FocusView.this.mAdapter.getCenterIndex()) {
                    FocusView.this.startAnimation();
                    FocusView.this.mLastItem = FocusView.this.mCurrentItem;
                    FocusView.this.mCurrentViewState = 4;
                }
                FocusView.this.setCurrentItem(-wrap3, false);
            }
            if (FocusView.this.mCurrentViewState == 0 || FocusView.this.mCurrentViewState == 1) {
                FocusView.this.mCurrentViewState = 1;
                FocusView.this.calculateAttribute();
                FocusView.this.invalidate();
                FocusView.this.mHandler.removeMessages(6);
            }
            return true;
        }
    }

    public FocusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mActivity = (Camera) context;
        this.mBigPaint = new Paint();
        this.mBigPaint.setAntiAlias(true);
        this.mBigPaint.setStrokeWidth((float) BIG_LINE_WIDTH);
        this.mBigPaint.setStyle(Style.STROKE);
        this.mBigPaint.setAlpha(this.mBigAlpha);
        this.mSmallPaint = new Paint();
        this.mSmallPaint.setAntiAlias(true);
        this.mSmallPaint.setStyle(Style.STROKE);
        this.mSmallPaint.setStrokeWidth((float) SMALL_LINE_WIDTH);
        this.mSmallPaint.setAlpha(this.mSmallAlpha);
        this.mInterpolator = new CubicEaseOutInterpolator();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(C0049R.style.SettingStatusBarText, new int[]{16842901, 16842904});
        this.mTextPaint = new Paint();
        this.mTextPaint.setColor(obtainStyledAttributes.getColor(obtainStyledAttributes.getIndex(1), -1));
        this.mTextPaint.setStyle(Style.FILL);
        this.mTextPaint.setTextSize((float) obtainStyledAttributes.getDimensionPixelSize(obtainStyledAttributes.getIndex(0), 0));
        this.mTextPaint.setTextAlign(Align.LEFT);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setAlpha(192);
        this.mIndicatorPaint = new Paint();
        this.mIndicatorPaint.setColor(-1);
        this.mIndicatorPaint.setStyle(Style.FILL);
        this.mIndicatorPaint.setAntiAlias(true);
        this.mMinusMoonPaint = new Paint();
        this.mMinusMoonPaint.setColor(-1);
        this.mMinusMoonPaint.setStyle(Style.FILL);
        this.mMinusMoonPaint.setAntiAlias(true);
        this.mMinusMoonPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        this.mGestureDetector = new GestureDetector(context, this.mSimpleOnGestureListener);
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mEvTextMargin = context.getResources().getDimensionPixelSize(C0049R.dimen.focus_view_ev_text_margin);
        this.mWidth = Util.sWindowWidth;
        this.mHeight = Util.sWindowHeight;
        this.mCenterX = this.mWidth / 2;
        this.mCenterY = this.mHeight / 2;
        this.mCaptureBitmap = getResources().getDrawable(C0049R.drawable.bg_capture);
        this.mCaptureBitmap.setFilterBitmap(true);
        this.mCaptureBitmapBounds.set((-this.mCaptureBitmap.getIntrinsicWidth()) / 2, (-this.mCaptureBitmap.getIntrinsicHeight()) / 2, this.mCaptureBitmap.getIntrinsicWidth() / 2, this.mCaptureBitmap.getIntrinsicHeight() / 2);
    }

    private void calculateAttribute() {
        float itemRatio = getItemRatio(this.mCurrentItem);
        float itemRatio2 = getItemRatio(this.mLastItem);
        float f;
        switch (this.mCurrentViewState) {
            case 0:
                this.mCenterFlag = this.mExposureViewListener.isShowCaptureButton() ? 1 : 0;
                return;
            case 1:
                f = itemRatio;
                if (this.mCurrentItem < this.mAdapter.getCenterIndex()) {
                    this.mCurrentRadius = Util.dpToPixel((2.0f * itemRatio) + 6.0f);
                    this.mCurrentMinusCircleCenter = (int) (((float) this.mCurrentRadius) * 0.5f);
                    this.mCurrentMinusCircleRadius = ((float) this.mCurrentRadius) * 0.8f;
                    this.mCenterFlag = 3;
                    return;
                }
                this.mCurrentRayWidth = Util.dpToPixel(1.5f);
                this.mCurrentRayHeight = Util.dpToPixel((2.0f * itemRatio) + 5.0f);
                this.mCurrentRayBottom = Util.dpToPixel((3.0f * itemRatio) + 7.5f);
                this.mCurrentRadius = Util.dpToPixel((2.0f * itemRatio) + 5.0f);
                this.mCenterFlag = 2;
                return;
            case 3:
                if (this.mEVAnimationRatio <= 0.5f) {
                    f = 2.0f * this.mEVAnimationRatio;
                    this.mCurrentRayWidth = Util.dpToPixel(1.5f);
                    this.mCurrentRayHeight = Util.dpToPixel(((((1.0f - f) * itemRatio2) - f) * 2.0f) + 5.0f);
                    this.mCurrentRayBottom = Util.dpToPixel(((((1.0f - f) * itemRatio2) - f) * 3.0f) + 7.5f);
                    this.mCurrentRadius = Util.dpToPixel(((3.0f * f) + 5.0f) + ((2.0f * itemRatio2) * (1.0f - f)));
                    this.mCenterFlag = 2;
                    return;
                }
                f = 2.0f * (this.mEVAnimationRatio - 0.5f);
                this.mCurrentRadius = Util.dpToPixel(8.0f - (((1.0f - itemRatio) * f) * 2.0f));
                this.mCurrentMinusCircleCenter = (int) (((float) this.mCurrentRadius) * (((1.0f - f) * 0.914f) + 0.5f));
                this.mCurrentMinusCircleRadius = ((float) this.mCurrentRadius) * (((1.0f - f) * 0.2f) + 0.8f);
                this.mCenterFlag = 3;
                return;
            case 4:
                if (this.mEVAnimationRatio < 0.5f) {
                    f = 2.0f * this.mEVAnimationRatio;
                    this.mCurrentRadius = Util.dpToPixel(((((1.0f - itemRatio2) * f) + itemRatio2) * 2.0f) + 6.0f);
                    this.mCurrentMinusCircleCenter = (int) (((float) this.mCurrentRadius) * ((0.914f * f) + 0.5f));
                    this.mCurrentMinusCircleRadius = ((float) this.mCurrentRadius) * ((0.2f * f) + 0.8f);
                    this.mCenterFlag = 3;
                    return;
                }
                f = 2.0f * (this.mEVAnimationRatio - 0.5f);
                this.mCurrentRayWidth = Util.dpToPixel(1.5f);
                this.mCurrentRayHeight = Util.dpToPixel((((itemRatio * f) - (1.0f - f)) * 2.0f) + 5.0f);
                this.mCurrentRayBottom = Util.dpToPixel((((itemRatio * f) - (1.0f - f)) * 3.0f) + 7.5f);
                this.mCurrentRadius = Util.dpToPixel((((1.0f - f) * 3.0f) + 5.0f) + ((2.0f * itemRatio) * f));
                this.mCenterFlag = 2;
                return;
            default:
                return;
        }
    }

    private void clearMessages() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(5);
        this.mHandler.removeMessages(8);
        this.mHandler.removeMessages(6);
        this.mHandler.removeMessages(7);
        this.mHandler.removeMessages(8);
    }

    private void drawCaptureBitmap(Canvas canvas) {
        if (this.mExposureViewListener.isShowCaptureButton()) {
            this.mCaptureBitmap.setBounds((int) (((float) this.mCaptureBitmapBounds.left) * this.mEVCaptureRatio), (int) (((float) this.mCaptureBitmapBounds.top) * this.mEVCaptureRatio), (int) (((float) this.mCaptureBitmapBounds.right) * this.mEVCaptureRatio), (int) (((float) this.mCaptureBitmapBounds.bottom) * this.mEVCaptureRatio));
            this.mCaptureBitmap.draw(canvas);
        }
    }

    private void drawCenterIndicator(Canvas canvas) {
        canvas.save();
        canvas.translate((float) this.mCenterX, (float) this.mCenterY);
        if (this.mAdapter != null && this.mCenterFlag != 0) {
            switch (this.mCenterFlag) {
                case 1:
                    drawCaptureBitmap(canvas);
                    break;
                case 2:
                    drawSun(canvas);
                    break;
                case 3:
                    canvas.drawCircle(0.0f, 0.0f, (float) this.mCurrentRadius, this.mIndicatorPaint);
                    canvas.drawCircle((float) (-this.mCurrentMinusCircleCenter), (float) (-this.mCurrentMinusCircleCenter), this.mCurrentMinusCircleRadius, this.mMinusMoonPaint);
                    break;
                default:
                    break;
            }
        }
        this.mSmallPaint.setColor(Color.argb(this.mSmallAlpha, 255, 255, 255));
        this.mSmallPaint.setStrokeWidth((float) this.mSmallLineWidth);
        canvas.drawCircle(0.0f, 0.0f, (float) this.mSmallRadius, this.mSmallPaint);
        canvas.translate((float) (-this.mCenterX), (float) (-this.mCenterY));
        canvas.restore();
    }

    private void drawCursor(Canvas canvas) {
        Object obj = 1;
        if (this.mIsTouchFocus && this.mAdapter != null) {
            Path path = new Path();
            if (1 != getLayoutDirection()) {
                obj = null;
            }
            int i = ((obj != null || ((Util.sWindowWidth - this.mCenterX) - BIG_RADIUS) - MARGIN >= TRIANGLE_MIN_MARGIN) && (obj == null || (this.mCenterX - BIG_RADIUS) - MARGIN < TRIANGLE_MIN_MARGIN)) ? ((this.mCenterX + BIG_RADIUS) + MARGIN) - (TRIANGLE_BASE_LEN / 2) : ((this.mCenterX - BIG_RADIUS) - MARGIN) - (TRIANGLE_BASE_LEN / 2);
            int i2 = (this.mCenterY + this.mCurrentDistanceY) - ((this.mEvTriangleDis + TRIANGLE_BASE_DIS) / 2);
            path.moveTo((float) i, (float) i2);
            path.lineTo((float) (TRIANGLE_BASE_LEN + i), (float) i2);
            path.lineTo((float) ((TRIANGLE_BASE_LEN / 2) + i), (float) (i2 - TRIANGLE_BASE_HEIGHT));
            path.lineTo((float) i, (float) i2);
            i2 = (this.mCenterY + this.mCurrentDistanceY) + ((this.mEvTriangleDis + TRIANGLE_BASE_DIS) / 2);
            path.moveTo((float) i, (float) i2);
            path.lineTo((float) (TRIANGLE_BASE_LEN + i), (float) i2);
            path.lineTo((float) ((TRIANGLE_BASE_LEN / 2) + i), (float) (TRIANGLE_BASE_HEIGHT + i2));
            path.lineTo((float) i, (float) i2);
            if (this.mState == 3) {
                this.mIndicatorPaint.setAlpha(this.mBigAlpha);
            } else {
                this.mIndicatorPaint.setAlpha(255);
            }
            canvas.drawPath(path, this.mIndicatorPaint);
        }
    }

    private void drawEvText(Canvas canvas) {
        if (this.mIsTouchFocus && ((double) Math.abs(this.mEvValue)) > 0.05d && this.mCenterFlag != 1 && this.mCenterFlag != 0) {
            String str = this.mEvValue < 0.0f ? "-" : "+";
            String format = String.format(Locale.ENGLISH, "%s %.1f", new Object[]{str, Float.valueOf(Math.abs(this.mEvValue))});
            canvas.drawText(format, (((float) this.mCenterX) - this.mTextPaint.measureText(format.split("\\.")[0])) - (this.mTextPaint.measureText(".") / 2.0f), (float) ((this.mCenterY - BIG_RADIUS) - this.mEvTextMargin), this.mTextPaint);
        }
    }

    private void drawSun(Canvas canvas) {
        canvas.rotate((float) getCurrentAngle());
        for (int i = 0; i < 2; i++) {
            if (i > 0) {
                canvas.rotate(45.0f);
            }
            canvas.drawRect((float) ((-this.mCurrentRayWidth) / 2), (float) ((-this.mCurrentRayBottom) - this.mCurrentRayHeight), (float) (this.mCurrentRayWidth / 2), (float) (-this.mCurrentRayBottom), this.mIndicatorPaint);
            canvas.drawRect((float) ((-this.mCurrentRayWidth) / 2), (float) this.mCurrentRayBottom, (float) (this.mCurrentRayWidth / 2), (float) (this.mCurrentRayBottom + this.mCurrentRayHeight), this.mIndicatorPaint);
            canvas.drawRect((float) ((-this.mCurrentRayBottom) - this.mCurrentRayHeight), (float) ((-this.mCurrentRayWidth) / 2), (float) (-this.mCurrentRayBottom), (float) (this.mCurrentRayWidth / 2), this.mIndicatorPaint);
            canvas.drawRect((float) this.mCurrentRayBottom, (float) ((-this.mCurrentRayWidth) / 2), (float) (this.mCurrentRayBottom + this.mCurrentRayHeight), (float) (this.mCurrentRayWidth / 2), this.mIndicatorPaint);
        }
        canvas.drawCircle(0.0f, 0.0f, (float) this.mCurrentRadius, this.mIndicatorPaint);
    }

    private int getCurrentAngle() {
        int i = 0;
        if (this.mCursorState == 2 && this.mCurrentViewState != 3 && this.mCurrentViewState != 4) {
            if (this.mCurrentItem >= this.mAdapter.getCenterIndex()) {
                i = ((this.mCurrentItem - this.mAdapter.getCenterIndex()) * 360) / this.mAdapter.getCenterIndex();
            }
            return 360 - Util.clamp(i, 0, 360);
        } else if (this.mCurrentViewState != 1) {
            return this.mCurrentViewState == 3 ? Util.clamp((int) ((this.mEVAnimationRatio * 2.0f) * 135.0f), 0, 135) : this.mCurrentViewState == 4 ? Util.clamp((int) ((1.0f - ((this.mEVAnimationRatio - 0.5f) * 2.0f)) * 135.0f), 0, 135) : 0;
        } else {
            int clamp = Util.clamp(this.mBottomRelative - this.mCurrentDistanceY, 0, MAX_SLIDE_DISTANCE);
            if (clamp >= MAX_SLIDE_DISTANCE / 2) {
                i = ((clamp - (MAX_SLIDE_DISTANCE / 2)) * 360) / (MAX_SLIDE_DISTANCE / 2);
            }
            return 360 - Util.clamp(i, 0, 360);
        }
    }

    private float getInterpolation(float f) {
        float interpolation = this.mInterpolator.getInterpolation(f);
        return ((double) interpolation) > 1.0d ? 1.0f : interpolation;
    }

    private int getItemByCoordinate() {
        return Util.clamp((this.mAdapter.getMaxItem() * (this.mBottomRelative - this.mCurrentDistanceY)) / MAX_SLIDE_DISTANCE, 0, this.mAdapter.getMaxItem());
    }

    private float getItemRatio(int i) {
        float maxItem = ((float) i) / ((float) this.mAdapter.getMaxItem());
        return maxItem >= 0.5f ? 2.0f * (maxItem - 0.5f) : maxItem * 2.0f;
    }

    private boolean isInCircle(float f, float f2, float f3) {
        float f4 = f - ((float) this.mCenterX);
        float f5 = f2 - ((float) this.mCenterY);
        return Math.sqrt((double) ((f4 * f4) + (f5 * f5))) <= ((double) f3);
    }

    private boolean isStableStart() {
        return this.mIsTouchFocus ? this.mExposureViewListener.isMeteringAreaOnly() : false;
    }

    private void performSlideBack() {
        this.mHandler.removeMessages(6);
        if (this.mCurrentDistanceY != 0) {
            this.mSlideDistance = this.mCurrentDistanceY;
            this.mSlideStartTime = SystemClock.uptimeMillis();
            this.mCursorState = 2;
            this.mHandler.removeMessages(8);
            this.mHandler.sendEmptyMessage(8);
            return;
        }
        this.mHandler.sendEmptyMessage(6);
    }

    private void processParameterIfNeeded(float f) {
        if (this.mIsTouchFocus && this.mEVCaptureRatio != -1.0f && this.mCenterFlag == 0) {
            this.mCenterFlag = 1;
        }
    }

    private void reload() {
        if (this.mAdapter != null) {
            this.mCurrentItem = 0;
            this.mCurrentItem = this.mAdapter.getItemIndexByValue(Integer.valueOf(CameraSettings.readExposure(CameraSettingPreferences.instance())));
            updateEV();
        }
    }

    private void removeMessages() {
        this.mHandler.removeMessages(8);
    }

    private void reset() {
        clearMessages();
        this.mState = 0;
        setPosition(this.mWidth / 2, this.mHeight / 2);
        this.mCurrentDistanceY = 0;
        this.mCurrentViewState = 0;
        this.mCenterFlag = 0;
        this.mIsDown = false;
        stopEvAdjust();
        setDraw(false);
        invalidate();
    }

    private void setCurrentItem(int i, boolean z) {
        if (i != this.mCurrentItem) {
            this.mCurrentItem = i;
            if (!(this.mMessageDispacher == null || this.mAdapter == null)) {
                this.mEvAdjusted = true;
                this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_focus_view, 2, Integer.valueOf(this.mAdapter.getItemValue(i)), Integer.valueOf(1));
            }
            updateEV();
        }
    }

    private void setDraw(boolean z) {
        if (z && this.mIsTouchFocus && this.mIsDraw != z) {
            reload();
        }
        this.mIsDraw = z;
    }

    private void setTouchDown() {
        this.mBottomRelative = (MAX_SLIDE_DISTANCE * this.mCurrentItem) / this.mAdapter.getMaxItem();
    }

    private void startAnimation() {
        this.mEVAnimationStartTime = SystemClock.uptimeMillis();
        this.mHandler.removeMessages(7);
        this.mHandler.removeMessages(6);
        this.mHandler.sendEmptyMessage(7);
    }

    private void stopEvAdjust() {
        if (this.mEvAdjusted) {
            this.mEvAdjusted = false;
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_focus_view, 2, Integer.valueOf(0), Integer.valueOf(2));
            }
        }
    }

    private void updateEV() {
        Parameters stashParameters = CameraManager.instance().getStashParameters();
        if (this.mAdapter == null || stashParameters == null) {
            this.mEvValue = 0.0f;
        } else {
            this.mEvValue = ((float) this.mAdapter.getItemValue(this.mCurrentItem)) * stashParameters.getExposureCompensationStep();
        }
    }

    public void clear() {
        if (this.mIsDraw) {
            reset();
            invalidate();
        }
    }

    public void enableControls(boolean z) {
    }

    public void initialize(ExposureViewListener exposureViewListener) {
        this.mExposureViewListener = exposureViewListener;
        clear();
    }

    public boolean isEvAdjusted() {
        return !this.mEvAdjusted ? this.mCaptured : true;
    }

    public boolean isEvAdjustedTime() {
        return (isShown() && this.mIsTouchFocus) ? !this.mEvAdjusted ? !Util.isTimeout(System.currentTimeMillis(), this.mAdjustedDoneTime, 2000) : true : false;
    }

    public boolean isVisible() {
        return this.mIsDraw;
    }

    public void onCameraOpen() {
        Parameters stashParameters = CameraManager.instance().getStashParameters();
        if (stashParameters != null) {
            int minExposureCompensation = stashParameters.getMinExposureCompensation();
            int maxExposureCompensation = stashParameters.getMaxExposureCompensation();
            if (maxExposureCompensation != 0 && maxExposureCompensation != minExposureCompensation) {
                this.mAdapter = new FloatSlideAdapter(minExposureCompensation, maxExposureCompensation, GAP_NUM == 0.0f ? 1.0f : ((float) (maxExposureCompensation - minExposureCompensation)) / GAP_NUM);
                if (this.mAdapter != null) {
                    this.mCurrentItem = 0;
                    int itemIndexByValue = this.mAdapter.getItemIndexByValue(Integer.valueOf(CameraSettings.readExposure(CameraSettingPreferences.instance())));
                    if (itemIndexByValue < 0) {
                        this.mCurrentItem = this.mAdapter.getMaxItem() / 2;
                    } else {
                        this.mCurrentItem = itemIndexByValue;
                    }
                    updateEV();
                }
            }
        }
    }

    public void onCreate() {
    }

    protected void onDraw(Canvas canvas) {
        if (this.mIsDraw) {
            if (this.mRotation != 0) {
                canvas.save();
                canvas.translate((float) this.mCenterX, (float) this.mCenterY);
                canvas.rotate((float) (-this.mRotation));
                canvas.translate((float) (-this.mCenterX), (float) (-this.mCenterY));
            }
            this.mBigPaint.setColor(Color.argb(this.mBigAlpha, 255, 255, 255));
            canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, (float) this.mBigRadius, this.mBigPaint);
            drawCenterIndicator(canvas);
            drawCursor(canvas);
            if (CameraSettings.isSupportedPortrait()) {
                drawEvText(canvas);
            }
            if (this.mRotation != 0) {
                canvas.restore();
            }
        }
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            this.mWidth = i3 - i;
            this.mHeight = i4 - i;
            this.mCenterX = this.mWidth / 2;
            this.mCenterY = this.mHeight / 2;
        }
        this.mRelativeLocation = Util.getRelativeLocation(this.mActivity.getUIController().getGLView(), this);
    }

    public void onPause() {
        setDraw(false);
        CameraSettings.resetExposure();
    }

    public void onResume() {
        setDraw(false);
        this.mActivity.loadCameraSound(6);
    }

    public boolean onViewTouchEvent(MotionEvent motionEvent) {
        boolean z = true;
        if (this.mAdapter == null || !this.mIsTouchFocus || (this.mState != 2 && !isStableStart())) {
            return false;
        }
        this.mGestureDetector.onTouchEvent(motionEvent);
        boolean z2 = this.mIsDown;
        if (motionEvent.getActionMasked() == 5 && this.mIsDown) {
            this.mIsDown = false;
            performSlideBack();
        }
        if (1 == motionEvent.getAction() || 3 == motionEvent.getAction()) {
            if (this.mEvAdjusted) {
                CameraDataAnalytics.instance().trackEvent("pref_camera_exposure_key");
                stopEvAdjust();
            }
            if (this.mCaptured) {
                this.mCaptured = false;
            }
            if (this.mIsDraw) {
                this.mIsDown = false;
                performSlideBack();
            }
        }
        if (!z2) {
            z = this.mIsDown;
        }
        return z;
    }

    public void setFocusType(boolean z) {
        this.mIsTouchFocus = z;
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setOrientation(int i, boolean z) {
        if (this.mRotation != i) {
            this.mRotation = i;
            if (this.mIsDraw) {
                invalidate();
            }
        }
    }

    public void setPosition(int i, int i2) {
        this.mCenterX = i;
        this.mCenterY = i2;
        removeMessages();
    }

    public void showFail() {
        if (this.mState == 1) {
            boolean hasMessages = this.mHandler.hasMessages(1);
            clearMessages();
            setDraw(true);
            this.mState = 3;
            if (hasMessages) {
                this.mFailTime = SystemClock.uptimeMillis();
                this.mHandler.sendEmptyMessageDelayed(3, 50);
                this.mHandler.sendEmptyMessageDelayed(5, 800);
                invalidate();
            } else {
                this.mFailTime = SystemClock.uptimeMillis();
                this.mHandler.sendEmptyMessageDelayed(3, 50);
                this.mHandler.sendEmptyMessageDelayed(5, 800);
                invalidate();
            }
        }
    }

    public void showStart() {
        clearMessages();
        this.mState = 1;
        this.mCursorState = 1;
        this.mSmallRadius = SMALL_RADIUS;
        this.mSmallAlpha = 180;
        this.mSmallLineWidth = SMALL_LINE_WIDTH;
        this.mStartTime = SystemClock.uptimeMillis();
        setDraw(true);
        if (isStableStart()) {
            this.mCenterFlag = 1;
            this.mHandler.sendEmptyMessage(1);
        } else {
            this.mBigRadius = BIG_INIT_RADIUS;
            this.mBigAlpha = 0;
            this.mEvTriangleDis = 0;
            this.mEVCaptureRatio = -1.0f;
            this.mCenterFlag = 0;
            processParameterIfNeeded(0.0f);
            this.mHandler.sendEmptyMessage(1);
            this.mHandler.sendEmptyMessageDelayed(4, 3000);
        }
        invalidate();
    }

    public void showSuccess() {
        if (this.mState == 1) {
            boolean hasMessages = this.mHandler.hasMessages(1);
            clearMessages();
            setDraw(true);
            this.mState = 2;
            if (hasMessages) {
                this.mSuccessTime = SystemClock.uptimeMillis();
                this.mHandler.sendEmptyMessageDelayed(2, 50);
            } else {
                this.mSuccessTime = SystemClock.uptimeMillis();
                this.mHandler.sendEmptyMessageDelayed(2, 50);
            }
            if (!this.mIsTouchFocus) {
                this.mHandler.sendEmptyMessageDelayed(5, 800);
            }
            invalidate();
        }
    }
}
