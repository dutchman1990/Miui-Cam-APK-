package com.android.camera.ui;

import android.graphics.Point;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.ViewConfiguration;
import com.android.camera.ActivityBase;
import com.android.camera.Camera;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Log;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.ui.EdgeGestureDetector.EdgeGestureListener;

public class V6GestureRecognizer {
    public static final int GESTURE_DETECT_DISTANCE = Util.dpToPixel(50.0f);
    public static final int SWITCH_CAMERA_IGNORE_DISTANCE = Util.dpToPixel(30.0f);
    private static V6GestureRecognizer sV6GestureRecognizer;
    private final Camera mActivity;
    private final CameraGestureDetector mCameraGestureDetector;
    private int mEdgeGesture = 0;
    private final EdgeGestureDetector mEdgeGestureDetector;
    private int mGesture = 0;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private boolean mTouchDown;

    private class CameraGestureDetector {
        private final int MIN_DETECT_DISTANCE = (ViewConfiguration.get(V6GestureRecognizer.this.mActivity).getScaledTouchSlop() * ViewConfiguration.get(V6GestureRecognizer.this.mActivity).getScaledTouchSlop());
        private Point mStartPoint = new Point();

        public void onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & 255) {
                case 0:
                    this.mStartPoint.set((int) motionEvent.getX(), (int) motionEvent.getY());
                    return;
                case 2:
                    Log.m5v("Camera12", "CameraGestureDetector ACTION_MOVE mGesture=" + V6GestureRecognizer.this.mGesture);
                    if (V6GestureRecognizer.this.mGesture / 100 == 0) {
                        Point -wrap0 = V6GestureRecognizer.this.getMoveVector(this.mStartPoint.x, this.mStartPoint.y, (int) motionEvent.getX(), (int) motionEvent.getY());
                        Log.m5v("CameraGestureRecognizer", "mGesture=" + V6GestureRecognizer.this.mGesture + " orientation=" + (Math.abs(-wrap0.x) > Math.abs(-wrap0.y) ? "h" : "v") + " dx=" + -wrap0.x + " dy=" + -wrap0.y);
                        if (this.MIN_DETECT_DISTANCE <= (-wrap0.x * -wrap0.x) + (-wrap0.y * -wrap0.y)) {
                            V6GestureRecognizer v6GestureRecognizer = V6GestureRecognizer.this;
                            v6GestureRecognizer.mGesture = (Math.abs(-wrap0.x) > Math.abs(-wrap0.y) ? 100 : 200) + v6GestureRecognizer.mGesture;
                        }
                    }
                    Log.m5v("Camera12", "CameraGestureDetector ACTION_MOVE end mGesture=" + V6GestureRecognizer.this.mGesture);
                    return;
                case 6:
                    if (motionEvent.getPointerCount() == 2 && V6GestureRecognizer.this.couldNotifyGesture(false) && V6GestureRecognizer.this.getUIController().getPreviewPage().isPreviewPageVisible()) {
                        float x;
                        float x2;
                        float y;
                        float y2;
                        if (motionEvent.getX(0) < motionEvent.getX(1)) {
                            x = motionEvent.getX(0);
                            x2 = motionEvent.getX(1);
                        } else {
                            x = motionEvent.getX(1);
                            x2 = motionEvent.getX(0);
                        }
                        if (motionEvent.getY(0) < motionEvent.getY(1)) {
                            y = motionEvent.getY(0);
                            y2 = motionEvent.getY(1);
                        } else {
                            y = motionEvent.getY(1);
                            y2 = motionEvent.getY(0);
                        }
                        if (V6GestureRecognizer.this.couldNotifyGesture(false) && V6GestureRecognizer.this.getUIController().getPreviewPage().isPreviewPageVisible()) {
                            V6GestureRecognizer v6GestureRecognizer2 = V6GestureRecognizer.this;
                            v6GestureRecognizer2.mGesture = v6GestureRecognizer2.mGesture + 10;
                            V6GestureRecognizer.this.mActivity.getCurrentModule().onGestureTrack(new RectF(x, y, x2, y2), true);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class MyEdgeGestureListener implements EdgeGestureListener {
        private MyEdgeGestureListener() {
        }
    }

    private class MyGestureListener extends SimpleOnGestureListener {
        private boolean mHandleConfirmTap;

        private MyGestureListener() {
        }

        private boolean handleSingleTap(MotionEvent motionEvent) {
            if (!V6GestureRecognizer.this.couldNotifyGesture(false) || !V6GestureRecognizer.this.getUIController().getPreviewPage().isPreviewPageVisible()) {
                return false;
            }
            V6GestureRecognizer.this.getUIController().getTopPopupParent().dismissAllPopupExceptSkinBeauty(true);
            V6GestureRecognizer.this.mActivity.getCurrentModule().onSingleTapUp((int) motionEvent.getX(), (int) motionEvent.getY());
            return true;
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            int i = 0;
            if (!this.mHandleConfirmTap) {
                return false;
            }
            V6GestureRecognizer.this.getUIController().getTopPopupParent().dismissAllPopupExceptSkinBeauty(true);
            int invertFlag = EffectController.getInstance().getInvertFlag();
            EffectController instance = EffectController.getInstance();
            if (invertFlag == 0) {
                i = 1;
            }
            instance.setInvertFlag(i);
            return true;
        }

        public void onLongPress(MotionEvent motionEvent) {
            Log.m5v("CameraGestureRecognizer", "onLongPress");
            if (V6GestureRecognizer.this.couldNotifyGesture(false) && V6GestureRecognizer.this.getUIController().getPreviewPage().isPreviewPageVisible()) {
                V6GestureRecognizer.this.getUIController().getTopPopupParent().dismissAllPopupExceptSkinBeauty(true);
                V6GestureRecognizer.this.mActivity.getCurrentModule().onLongPress((int) motionEvent.getX(), (int) motionEvent.getY());
            }
        }

        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return (V6GestureRecognizer.this.getUIController().getEffectCropView().isVisible() && this.mHandleConfirmTap) ? handleSingleTap(motionEvent) : false;
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            Log.m5v("CameraGestureRecognizer", "onSingleTapUp");
            if (!V6GestureRecognizer.this.getUIController().getEffectCropView().isVisible()) {
                return handleSingleTap(motionEvent);
            }
            this.mHandleConfirmTap = V6GestureRecognizer.this.couldNotifyGesture(false) ? V6GestureRecognizer.this.getUIController().getPreviewPage().isPreviewPageVisible() : false;
            return false;
        }
    }

    private class MyScaleListener extends SimpleOnScaleGestureListener {
        private MyScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if ((!V6GestureRecognizer.this.isGestureDetecting() && V6GestureRecognizer.this.getCurrentGesture() != 9) || !V6GestureRecognizer.this.getUIController().getPreviewPage().isPreviewPageVisible()) {
                return false;
            }
            CameraDataAnalytics.instance().trackEvent("zoom_gesture_times");
            V6GestureRecognizer.this.setGesture(9);
            return V6GestureRecognizer.this.mActivity.getCurrentModule().onScale(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY(), scaleGestureDetector.getScaleFactor());
        }

        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return V6GestureRecognizer.this.mActivity.getCurrentModule().onScaleBegin(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
        }

        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            V6GestureRecognizer.this.mActivity.getCurrentModule().onScaleEnd();
        }
    }

