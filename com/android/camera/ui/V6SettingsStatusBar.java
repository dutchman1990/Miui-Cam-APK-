package com.android.camera.ui;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.CameraManager;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.preferences.CameraSettingPreferences;
import java.util.Locale;

public class V6SettingsStatusBar extends V6RelativeLayout implements MutexView {
    private TextView mApertureTextView;
    private TextView mEvTextView;
    private int mMarginTop;
    private int mMarginTopLandscape;
    private MessageDispacher mMessageDispacher;
    private int mOrientation = -1;
    private boolean mVisible;
    private TextView mZoomTextView;

    public V6SettingsStatusBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void setSubViewVisible(View view, int i, boolean z) {
        if (i != view.getVisibility() || !z) {
            if (i != 8) {
                show();
            }
            view.setVisibility(i);
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(4, C0049R.id.v6_setting_status_bar, 3, Boolean.valueOf(isSubViewShown()), null);
            }
        }
    }

    public void animateIn(Runnable runnable) {
        if (getVisibility() != 0 || !this.mVisible) {
            if (getVisibility() != 0) {
                setVisibility(0);
            }
            animate().withLayer().alpha(1.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).withEndAction(runnable).start();
            this.mVisible = true;
        }
    }

    public void animateOut(final Runnable runnable) {
        this.mVisible = false;
        if (getVisibility() == 0) {
            animate().withLayer().alpha(0.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                public void run() {
                    if (!V6SettingsStatusBar.this.mVisible) {
                        V6SettingsStatusBar.this.setVisibility(8);
                    }
                    if (runnable != null) {
                        runnable.run();
                    }
                    V6SettingsStatusBar.this.setAlpha(1.0f);
                }
            }).start();
        }
    }

    public void hide() {
        this.mVisible = false;
        setVisibility(8);
    }

    public boolean isSubViewShown() {
        for (View visibility : this.mChildren) {
            if (visibility.getVisibility() == 0) {
                return true;
            }
        }
        return false;
    }

    public void onCameraOpen() {
        boolean z = false;
        updateStatus();
        if (getVisibility() == 0) {
            z = true;
        }
        this.mVisible = z;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mEvTextView = (TextView) findChildrenById(C0049R.id.ev_text);
        this.mZoomTextView = (TextView) findChildrenById(C0049R.id.zoom_text);
        this.mApertureTextView = (TextView) findChildrenById(C0049R.id.aperture_text);
        this.mMarginTop = getResources().getDimensionPixelOffset(C0049R.dimen.v6_setting_status_bar_margin_top);
        this.mMarginTopLandscape = getResources().getDimensionPixelOffset(C0049R.dimen.v6_setting_status_bar_margin_top_landscape);
    }

    public void onResume() {
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        super.setMessageDispacher(messageDispacher);
        this.mMessageDispacher = messageDispacher;
    }

    public void setOrientation(int i, boolean z) {
    }

    public void show() {
        setVisibility(0);
        this.mVisible = true;
    }

    public void updateAperture() {
        if (Device.isSupportedStereo() && CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            CharSequence entry = ((ActivityBase) this.mContext).getUIController().getPreferenceGroup().findPreference("pref_camera_stereo_key").getEntry();
            boolean equals = this.mApertureTextView.getText().equals(entry);
            this.mApertureTextView.setText(entry);
            setSubViewVisible(this.mApertureTextView, 0, equals);
            return;
        }
        setSubViewVisible(this.mApertureTextView, 8, true);
    }

    public void updateEV() {
        float readExposure = ((float) CameraSettings.readExposure(CameraSettingPreferences.instance())) * CameraManager.instance().getStashParameters().getExposureCompensationStep();
        if (Math.abs(readExposure) <= 0.05f || CameraSettings.isSupportedPortrait()) {
            setSubViewVisible(this.mEvTextView, 8, true);
            return;
        }
        String str = readExposure < 0.0f ? "-" : "+";
        CharSequence format = String.format(Locale.ENGLISH, "%s %.1f", new Object[]{str, Float.valueOf(Math.abs(readExposure))});
        boolean equals = this.mEvTextView.getText().equals(format);
        this.mEvTextView.setText(format);
        setSubViewVisible(this.mEvTextView, 0, equals);
    }

    public void updateStatus() {
        updateEV();
        updateZoom();
        updateAperture();
    }

    public void updateStatus(String str) {
        if ("pref_camera_zoom_key".equals(str)) {
            updateZoom();
        } else if ("pref_camera_exposure_key".equals(str)) {
            updateEV();
        } else if ("pref_camera_stereo_key".equals(str)) {
            updateAperture();
        }
    }

    public void updateZoom() {
        Parameters stashParameters = CameraManager.instance().getStashParameters();
        if (((((ActivityBase) this.mContext).getUIController().getZoomButton().getVisibility() == 8) || CameraSettings.isSwitchCameraZoomMode()) && stashParameters != null) {
            int intValue = ((Integer) stashParameters.getZoomRatios().get(CameraSettings.readZoom(CameraSettingPreferences.instance()))).intValue();
            if (intValue > 100) {
                intValue /= 10;
                CharSequence charSequence = "x " + (intValue / 10) + "." + (intValue % 10);
                boolean equals = this.mZoomTextView.getText().equals(charSequence);
                this.mZoomTextView.setText(charSequence);
                setSubViewVisible(this.mZoomTextView, 0, equals);
                return;
            }
        }
        setSubViewVisible(this.mZoomTextView, 8, true);
    }
}
