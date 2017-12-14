package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.ActivityBase;
import com.android.camera.Log;
import com.android.camera.Util;

public class V6ModeExitView extends LinearLayout implements V6FunctionUI {
    private String mCurrentKey;
    private ExitButton mExitButton;
    private boolean mVisible = true;

    class C01671 implements OnLayoutChangeListener {
        C01671() {
        }

        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            Util.expandViewTouchDelegate(V6ModeExitView.this.mExitButton);
        }
    }

    public V6ModeExitView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void clearExitButtonClickListener(boolean z) {
        if (z && this.mExitButton.hasOnClickListeners()) {
            this.mExitButton.callOnClick();
        }
        setExitButtonClickListener(null, null);
        updateExitButton(-1, false);
    }

    public void enableControls(boolean z) {
        setEnabled(z);
        this.mExitButton.setEnabled(z);
    }

    public ExitButton getExitButton() {
        return this.mExitButton;
    }

    public void hide() {
        if (this.mVisible) {
            this.mVisible = false;
            setVisibility(8);
        }
    }

    public boolean isCurrentExitView(String str) {
        Log.m5v("Camera5", "V6ModeExitView isCurrent key=" + str + " mCurrentKey=" + this.mCurrentKey);
        return str != null ? str.equals(this.mCurrentKey) : false;
    }

    public boolean isExitButtonShown() {
        return this.mExitButton.getVisibility() == 0;
    }

    public void onCameraOpen() {
        if (this.mVisible) {
            updateBackground();
            setVisibility(0);
        }
    }

    public void onCreate() {
        if (!isCurrentExitView("pref_camera_stereo_mode_key")) {
            setExitButtonVisible(8);
            setExitButtonClickListener(null, null);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mExitButton = (ExitButton) findViewById(C0049R.id.camera_mode_exit_button);
        this.mExitButton.addOnLayoutChangeListener(new C01671());
    }

    public void onPause() {
        this.mExitButton.setExpandedAnimation(false);
    }

    public void onResume() {
    }

    public void setExitButtonClickListener(OnClickListener onClickListener, String str) {
        Log.m5v("Camera6", "V6ModeExitView setOnClickListener = " + onClickListener + " this=" + this);
        this.mCurrentKey = str;
        this.mExitButton.setOnClickListener(onClickListener);
    }

    public void setExitButtonVisible(int i) {
        if (this.mExitButton != null) {
            this.mExitButton.setAlpha(1.0f);
            this.mExitButton.setVisibility(i);
            if (8 == i) {
                Util.expandViewTouchDelegate(this.mExitButton);
            }
        }
    }

    public void setExitContent(int i) {
        if (i != -1) {
            this.mExitButton.setText(this.mContext.getResources().getString(i));
        }
    }

    public void setLayoutParameters(int i, int i2) {
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        if (i != 0) {
            layoutParams.addRule(2, i);
        } else {
            layoutParams.removeRule(2);
        }
        layoutParams.bottomMargin = i2;
        if (i2 != 0) {
            layoutParams.addRule(12, -1);
        } else {
            layoutParams.removeRule(12);
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
    }

    public void show() {
        if (!this.mVisible) {
            this.mVisible = true;
            setVisibility(0);
        }
    }

    public void updateBackground() {
        if (((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
            this.mExitButton.setBackgroundResource(C0049R.drawable.btn_camera_mode_exit_full_screen);
        } else {
            this.mExitButton.setBackgroundResource(C0049R.drawable.btn_camera_mode_exit);
        }
    }

    public void updateExitButton(int i, boolean z) {
        Log.m5v("Camera6", "V6ModeExitView updateExitButton = " + z);
        if (i != -1) {
            this.mExitButton.setText(this.mContext.getResources().getString(i));
        }
        int i2 = (!z || TextUtils.isEmpty(this.mExitButton.getText())) ? 8 : 0;
        setExitButtonVisible(i2);
    }
}
