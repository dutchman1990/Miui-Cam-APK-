package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.camera.AutoLockManager;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.ui.V6SeekBar.OnValueChangedListener;

public class V6SkinBeautifySeekbarPopup extends V6AbstractSettingPopup implements OnValueChangedListener {
    private V6SeekBar mBar;
    private TextView mMaxText;
    private TextView mMinText;
    private int mValue;

    public V6SkinBeautifySeekbarPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        this.mValue = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        this.mMinText.setText(iconListPreference.getEntries()[0]);
        this.mMaxText.setText(iconListPreference.getEntries()[iconListPreference.getEntries().length - 1]);
        this.mBar.initialize(iconListPreference);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBar = (V6SeekBar) findViewById(C0049R.id.bar);
        this.mMinText = (TextView) findViewById(C0049R.id.min);
        this.mMaxText = (TextView) findViewById(C0049R.id.max);
        this.mBar.setOnValueChangedListener(this);
    }

    public void onValueChanged(int i, boolean z) {
        if (i != this.mValue) {
            this.mValue = i;
            this.mPreference.setValueIndex(i);
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(7, 0, 0, new String(this.mPreference.getKey()), null);
            }
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
        }
    }

    public void reloadPreference() {
        this.mValue = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        this.mBar.setValue(this.mValue);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mBar != null) {
            this.mBar.setEnabled(z);
        }
    }

    public void setOrientation(int i, boolean z) {
    }
}
