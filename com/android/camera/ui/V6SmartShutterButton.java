package com.android.camera.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import com.android.camera.CameraSettings;
import com.android.camera.Util;

public class V6SmartShutterButton extends RotateImageView implements V6FunctionUI {
    private static final int DISMISS_DISTANCE_THRESHOLD = Util.dpToPixel(70.0f);
    private static final int FADEOUT_BOUNT_THRESHOLD = Util.dpToPixel(10.0f);
    private static final int MOVE_THRESHOLD = Util.dpToPixel(30.0f);
    private static int UNUSED_TRIGGER_TIME = 15000;
    private static double sDeviceScreenInches;
    private Animation mFadeout;
    private int mFixedShutterCenterX;
    private int mFixedShutterCenterY;
    private Handler mHandler = new C01741();
    private boolean mInShutterButton;
    private MessageDispacher mMessageDispacher;
    private Rect mMoveBount;
    private int mOriginX;
    private int mOriginY;
    private int mState = 0;
    private Rect mVisableBount;
    private int mVisibleState;

    class C01741 extends Handler {
        C01741() {
        }

        public void dispatchMessage(Message message) {
            switch (message.what) {
                case 0:
                    V6SmartShutterButton.this.mVisibleState = 1;
                    V6SmartShutterButton.this.updateVisibleState();
                    return;
                default:
                    return;
            }
        }
    }

