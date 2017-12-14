package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.ActivityBase;
import com.android.camera.Camera;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Util;
import com.android.camera.hardware.CameraHardwareProxy.CameraHardwareFace;

public class ObjectView extends FrameView {
    private static final int NEAR_EDGE = Util.dpToPixel(35.0f);
    private static final int[] OBJECT_TRACKING_ICON = new int[]{C0049R.drawable.ic_object_tracking, C0049R.drawable.ic_object_tracking_succeed};
    private static final int RECT_EDGE_WIDTH = Util.dpToPixel(10.0f);
    private static final int TOUCH_TOLERANCE = Util.dpToPixel(25.0f);
    private static final int VERTEXS_TOUCH_TOLERANCE = Util.dpToPixel(35.0f);
    private final RectF mDisplayBounds = new RectF();
    private Runnable mEndAction = new EndAction();
    protected CameraHardwareFace mFace;
    private Matrix mFace2UIMatrix = new Matrix();
    private Filter mFilter = new Filter();
    private Handler mHandler = new C01401();
    private float mLastX;
    private float mLastY;
    private ObjectViewListener mListener;
    private int mLostTrackThreshold;
    private int mLostTrackingNum = 0;
    private int mMovingMode;
    protected RectF mObjectRect = new RectF();
    protected RectF mOldObjectRect = new RectF();
    private int mPreviewHeight;
    private int mPreviewWidth;
    private Runnable mStartAction = new StartAction();
    protected Drawable mTrackIndicator;
    private Drawable[] mTrackStatusIndicator = new Drawable[OBJECT_TRACKING_ICON.length];
    private int mZoomAnimaState = 0;

    public interface ObjectViewListener {
        void onObjectStable();

        void startObjectTracking();

        void stopObjectTracking(boolean z);
    }

    class C01401 extends Handler {
        C01401() {
        }

