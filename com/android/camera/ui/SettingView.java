package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.camera.preferences.PreferenceGroup;
import java.util.ArrayList;
import java.util.List;

public abstract class SettingView extends RelativeLayout implements Rotatable {
    protected ArrayList<V6AbstractIndicator> mIndicators = new ArrayList();
    protected boolean mIsAnimating = false;
    protected MessageDispacher mMessageDispacher;
    protected int mOrientation = 0;
    protected PreferenceGroup mPreferenceGroup;
    protected ArrayList<Rotatable> mRotatables = new ArrayList();

    public SettingView(Context context) {
        super(context);
    }

    public SettingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SettingView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public abstract void initializeSettingScreen(PreferenceGroup preferenceGroup, List<String> list, int i, MessageDispacher messageDispacher, ViewGroup viewGroup, V6AbstractSettingPopup v6AbstractSettingPopup);

    public void onDestroy() {
        this.mIndicators.clear();
        this.mMessageDispacher = null;
    }

    public void onDismiss() {
        for (V6AbstractIndicator onDismiss : this.mIndicators) {
            onDismiss.onDismiss();
        }
    }

    public void reloadPreferences() {
        for (V6AbstractIndicator reloadPreference : this.mIndicators) {
            reloadPreference.reloadPreference();
        }
    }

    public void setOrientation(int i, boolean z) {
    }

    public void setPressed(String str, boolean z) {
        for (V6AbstractIndicator v6AbstractIndicator : this.mIndicators) {
            if (str != null && str.equals(v6AbstractIndicator.getKey())) {
                v6AbstractIndicator.setPressed(z);
                return;
            }
        }
    }
}
