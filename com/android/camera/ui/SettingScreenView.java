package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingScreenView extends SettingView implements AnimationListener {
    private int mColumnCount;
    private Set<String> mDisabledIndicator = new HashSet();
    private Animation mFadeIn;
    private Animation mFadeOut;
    private List<String> mKeys;
    private View mParent;
    private V6AbstractSettingPopup mParentPopup;
    private ViewGroup mPopupRoot;
    private int mRowCount;
    private int mScreenHeight;
    private ScreenView mSettingScreen;
    protected SplitLineDrawer mSplitLineDrawer;

    public SettingScreenView(Context context) {
        super(context);
    }

    public SettingScreenView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SettingScreenView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private void clearMessageDispatcher() {
        for (V6AbstractIndicator onDestroy : this.mIndicators) {
            onDestroy.onDestroy();
        }
    }

    private int getActualColumnCount() {
        return this.mKeys.size() < this.mColumnCount ? this.mKeys.size() : this.mColumnCount;
    }

    private void initScreenView() {
        this.mColumnCount = 4;
        this.mRowCount = 1;
        this.mScreenHeight = getResources().getDimensionPixelSize(C0049R.dimen.settings_screen_height);
    }

    private void initializeSplitLine() {
        int i = getResources().getDisplayMetrics().widthPixels;
        int i2 = getResources().getDisplayMetrics().heightPixels;
        int i3 = i < i2 ? i : i2;
        LayoutParams layoutParams = (LayoutParams) this.mSplitLineDrawer.getLayoutParams();
        layoutParams.height = this.mScreenHeight;
        layoutParams.width = i3;
        this.mSplitLineDrawer.setLayoutParams(layoutParams);
        this.mSplitLineDrawer.initialize(this.mRowCount, getActualColumnCount());
        this.mSplitLineDrawer.setVisibility(0);
    }

    public void dismiss() {
        if (this.mParent != null) {
            this.mParent.clearAnimation();
            this.mParent.startAnimation(this.mFadeOut);
            this.mParent.setVisibility(8);
        }
    }

    public int getVisibility() {
        return this.mParent != null ? this.mParent.getVisibility() : 8;
    }

    protected void initIndicators(List<String> list) {
        int i = this.mColumnCount * this.mRowCount;
        int size = ((list.size() - 1) / i) + 1;
        int paddingLeft = (((getResources().getDisplayMetrics().widthPixels - this.mParent.getPaddingLeft()) - this.mParent.getPaddingRight()) - this.mPaddingLeft) - this.mPaddingRight;
        int i2 = getResources().getDisplayMetrics().heightPixels;
        if (paddingLeft >= i2) {
            paddingLeft = i2;
        }
        int actualColumnCount = (int) ((((float) paddingLeft) / ((float) getActualColumnCount())) + 0.5f);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        for (int i3 = 0; i3 < size; i3++) {
            View staticGridView = new StaticGridView(getContext(), this.mRowCount, getActualColumnCount(), actualColumnCount, this.mScreenHeight);
            for (int i4 = 0; i4 < i; i4++) {
                int i5 = i4 + ((this.mRowCount * i3) * this.mColumnCount);
                if (i5 >= list.size()) {
                    break;
                }
                IconListPreference iconListPreference = (IconListPreference) this.mPreferenceGroup.findPreference((String) list.get(i5));
                if (iconListPreference != null) {
                    SubScreenIndicatorButton subScreenIndicatorButton = (SubScreenIndicatorButton) layoutInflater.inflate(C0049R.layout.sub_screen_indicator_button, staticGridView, false);
                    subScreenIndicatorButton.initialize(iconListPreference, this.mMessageDispacher, this.mPopupRoot, actualColumnCount, -2, this.mPreferenceGroup, this.mParentPopup);
                    this.mIndicators.add(subScreenIndicatorButton);
                    subScreenIndicatorButton.setContentDescription(iconListPreference.getTitle());
                    staticGridView.addView(subScreenIndicatorButton);
                }
            }
            this.mSettingScreen.addView(staticGridView);
        }
    }

    public void initializeSettingScreen(PreferenceGroup preferenceGroup, List<String> list, int i, MessageDispacher messageDispacher, ViewGroup viewGroup, V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (this.mFadeIn != null) {
            this.mFadeIn.setAnimationListener(null);
        }
        if (this.mFadeOut != null) {
            this.mFadeOut.setAnimationListener(null);
        }
        this.mFadeIn = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.screen_setting_fade_in);
        this.mFadeOut = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.screen_setting_fade_out);
        this.mFadeIn.setAnimationListener(this);
        this.mFadeOut.setAnimationListener(this);
        this.mParent = getRootView().findViewById(C0049R.id.setting_view_popup_layout);
        this.mPreferenceGroup = preferenceGroup;
        this.mMessageDispacher = messageDispacher;
        this.mKeys = list;
        initScreenView();
        this.mColumnCount = i;
        this.mSettingScreen = (ScreenView) findViewById(C0049R.id.setting_screens);
        this.mSettingScreen.setLayoutParams(new LayoutParams(-1, this.mScreenHeight));
        this.mSettingScreen.removeAllScreens();
        this.mSettingScreen.setOverScrollRatio(0.0f);
        this.mPopupRoot = viewGroup;
        this.mParentPopup = v6AbstractSettingPopup;
        this.mDisabledIndicator.clear();
        ((ViewGroup) getRootView().findViewById(C0049R.id.setting_view_popup_parent)).removeAllViews();
        clearMessageDispatcher();
        this.mIndicators.clear();
        initIndicators(list);
        initializeSplitLine();
        this.mSettingScreen.setCurrentScreen(0);
    }

    public void onAnimationEnd(Animation animation) {
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onDestroy() {
        clearMessageDispatcher();
        this.mSettingScreen.removeAllScreens();
        super.onDestroy();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSplitLineDrawer = (SplitLineDrawer) findViewById(C0049R.id.split_line_view);
    }

    public void setEnabled(boolean z) {
        for (V6AbstractIndicator v6AbstractIndicator : this.mIndicators) {
            if (!this.mDisabledIndicator.contains(v6AbstractIndicator.getKey())) {
                v6AbstractIndicator.setEnabled(z);
            }
        }
        super.setEnabled(z);
    }

    public void setOrientation(int i, boolean z) {
        if (this.mIndicators != null) {
            for (int i2 = 0; i2 < this.mIndicators.size(); i2++) {
                ((V6AbstractIndicator) this.mIndicators.get(i2)).setOrientation(i, z);
            }
        }
    }

    public void setVisibility(int i) {
        if (i == 0) {
            show();
        } else {
            dismiss();
        }
    }

    public void show() {
        if (this.mParent != null) {
            this.mParent.clearAnimation();
            this.mParent.setVisibility(0);
            this.mParent.startAnimation(this.mFadeIn);
        }
    }
}