        public void dispatchMessage(Message message) {
            switch (message.what) {
                case 1:
                    ObjectView.this.showStart();
                    if (!(ObjectView.this.mListener == null || ObjectView.this.getFocusRect() == null)) {
                        ObjectView.this.mListener.startObjectTracking();
                        ObjectView.this.mFilter.resetState();
                    }
                    sendEmptyMessageDelayed(2, 1000);
                    return;
                case 2:
                    if (ObjectView.this.mListener != null) {
                        ObjectView.this.mListener.stopObjectTracking(true);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class EndAction implements Runnable {
        private EndAction() {
        }

        public void run() {
            ObjectView.this.mZoomAnimaState = 5;
        }
    }

    private class Filter {
        int mCurrentValue;
        int mHoldTimes;
        int mHoldValue;

        private Filter() {
            this.mCurrentValue = 0;
            this.mHoldValue = -1;
        }

        private int filter(int i) {
            if (i != this.mHoldValue) {
                this.mHoldValue = i;
                this.mHoldTimes = 1;
            } else {
                if (this.mHoldTimes < 4) {
                    this.mHoldTimes++;
                }
                if (this.mHoldTimes == 4 && this.mHoldValue != this.mCurrentValue) {
                    this.mCurrentValue = this.mHoldValue;
                }
            }
            return this.mCurrentValue;
        }

        public boolean isBecomeStable(int i) {
            return this.mCurrentValue == 0 && filter(i) == 1;
        }

        public void resetState() {
            this.mCurrentValue = 0;
            this.mHoldValue = -1;
        }
    }

    private class StartAction implements Runnable {
        private StartAction() {
        }

        public void run() {
            ObjectView.this.mZoomAnimaState = 3;
        }
    }

    public ObjectView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        for (int i = 0; i < OBJECT_TRACKING_ICON.length; i++) {
            this.mTrackStatusIndicator[i] = this.mContext.getResources().getDrawable(OBJECT_TRACKING_ICON[i]);
        }
        this.mTrackIndicator = this.mTrackStatusIndicator[0];
        this.mContext = (Camera) context;
    }

    private void detectMovingStyle(float f, float f2, float f3, float f4) {
        int i = 1;
        if (f3 < 0.0f || f4 < 0.0f) {
            this.mMovingMode = 0;
            float abs = Math.abs(f - this.mObjectRect.left);
            float abs2 = Math.abs(f - this.mObjectRect.right);
            if (abs <= ((float) TOUCH_TOLERANCE) && abs < abs2) {
                this.mMovingMode |= 1;
            } else if (abs2 <= ((float) TOUCH_TOLERANCE)) {
                this.mMovingMode |= 4;
            }
            float abs3 = Math.abs(f2 - this.mObjectRect.top);
            float abs4 = Math.abs(f2 - this.mObjectRect.bottom);
            int i2 = abs3 <= ((float) TOUCH_TOLERANCE) ? 1 : 0;
            if (abs3 >= abs4) {
                i = 0;
            }
            if ((i & i2) != 0) {
                this.mMovingMode |= 2;
            } else if (abs4 <= ((float) TOUCH_TOLERANCE)) {
                this.mMovingMode |= 8;
            }
            if (this.mObjectRect.contains(f, f2) && this.mMovingMode == 0) {
                this.mMovingMode = 16;
            }
        } else if (Math.abs(this.mObjectRect.left - Math.min(f, f3)) <= ((float) VERTEXS_TOUCH_TOLERANCE) && Math.abs(this.mObjectRect.right - Math.max(f, f3)) <= ((float) VERTEXS_TOUCH_TOLERANCE) && Math.abs(this.mObjectRect.top - Math.min(f2, f4)) <= ((float) VERTEXS_TOUCH_TOLERANCE) && Math.abs(this.mObjectRect.bottom - Math.max(f2, f4)) <= ((float) VERTEXS_TOUCH_TOLERANCE)) {
            this.mMovingMode = 32;
        }
        invalidate();
    }

    private boolean isInObjectViewArea(float f, float f2) {
        return f >= this.mObjectRect.left - ((float) RECT_EDGE_WIDTH) && f < this.mObjectRect.right + ((float) RECT_EDGE_WIDTH) && f2 >= this.mObjectRect.top - ((float) RECT_EDGE_WIDTH) && f2 < this.mObjectRect.bottom + ((float) RECT_EDGE_WIDTH);
    }

    private RectF mapRect(RectF rectF, boolean z) {
        RectF rectF2 = new RectF();
        rectF2.set(rectF);
        if (z) {
            this.mMatrix.mapRect(rectF2);
        } else {
            this.mFace2UIMatrix.mapRect(rectF2);
        }
        return rectF2;
    }

    private void moveObjectView(MotionEvent motionEvent) {
        if (this.mMovingMode == 32) {
            float x = motionEvent.getX(0);
            float y = motionEvent.getY(0);
            float x2 = motionEvent.getX(1);
            float y2 = motionEvent.getY(1);
            if (200.0f < Math.abs(x2 - x)) {
                this.mObjectRect.left = Math.min(x, x2);
                this.mObjectRect.right = Math.max(x, x2);
            }
            if (200.0f < Math.abs(y2 - y)) {
                this.mObjectRect.top = Math.min(y, y2);
                this.mObjectRect.bottom = Math.max(y, y2);
            }
        } else {
            float x3 = motionEvent.getX() - this.mLastX;
            float y3 = motionEvent.getY() - this.mLastY;
            if (this.mMovingMode == 16) {
                this.mObjectRect.offset(x3 > 0.0f ? Math.min(this.mDisplayBounds.right - this.mObjectRect.right, x3) : Math.max(this.mDisplayBounds.left - this.mObjectRect.left, x3), y3 > 0.0f ? Math.min(this.mDisplayBounds.bottom - this.mObjectRect.bottom, y3) : Math.max(this.mDisplayBounds.top - this.mObjectRect.top, y3));
            } else {
                if ((this.mMovingMode & 1) != 0) {
                    this.mObjectRect.left = Math.min(this.mObjectRect.left + x3, this.mObjectRect.right - 200.0f);
                }
                if ((this.mMovingMode & 2) != 0) {
                    this.mObjectRect.top = Math.min(this.mObjectRect.top + y3, this.mObjectRect.bottom - 200.0f);
                }
                if ((this.mMovingMode & 4) != 0) {
                    this.mObjectRect.right = Math.max(this.mObjectRect.right + x3, this.mObjectRect.left + 200.0f);
                }
                if ((this.mMovingMode & 8) != 0) {
                    this.mObjectRect.bottom = Math.max(this.mObjectRect.bottom + y3, this.mObjectRect.top + 200.0f);
                }
                this.mObjectRect.intersect(this.mDisplayBounds);
            }
        }
        invalidate();
    }

    private void resetView() {
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        setBackground(null);
        animate().cancel();
        setScaleX(1.0f);
        setScaleY(1.0f);
        layoutParams.width = (int) this.mDisplayBounds.width();
        layoutParams.height = (int) this.mDisplayBounds.height();
        layoutParams.setMargins(0, 0, 0, 0);
        this.mTrackIndicator = this.mTrackStatusIndicator[0];
    }

    private void setMatrix() {
        if (this.mPreviewWidth != 0 && this.mPreviewHeight != 0 && this.mDisplayBounds.width() != 0.0f && this.mDisplayBounds.height() != 0.0f) {
            this.mMatrix.reset();
            this.mMatrix.postScale(((float) this.mPreviewWidth) / this.mDisplayBounds.width(), ((float) this.mPreviewHeight) / this.mDisplayBounds.height());
            this.mFace2UIMatrix.reset();
            this.mMatrix.invert(this.mFace2UIMatrix);
        }
    }

    private void updateAnimateView() {
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        layoutParams.width = (int) this.mObjectRect.width();
        layoutParams.height = (int) this.mObjectRect.height();
        layoutParams.setMargins((int) this.mObjectRect.left, (int) this.mObjectRect.top, 0, 0);
        requestLayout();
    }

    public void clear() {
        this.mFace = null;
        this.mOldObjectRect.set(this.mObjectRect);
        this.mObjectRect.set(0.0f, 0.0f, 0.0f, 0.0f);
        resetView();
        this.mZoomAnimaState = 0;
        invalidate();
    }

    public boolean faceExists() {
        return this.mFace == null ? isAdjusting() : true;
    }

    public RectF getFocusRect() {
        return (((ActivityBase) getContext()).getCameraScreenNail() == null || this.mPause || this.mObjectRect.width() <= 0.0f || this.mObjectRect.height() <= 0.0f) ? null : this.mObjectRect;
    }

    public RectF getFocusRectInPreviewFrame() {
        RectF focusRect = getFocusRect();
        return focusRect != null ? mapRect(focusRect, true) : null;
    }

    public boolean initializeTrackView(RectF rectF, boolean z) {
        if (rectF != null) {
            CameraDataAnalytics.instance().trackEvent("t2t_times");
            rectF.intersect(this.mDisplayBounds);
            if (rectF.width() < 200.0f || rectF.height() < 200.0f) {
                return false;
            }
            this.mObjectRect.set(rectF);
            this.mOldObjectRect.set(rectF);
        } else {
            this.mObjectRect.set(this.mOldObjectRect);
        }
        setVisibility(0);
        resume();
        requestLayout();
        if (z) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 0);
        }
        return true;
    }

