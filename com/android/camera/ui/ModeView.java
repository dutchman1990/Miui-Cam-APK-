package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.android.camera.ActivityBase;
import com.android.camera.Log;
import com.android.camera.effect.EffectController;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModeView extends RelativeLayout implements Rotatable, AnimationListener, MessageDispacher {
    private int mColumnCount;
    protected String mCurrentMode = "mode_none";
    private Set<String> mDisabledIndicator = new HashSet();
    private boolean mEnabled;
    private int mFirstSelectedItem = -1;
    protected ArrayList<V6IndicatorButton> mIndicators = new ArrayList();
    protected boolean mIsAnimating = false;
    private int mItemWidth;
    protected boolean mKeepExitButtonGone = false;
    protected MessageDispacher mMessageDispacher;
    protected int mOrientation = 0;
    protected PreferenceGroup mPreferenceGroup;
    protected ArrayList<Rotatable> mRotatables = new ArrayList();
    private int mRowCount;
    private ScreenView mSettingScreen;

    public ModeView(Context context) {
        super(context);
    }

    public ModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ModeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private void initScreenView(int i) {
        if (i < 9) {
            this.mRowCount = ((i + 3) - 1) / 3;
            if (i >= 3) {
                i = 3;
            }
            this.mColumnCount = i;
            return;
        }
        this.mRowCount = 3;
        this.mColumnCount = 3;
    }

    private void resetOtherSettings(V6IndicatorButton v6IndicatorButton) {
        if (this.mFirstSelectedItem >= 0 && this.mFirstSelectedItem < this.mIndicators.size()) {
            ((V6IndicatorButton) this.mIndicators.get(this.mFirstSelectedItem)).resetSettings();
        }
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        Log.m5v("Camera5", "ModeView dispacherMessage mEnabled=" + this.mEnabled + " what=" + i + " sender=" + i2 + " receiver=" + i3 + " extra1=" + obj + " extra2=" + obj2 + " getVisibility()=" + getVisibility() + " mEnabled=" + this.mEnabled);
        if (i == 8) {
            resetOtherSettings((V6IndicatorButton) obj2);
            return true;
        }
        ((ActivityBase) this.mContext).getUIController().getPreviewPage().onPopupChange();
        if (i == 6 && (obj2 instanceof V6IndicatorButton)) {
            if (((V6IndicatorButton) obj2).isItemSelected()) {
                this.mFirstSelectedItem = this.mIndicators.indexOf(obj2);
            } else {
                this.mFirstSelectedItem = -1;
            }
        }
        this.mMessageDispacher.dispacherMessage(i, C0049R.id.v6_setting_page, 2, obj, obj2);
        if (obj2 instanceof V6IndicatorButton) {
            Log.m5v("Camera5", "call indicatorbutton reloadPreference");
            ((V6IndicatorButton) obj2).reloadPreference();
        }
        return false;
    }

    public void dissmissAllPopup() {
        for (V6IndicatorButton v6IndicatorButton : this.mIndicators) {
            v6IndicatorButton.onDestroy();
            if (v6IndicatorButton.getPopup() != null) {
                v6IndicatorButton.dismissPopup();
            }
        }
    }

    public View getCurrentPopup() {
        for (V6IndicatorButton v6IndicatorButton : this.mIndicators) {
            if (v6IndicatorButton.isPopupVisible()) {
                return v6IndicatorButton.getPopup();
            }
        }
        return null;
    }

    protected void initIndicators(List<String> list) {
        int size = ((list.size() - 1) / 9) + 1;
        initScreenView(list.size());
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        for (int i = 0; i < size; i++) {
            View modeGridView = new ModeGridView(getContext(), this.mSettingScreen, this.mRowCount, this.mColumnCount, this.mItemWidth, this.mItemWidth, i);
            for (int i2 = 0; i2 < 9; i2++) {
                int i3 = i2 + ((i * 3) * 3);
                if (i3 >= list.size()) {
                    break;
                }
                IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference((String) list.get(i3));
                if (iconListPreference != null) {
                    updatePrefence(iconListPreference);
                    V6IndicatorButton v6IndicatorButton = (V6IndicatorButton) layoutInflater.inflate(C0049R.layout.v6_indicator_button, modeGridView, false);
                    v6IndicatorButton.initialize(iconListPreference, this, ((ActivityBase) this.mContext).getUIController().getPopupParent(), this.mItemWidth, this.mItemWidth, this.mPreferenceGroup);
                    v6IndicatorButton.setMessageDispacher(this);
                    if (v6IndicatorButton.isItemSelected()) {
                        this.mFirstSelectedItem = this.mIndicators.size();
                    }
                    modeGridView.addView(v6IndicatorButton);
                    this.mIndicators.add(v6IndicatorButton);
                }
            }
            this.mSettingScreen.addView(modeGridView);
        }
    }

    public void initializeSettingScreen(PreferenceGroup preferenceGroup, List<String> list, MessageDispacher messageDispacher, int i) {
        this.mPreferenceGroup = preferenceGroup;
        this.mMessageDispacher = messageDispacher;
        this.mSettingScreen = (ScreenView) findViewById(C0049R.id.setting_screens);
        this.mSettingScreen.setSeekPointResource(C0049R.drawable.screen_view_seek_point_selector);
        LayoutParams layoutParams = new LayoutParams(-2, -2, 49);
        layoutParams.setMargins(0, getResources().getDimensionPixelSize(C0049R.dimen.mode_settings_screen_indicator_margin_top), 0, 0);
        this.mSettingScreen.setSeekBarPosition(layoutParams);
        this.mSettingScreen.removeAllScreens();
        this.mSettingScreen.setOverScrollRatio(0.0f);
        this.mFirstSelectedItem = -1;
        this.mDisabledIndicator.clear();
        dissmissAllPopup();
        removePopup();
        this.mIndicators.clear();
        initIndicators(list);
        this.mSettingScreen.setCurrentScreen(0);
    }

    public boolean isItemSelected() {
        return this.mFirstSelectedItem != -1;
    }

    public void onAnimationEnd(Animation animation) {
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mItemWidth = getResources().getDimensionPixelSize(C0049R.dimen.v6_setting_item_width);
    }

    public void overrideSettings(String... strArr) {
        if (strArr.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        for (V6IndicatorButton overrideSettings : this.mIndicators) {
            overrideSettings.overrideSettings(strArr);
        }
    }

    public void reloadPreferences() {
        for (V6IndicatorButton v6IndicatorButton : this.mIndicators) {
            v6IndicatorButton.setOrientation(this.mOrientation, false);
            v6IndicatorButton.reloadPreference();
        }
    }

    public void removePopup() {
        for (V6IndicatorButton v6IndicatorButton : this.mIndicators) {
            v6IndicatorButton.removePopup();
            PopupManager.getInstance(this.mContext).removeOnOtherPopupShowedListener(v6IndicatorButton);
        }
    }

    public void resetSelectedFlag() {
        this.mFirstSelectedItem = -1;
    }

    public boolean resetSettings() {
        if (this.mFirstSelectedItem == -1 || this.mFirstSelectedItem >= this.mIndicators.size()) {
            return false;
        }
        ((V6IndicatorButton) this.mIndicators.get(this.mFirstSelectedItem)).resetSettings();
        return true;
    }

    public void setEnabled(boolean z) {
        this.mEnabled = z;
        for (V6IndicatorButton v6IndicatorButton : this.mIndicators) {
            if (!this.mDisabledIndicator.contains(v6IndicatorButton.getKey())) {
                v6IndicatorButton.setEnabled(z);
            }
        }
        super.setEnabled(z);
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        if (this.mIndicators != null) {
            for (int i2 = 0; i2 < this.mIndicators.size(); i2++) {
                ((V6AbstractIndicator) this.mIndicators.get(i2)).setOrientation(i, z);
            }
        }
    }

    protected void updatePrefence(IconListPreference iconListPreference) {
        if ("pref_camera_shader_coloreffect_key".equals(iconListPreference.getKey())) {
            iconListPreference.setEntries(EffectController.getInstance().getEntries());
            iconListPreference.setEntryValues(EffectController.getInstance().getEntryValues());
        }
    }
}
