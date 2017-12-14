package com.android.camera.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;
import java.util.List;

public class ModeButton extends V6TopTextView {
    public ModeButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void notifyClickToDispatcher() {
        if (this.mMessageDispacher != null) {
            CameraSettings.cancelRemind("pref_camera_mode_settings_key");
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.mode_button, 3, null, null);
        }
    }

    public void onCameraOpen() {
        updateVisible();
        updateRemind();
    }

    public void updateRemind() {
        List supportedSettingKeys = ((ActivityBase) this.mContext).getCurrentModule().getSupportedSettingKeys();
        Object obj = null;
        for (String str : CameraSettings.sRemindMode) {
            if (CameraSettings.isNeedRemind(str) && ("pref_camera_mode_settings_key".equals(str) || supportedSettingKeys.contains(str))) {
                obj = 1;
                break;
            }
        }
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0049R.dimen.panel_imageview_button_padding_width);
        if (obj != null) {
            int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0049R.dimen.mode_remind_margin);
            Drawable drawable = getResources().getDrawable(C0049R.drawable.ic_new_remind);
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
            setCompoundDrawablePadding(dimensionPixelSize2);
            setPaddingRelative((drawable.getIntrinsicWidth() + dimensionPixelSize2) + dimensionPixelSize, 0, dimensionPixelSize, 0);
        } else if (super.getCompoundDrawablesRelative()[2] != null) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            setPaddingRelative(dimensionPixelSize, 0, dimensionPixelSize, 0);
        }
    }

    public void updateVisible() {
        List supportedSettingKeys = ((ActivityBase) this.mContext).getCurrentModule().getSupportedSettingKeys();
        int i = (supportedSettingKeys == null || supportedSettingKeys.size() == 0) ? 8 : 0;
        setVisibility(i);
    }
}