    public boolean isAdjusting() {
        return this.mMovingMode == 0 ? (this.mObjectRect.width() <= 0.0f || this.mObjectRect.height() <= 0.0f) ? false : this.mZoomAnimaState == 0 : true;
    }

    public boolean isTrackFailed() {
        return this.mFace == null || 1 > this.mFace.score;
    }

    protected void onDraw(Canvas canvas) {
        if (Util.sIsDumpLog) {
            Log.v("ObjectView", "onDraw(), mZoomAnimaState:" + this.mZoomAnimaState + ", mPause=" + this.mPause + " mObjectRect=" + this.mObjectRect + " mFace=" + this.mFace + ", mTrackIndicator=" + this.mTrackIndicator + ", getWidth=" + getWidth() + ", getheight=" + getHeight() + ", mDisplayBounds=" + this.mDisplayBounds);
        }
        if (this.mPause || (this.mZoomAnimaState != 0 && (this.mZoomAnimaState != 5 || this.mFace == null))) {
            super.onDraw(canvas);
            return;
        }
        canvas.save();
        canvas.translate(this.mObjectRect.centerX(), this.mObjectRect.centerY());
        this.mTrackIndicator.setBounds(Math.round((-(this.mObjectRect.right - this.mObjectRect.left)) / 2.0f), Math.round((-(this.mObjectRect.bottom - this.mObjectRect.top)) / 2.0f), Math.round((this.mObjectRect.right - this.mObjectRect.left) / 2.0f), Math.round((this.mObjectRect.bottom - this.mObjectRect.top) / 2.0f));
        this.mTrackIndicator.draw(canvas);
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (this.mPause || ((this.mMovingMode == 0 && !isInObjectViewArea(x, y)) || this.mLostTrackingNum != 0)) {
            return false;
        }
        if (isEnabled()) {
            switch (motionEvent.getActionMasked()) {
                case 0:
                    if (!this.mHandler.hasMessages(1)) {
                        if (this.mListener != null) {
                            if (this.mZoomAnimaState == 4 || this.mZoomAnimaState == 2) {
                                clearAnimation();
                            }
                            this.mZoomAnimaState = 0;
                            this.mListener.stopObjectTracking(false);
                        }
                        initializeTrackView(null, false);
                    }
                    this.mHandler.removeMessages(1);
                    detectMovingStyle(x, y, -1.0f, -1.0f);
                    this.mLastX = x;
                    this.mLastY = y;
                    break;
                case 1:
                case 3:
                    this.mMovingMode = 0;
                    invalidate();
                    this.mHandler.sendEmptyMessageDelayed(1, 0);
                    break;
                case 2:
                    if (this.mMovingMode != 0) {
                        moveObjectView(motionEvent);
                    }
                    this.mLastX = x;
                    this.mLastY = y;
                    break;
                case 5:
                    if (motionEvent.getPointerCount() == 2) {
                        detectMovingStyle(motionEvent.getX(0), motionEvent.getY(0), motionEvent.getX(1), motionEvent.getY(1));
                        if (this.mMovingMode == 32 && this.mListener != null) {
                            if (this.mZoomAnimaState == 4 || this.mZoomAnimaState == 2) {
                                clearAnimation();
                            }
                            this.mZoomAnimaState = 0;
                            this.mListener.stopObjectTracking(false);
                            moveObjectView(motionEvent);
                            break;
                        }
                    }
                    break;
                case 6:
                    if (motionEvent.getActionIndex() < 2 && this.mMovingMode == 32) {
                        this.mLastX = motionEvent.getX(1 - motionEvent.getActionIndex());
                        this.mLastY = motionEvent.getY(1 - motionEvent.getActionIndex());
                        detectMovingStyle(this.mLastX, this.mLastY, -1.0f, -1.0f);
                        break;
                    }
            }
        }
        return true;
    }

