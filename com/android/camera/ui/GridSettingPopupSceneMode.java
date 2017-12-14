package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;

public class GridSettingPopupSceneMode extends GridSettingPopup {
    public GridSettingPopupSceneMode(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        iconListPreference = (IconListPreference) preferenceGroup.findPreference("pref_camera_scenemode_key");
        this.mGridViewHeight = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.scene_settings_popup_height);
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
    }
}