    public V6SmartShutterButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mVisableBount = new Rect(0, getResources().getDimensionPixelSize(C0049R.dimen.camera_control_top_height), displayMetrics.widthPixels, displayMetrics.heightPixels - getResources().getDimensionPixelSize(C0049R.dimen.camera_control_bottom_height));
        this.mMoveBount = new Rect(this.mVisableBount.left - FADEOUT_BOUNT_THRESHOLD, this.mVisableBount.top - FADEOUT_BOUNT_THRESHOLD, this.mVisableBount.right + FADEOUT_BOUNT_THRESHOLD, this.mVisableBount.bottom + FADEOUT_BOUNT_THRESHOLD);
    }

    private int getDistanceFromPoint(int i, int i2, int i3, int i4) {
        int abs = Math.abs(i3 - i);
        int abs2 = Math.abs(i4 - i2);
        return (int) Math.sqrt((double) ((abs * abs) + (abs2 * abs2)));
    }

    private boolean nearCenterOfShutter(int i, int i2) {
        int abs = Math.abs(this.mFixedShutterCenterX - i);
        int abs2 = Math.abs(this.mFixedShutterCenterY - i2);
        return abs <= DISMISS_DISTANCE_THRESHOLD && abs2 <= DISMISS_DISTANCE_THRESHOLD && Math.sqrt((double) ((abs * abs) + (abs2 * abs2))) < ((double) DISMISS_DISTANCE_THRESHOLD);
    }

    private boolean needMovableShutter() {
        if (sDeviceScreenInches == 0.0d) {
            sDeviceScreenInches = Util.getScreenInches(getContext());
        }
        return sDeviceScreenInches > 4.9d;
    }

    private void onClick() {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_shutter_button, 2, null, null);
        }
    }

    private void onFocused(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(3, C0049R.id.v6_shutter_button, 2, Boolean.valueOf(z), null);
        }
    }

    private Rect reviseLocation(int i, int i2, Rect rect) {
        Rect rect2 = new Rect(i - (getWidth() / 2), i2 - (getHeight() / 2), (getWidth() / 2) + i, (getHeight() / 2) + i2);
        if (rect.contains(rect2)) {
            return rect2;
        }
        if (rect2.left < rect.left) {
            rect2.right = rect.left + rect2.width();
            rect2.left = rect.left;
        } else if (rect2.right > rect.right) {
            rect2.left = rect.right - rect2.width();
            rect2.right = rect.right;
        }
        if (rect2.top < rect.top) {
            rect2.bottom = rect.top + rect2.height();
            rect2.top = rect.top;
        } else if (rect2.bottom > rect.bottom) {
            rect2.top = rect.bottom - rect2.height();
            rect2.bottom = rect.bottom;
        }
        return rect2;
    }

    private void setDisplayPosition(int i, int i2) {
        setX((float) i);
        setY((float) i2);
    }

    private void setRelateVisible(int i) {
        int i2 = 8;
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        if (relativeLayout != null) {
            if (8 != i) {
                i2 = 0;
            }
            relativeLayout.setVisibility(i2);
        }
        setVisibility(i);
    }

    private void updateVisibleState() {
        if (this.mFadeout == null) {
            this.mFadeout = new AlphaAnimation(1.0f, 0.01f);
            this.mFadeout.setStartOffset(500);
            this.mFadeout.setDuration(2000);
        }
        switch (this.mVisibleState) {
            case 0:
                this.mFadeout.cancel();
                setRelateVisible(0);
                return;
            case 1:
                setAnimation(this.mFadeout);
                this.mFadeout.start();
                setRelateVisible(4);
                return;
            case 2:
                clearAnimation();
                setAlpha(1.0f);
                setRelateVisible(4);
                return;
            default:
                return;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        int rawX = (int) motionEvent.getRawX();
        int rawY = (int) motionEvent.getRawY();
        switch (motionEvent.getAction()) {
            case 0:
                this.mOriginX = rawX;
                this.mOriginY = rawY;
                this.mState = 0;
                setPressed(true);
                onFocused(true);
                this.mHandler.removeMessages(0);
                break;
            case 1:
            case 3:
                setPressed(false);
                this.mHandler.sendEmptyMessageDelayed(0, (long) UNUSED_TRIGGER_TIME);
                this.mInShutterButton = Util.pointInView((float) rawX, (float) rawY, this);
                if (this.mInShutterButton && this.mState != 1) {
                    CameraSettings.setSmartShutterPosition(rawX + "x" + rawY);
                    onClick();
                }
                onFocused(false);
                updateVisibleState();
                break;
            case 2:
                Rect rect;
                if (this.mState != 1) {
                    if (MOVE_THRESHOLD <= getDistanceFromPoint(rawX, rawY, this.mOriginX, this.mOriginY)) {
                        this.mState = 1;
                    }
                }
                if (nearCenterOfShutter(rawX, rawY)) {
                    this.mVisibleState = 2;
                    setAlpha(0.6f);
                    rect = new Rect(this.mFixedShutterCenterX - (getWidth() / 2), this.mFixedShutterCenterY - (getHeight() / 2), this.mFixedShutterCenterX + (getWidth() / 2), this.mFixedShutterCenterY + (getHeight() / 2));
                } else {
                    setAlpha(1.0f);
                    rect = reviseLocation(rawX, rawY, this.mMoveBount);
                    if (this.mVisableBount.contains(rect)) {
                        if (this.mVisibleState == 1 && this.mFadeout != null) {
                            this.mFadeout.cancel();
                        }
                        this.mVisibleState = 0;
                    }
                }
                setDisplayPosition(rect.left, rect.top);
                break;
        }
        return true;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public void flyin(int i, int i2, int i3, int i4) {
        this.mFixedShutterCenterX = i3;
        this.mFixedShutterCenterY = i4;
        if (needMovableShutter() && !isShown()) {
            if (this.mFadeout != null) {
                this.mFadeout.cancel();
            }
            setRelateVisible(0);
            setAlpha(0.01f);
            setDisplayPosition(i3 - (getWidth() / 2), i4 - (getHeight() / 2));
            String smartShutterPosition = CameraSettings.getSmartShutterPosition();
            int i5 = -1;
            int i6 = -1;
            if (smartShutterPosition != null) {
                int indexOf = smartShutterPosition.indexOf(120);
                if (indexOf != -1) {
                    i5 = Integer.parseInt(smartShutterPosition.substring(0, indexOf));
                    i6 = Integer.parseInt(smartShutterPosition.substring(indexOf + 1));
                }
            }
            if (i5 == -1 && r4 == -1) {
                i5 = i;
                i6 = i2;
            }
            Rect reviseLocation = reviseLocation(i5, i6, this.mVisableBount);
            animate().alpha(1.0f).x((float) reviseLocation.left).y((float) reviseLocation.top).setDuration(400);
            this.mVisibleState = 0;
            this.mHandler.removeMessages(0);
            this.mHandler.sendEmptyMessageDelayed(0, (long) UNUSED_TRIGGER_TIME);
        }
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
    }

    public void onPause() {
        this.mHandler.removeMessages(0);
        setRelateVisible(8);
        this.mVisibleState = 2;
    }

    public void onResume() {
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }
}