    public void setDisplaySize(int i, int i2) {
        this.mDisplayBounds.set(0.0f, 0.0f, (float) i, (float) i2);
        setMatrix();
    }

    public void setObject(CameraHardwareFace cameraHardwareFace) {
        if (Util.sIsDumpLog) {
            Log.i("ObjectView", "setObject(), mZoomAnimaState:" + this.mZoomAnimaState + " , face.rect:" + cameraHardwareFace.rect + " , face.score:" + cameraHardwareFace.score + " , face.t2tStop:" + cameraHardwareFace.t2tStop + " , moving=" + this.mFilter.mCurrentValue + " , mPause=" + this.mPause + " , visible=" + getVisibility() + " , getWidth()=" + getWidth() + " ,mDisplayBounds.width()=" + this.mDisplayBounds.width() + " , getHeight()=" + getHeight() + " ,mDisplayBounds.height()=" + this.mDisplayBounds.height());
        }
        if (this.mZoomAnimaState > 2) {
            if (this.mZoomAnimaState == 3) {
                if (cameraHardwareFace.score >= 1) {
                    this.mHandler.removeMessages(2);
                    showSuccess();
                }
            } else if (this.mZoomAnimaState == 5 && !this.mPause) {
                if (cameraHardwareFace.score < 1) {
                    if (this.mLostTrackThreshold == 0) {
                        if (this.mObjectRect.left - this.mDisplayBounds.left < ((float) NEAR_EDGE) || this.mObjectRect.top - this.mDisplayBounds.top < ((float) NEAR_EDGE) || this.mDisplayBounds.right - this.mObjectRect.right < ((float) NEAR_EDGE) || this.mDisplayBounds.bottom - this.mObjectRect.bottom < ((float) NEAR_EDGE)) {
                            this.mLostTrackThreshold = 150;
                        } else {
                            this.mLostTrackThreshold = 50;
                        }
                    }
                    this.mLostTrackingNum++;
                    if (this.mLostTrackingNum % 5 == 0 || this.mLostTrackingNum == this.mLostTrackThreshold) {
                        Log.v("ObjectView", "lost " + this.mLostTrackingNum + " times");
                    }
                    if (this.mLostTrackThreshold <= this.mLostTrackingNum) {
                        this.mListener.stopObjectTracking(true);
                    }
                } else {
                    this.mLostTrackThreshold = 0;
                    this.mLostTrackingNum = 0;
                }
                boolean z = ((double) cameraHardwareFace.rect.width()) / ((double) this.mPreviewWidth) <= 0.33d ? ((double) cameraHardwareFace.rect.height()) / ((double) this.mPreviewHeight) > 0.33d : true;
                this.mIsBigEnoughRect = z;
                if (((float) getWidth()) != this.mDisplayBounds.width() || ((float) getHeight()) != this.mDisplayBounds.height()) {
                    resetView();
                } else if (cameraHardwareFace == null || cameraHardwareFace.score < 1) {
                    this.mFace = null;
                } else {
                    this.mFace = cameraHardwareFace;
                    this.mTrackIndicator = this.mTrackStatusIndicator[1];
                    this.mObjectRect.set(mapRect(new RectF(this.mFace.rect), false));
                    this.mOldObjectRect.set(this.mObjectRect);
                    if (this.mFilter.isBecomeStable(cameraHardwareFace.t2tStop)) {
                        this.mListener.onObjectStable();
                    }
                }
                invalidate();
            }
        }
    }

    public void setObjectViewListener(ObjectViewListener objectViewListener) {
        this.mListener = objectViewListener;
    }

    public void setPreviewSize(int i, int i2) {
        this.mPreviewWidth = i;
        this.mPreviewHeight = i2;
        setMatrix();
    }

    public void showFail() {
    }

    public void showStart() {
        Log.i("ObjectView", "showStart()");
        if (this.mZoomAnimaState == 0) {
            this.mZoomAnimaState = 1;
            this.mTrackIndicator = this.mTrackStatusIndicator[0];
            updateAnimateView();
            setBackground(this.mTrackIndicator);
            animate().withLayer().setDuration(150).scaleX(1.2f).scaleY(1.2f).withEndAction(this.mStartAction);
        }
    }

    public void showSuccess() {
        Log.i("ObjectView", "showSuccess()");
        if (this.mZoomAnimaState == 3) {
            this.mZoomAnimaState = 4;
            this.mTrackIndicator = this.mTrackStatusIndicator[1];
            updateAnimateView();
            setBackground(this.mTrackIndicator);
            animate().withLayer().setDuration(80).scaleX(0.7f).scaleY(0.7f).withEndAction(this.mEndAction);
        }
    }
}
