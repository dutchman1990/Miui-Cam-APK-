package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;

public class GridSettingTextPopup extends GridSettingPopup {
    private int mSavedGridViewWidth;
    private int mSavedPopupWidth;
    private SplitLineDrawer mSplitLineDrawer;

    public GridSettingTextPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void initializeSplitLine() {
        int i = getResources().getDisplayMetrics().widthPixels;
        int i2 = getResources().getDisplayMetrics().heightPixels;
        setSplitLineParameters(i < i2 ? i : i2, true, true);
    }

    private int setGridViewParameters(int i) {
        LayoutParams layoutParams = this.mGridView.getLayoutParams();
        int i2 = layoutParams.width;
        layoutParams.width = i;
        this.mGridView.setLayoutParams(layoutParams);
        return i2;
    }

    private void setGridViewSoundEffects(boolean z) {
        if (this.mGridView != null) {
            int childCount = this.mGridView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mGridView.getChildAt(i).setSoundEffectsEnabled(z);
            }
        }
    }

    private void setSplitLineParameters(int i, boolean z, boolean z2) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mSplitLineDrawer.getLayoutParams();
        layoutParams.height = -1;
        layoutParams.width = i;
        this.mSplitLineDrawer.setLayoutParams(layoutParams);
        this.mSplitLineDrawer.initialize(1, this.mDisplayColumnNum);
        this.mSplitLineDrawer.setBorderVisible(z, z2);
        this.mSplitLineDrawer.setVisibility(0);
    }

    protected int getItemResId() {
        return C0049R.layout.grid_setting_text_item;
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        this.mHasImage = false;
        if ("pref_audio_focus_mode_key".equals(iconListPreference.getKey())) {
            iconListPreference = (IconListPreference) preferenceGroup.findPreference("pref_audio_focus_key");
        } else if ("pref_camera_tilt_shift_mode".equals(iconListPreference.getKey())) {
            iconListPreference = (IconListPreference) preferenceGroup.findPreference("pref_camera_tilt_shift_key");
        } else {
            this.mIgnoreSameItemClick = false;
        }
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        initializeSplitLine();
    }

    protected void notifyToDispatcher(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(6, 0, 3, this.mPreference.getKey(), Boolean.valueOf(z));
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSplitLineDrawer = (SplitLineDrawer) findViewById(C0049R.id.text_popup_split_line_view);
        this.mGridViewHeight = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.text_only_settings_screen_popup_height);
    }

    public void restoreFromShrink() {
        updateBackground();
        if (this.mSavedPopupWidth != 0) {
            LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = this.mSavedPopupWidth;
            this.mSavedPopupWidth = 0;
            setLayoutParams(layoutParams);
        }
        if (this.mSavedGridViewWidth != 0) {
            setGridViewParameters(this.mSavedGridViewWidth);
            this.mSavedGridViewWidth = 0;
        }
        setGridViewSoundEffects(true);
        initializeSplitLine();
    }

    public void shrink(int i) {
        setBackgroundResource(C0049R.drawable.bg_shrunk_audio_focus_full_screen);
        LayoutParams layoutParams = getLayoutParams();
        this.mSavedPopupWidth = layoutParams.width;
        layoutParams.width = i;
        setLayoutParams(layoutParams);
        this.mSavedGridViewWidth = setGridViewParameters(i);
        setGridViewSoundEffects(false);
        setSplitLineParameters(i, false, false);
    }

    protected void updateItemView(int i, View view) {
        super.updateItemView(i, view);
        if (view != null) {
            ((TextView) view.findViewById(C0049R.id.text)).setTextAppearance(this.mContext, C0049R.style.GridTextOnlySettingItem);
            if (this.mDisableKeys != null && this.mPreference.getEntryValues() != null && i < this.mPreference.getEntryValues().length) {
                if (this.mDisableKeys.contains(this.mPreference.getEntryValues()[i])) {
                    view.setEnabled(false);
                } else {
                    view.setEnabled(true);
                }
            }
        }
    }
}