    private V6GestureRecognizer(ActivityBase activityBase) {
        this.mActivity = (Camera) activityBase;
        this.mGestureDetector = new GestureDetector(activityBase, new MyGestureListener(), null, true);
        this.mEdgeGestureDetector = new EdgeGestureDetector(new MyEdgeGestureListener());
        this.mScaleDetector = new ScaleGestureDetector(activityBase, new MyScaleListener());
        this.mCameraGestureDetector = new CameraGestureDetector();
    }

    private boolean checkControlView(MotionEvent motionEvent) {
        if (getUIController().getPreviewPage().isPreviewPageVisible()) {
            V6EffectCropView effectCropView = getUIController().getEffectCropView();
            if (effectCropView.isVisible()) {
                effectCropView.onViewTouchEvent(motionEvent);
                if (effectCropView.isMoved()) {
                    if (isGestureDetecting()) {
                        this.mGesture += 6;
                    }
                } else if (!effectCropView.isMoved() && getCurrentGesture() == 6) {
                    setGesture(0);
                }
            }
            FocusView focusView = getUIController().getFocusView();
            boolean isEvAdjusted = focusView.isEvAdjusted();
            if (focusView.isVisible()) {
                focusView.onViewTouchEvent(motionEvent);
                if (focusView.isEvAdjusted()) {
                    if (isGestureDetecting()) {
                        this.mGesture += 7;
                    }
                } else if (!isEvAdjusted && getCurrentGesture() == 7) {
                    setGesture(0);
                }
            }
        }
        return !isGestureDetecting();
    }

