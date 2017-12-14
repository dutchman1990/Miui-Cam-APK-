package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import com.android.camera.ActivityBase;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.List;

public abstract class V6AbstractSettingPopup extends RelativeLayout implements Rotatable {
    protected List<String> mDisableKeys;
    protected MessageDispacher mMessageDispacher;
    protected IconListPreference mPreference;
    protected PreferenceGroup mPreferenceGroup;

    public V6AbstractSettingPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void dismiss(boolean z) {
        setVisibility(8);
    }

    public Animation getAnimation(boolean z) {
        return null;
    }

    public String getKey() {
        return this.mPreference != null ? this.mPreference.getKey() : null;
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        this.mPreferenceGroup = preferenceGroup;
        this.mPreference = iconListPreference;
        this.mMessageDispacher = messageDispacher;
        updateBackground();
    }

    protected void notifyPopupVisibleChange(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(3, 0, 3, Boolean.valueOf(z), null);
        }
    }

    public void onDestroy() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public abstract void reloadPreference();

    public void show(boolean z) {
        setVisibility(0);
    }

    public void updateBackground() {
        if (((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
            setBackgroundResource(C0049R.color.fullscreen_background);
        } else {
            setBackgroundResource(C0049R.color.halfscreen_background);
        }
    }
}
