package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import com.android.camera.AutoLockManager;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.ui.V6SeekBar.OnValueChangedListener;
import java.util.ArrayList;
import java.util.List;

public class V6SeekbarPopup extends V6AbstractSettingPopup implements OnValueChangedListener {
    private V6SeekBar mBar;
    private V6SeekbarPopupTexts mTexts;
    private int mValue;

    public V6SeekbarPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void filterPreference(IconListPreference iconListPreference) {
        if (iconListPreference != null && "pref_delay_capture_key".equals(iconListPreference.getKey())) {
            List arrayList = new ArrayList(3);
            for (CharSequence charSequence : iconListPreference.getEntryValues()) {
                if (!charSequence.equals("0")) {
                    arrayList.add(charSequence.toString());
                }
            }
            iconListPreference.filterUnsupported(arrayList);
        }
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        if ("pref_camera_face_beauty_mode_key".equals(iconListPreference.getKey())) {
            iconListPreference = (IconListPreference) preferenceGroup.findPreference("pref_camera_face_beauty_key");
        } else if ("pref_delay_capture_mode".equals(iconListPreference.getKey())) {
            iconListPreference = (IconListPreference) preferenceGroup.findPreference("pref_delay_capture_key");
            filterPreference(iconListPreference);
        }
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        this.mValue = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        this.mTexts.initialize(iconListPreference);
        this.mBar.initialize(iconListPreference);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBar = (V6SeekBar) findViewById(C0049R.id.bar);
        this.mTexts = (V6SeekbarPopupTexts) findViewById(C0049R.id.texts);
        this.mBar.setOnValueChangedListener(this);
    }

    public void onValueChanged(int i, boolean z) {
        if (i != this.mValue) {
            this.mValue = i;
            this.mTexts.setValue(i);
            this.mPreference.setValueIndex(i);
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(7, 0, 0, new String(this.mPreference.getKey()), null);
            }
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
        }
    }

    public void reloadPreference() {
        this.mValue = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        this.mTexts.setValue(this.mValue);
        this.mBar.setValue(this.mValue);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mBar != null) {
            this.mBar.setEnabled(z);
        }
    }

    public void setOrientation(int i, boolean z) {
        this.mTexts.setOrientation(i, z);
    }
}