    private boolean couldNotifyGesture(boolean z) {
        return isGestureDetecting(z) && !this.mActivity.getCurrentModule().IsIgnoreTouchEvent();
    }

    public static synchronized V6GestureRecognizer getInstance(ActivityBase activityBase) {
        V6GestureRecognizer v6GestureRecognizer;
        synchronized (V6GestureRecognizer.class) {
            if (sV6GestureRecognizer == null || activityBase != sV6GestureRecognizer.mActivity) {
                sV6GestureRecognizer = new V6GestureRecognizer(activityBase);
            }
            v6GestureRecognizer = sV6GestureRecognizer;
        }
        return v6GestureRecognizer;
    }

    private Point getMoveVector(int i, int i2, int i3, int i4) {
        Point point = new Point();
        point.x = i - i3;
        point.y = i2 - i4;
        return point;
    }

    private UIController getUIController() {
        return this.mActivity.getUIController();
    }

    private boolean isGestureDetecting(boolean z) {
        return (z ? this.mEdgeGesture : this.mGesture) % 100 == 0;
    }

    public static void onDestory(ActivityBase activityBase) {
        if (sV6GestureRecognizer != null && sV6GestureRecognizer.mActivity == activityBase) {
            sV6GestureRecognizer = null;
        }
    }

    public int getCurrentGesture() {
        return this.mGesture % 100;
    }

    public int getGestureOrientation() {
        return (this.mGesture / 100) * 100;
    }

    public boolean isGestureDetecting() {
        return this.mGesture % 100 == 0;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        Log.m5v("CameraGestureRecognizer", "onTouchEvent mGesture=" + this.mGesture + " action=" + motionEvent.getAction());
        if (motionEvent.getActionMasked() == 0) {
            this.mGesture = 0;
        }
        if (this.mActivity.getCurrentModule().IsIgnoreTouchEvent() && motionEvent.getAction() != 1 && motionEvent.getAction() != 3) {
            return false;
        }
        if (motionEvent.getActionMasked() == 0) {
            this.mTouchDown = true;
        } else if (!this.mTouchDown) {
            return false;
        } else {
            if (motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1) {
                this.mTouchDown = false;
            }
        }
        checkControlView(motionEvent);
        Log.m5v("CameraGestureRecognizer", "set to detector");
        this.mCameraGestureDetector.onTouchEvent(motionEvent);
        this.mGestureDetector.onTouchEvent(motionEvent);
        this.mScaleDetector.onTouchEvent(motionEvent);
        boolean z = !isGestureDetecting();
        if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
            this.mGesture = 0;
        }
        return z;
    }

    public void setGesture(int i) {
        this.mGesture = ((this.mGesture / 100) * 100) + i;
    }
}
