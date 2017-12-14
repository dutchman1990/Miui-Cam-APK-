package com.android.camera.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Log;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubScreenPopup extends V6AbstractSettingPopup {
    private V6AbstractSettingPopup mCurrentPopup;
    private V6ModeExitView mExitView;
    private OnPreDrawListener mOnPreDrawListener = new C01571();
    private AnimationType mPendingAnimationType;
    private HashMap<V6AbstractSettingPopup, Integer> mPopupTranslationMap = new HashMap();
    private ValueAnimator mRunningAnimation;
    private SettingView mSettingView;
    private FrameLayout mSubPopupParent;
    private SparseArray<ValueAnimator> mTranslationAnimationMap = new SparseArray();
    private View mValueBottomLine;

    class C01571 implements OnPreDrawListener {
        C01571() {
        }

        public boolean onPreDraw() {
            if (SubScreenPopup.this.mCurrentPopup == null || SubScreenPopup.this.mPendingAnimationType == null) {
                return true;
            }
            int -wrap2 = SubScreenPopup.this.computeTransY();
            SubScreenPopup.this.setTransY(SubScreenPopup.this.mCurrentPopup, -wrap2);
            SubScreenPopup.this.startAnimation(SubScreenPopup.this.setupAnimation(-wrap2, SubScreenPopup.this.mPendingAnimationType), SubScreenPopup.this.mPendingAnimationType);
            SubScreenPopup.this.removeOnPreDrawListener();
            return false;
        }
    }

    private enum AnimationType {
        SLIDE_DOWN_POPUP,
        SLIDE_UP_POPUP
    }

    private class CustomAnimatorListener extends AnimatorListenerAdapter implements AnimatorUpdateListener {
        private boolean mIsValueVisible;
        private int mLayerType;
        private AnimationType mType;

        public CustomAnimatorListener(SubScreenPopup subScreenPopup) {
            this(true, AnimationType.SLIDE_DOWN_POPUP);
        }

        public CustomAnimatorListener(boolean z, AnimationType animationType) {
            this.mIsValueVisible = z;
            this.mType = animationType;
        }

        public void onAnimationCancel(Animator animator) {
            Log.m5v("V6ManualPopup", "onAnimationCancel: animation=" + animator);
        }

        public void onAnimationEnd(Animator animator) {
            Object obj = AnimationType.SLIDE_DOWN_POPUP == this.mType ? 1 : null;
            Log.m5v("V6ManualPopup", "onAnimationEnd: type=" + this.mType + ",animation=" + animator + ",popup=" + SubScreenPopup.this.mCurrentPopup);
            if (obj != null) {
                SubScreenPopup.this.mValueBottomLine.setVisibility(8);
                if (SubScreenPopup.this.mCurrentPopup != null) {
                    SubScreenPopup.this.mCurrentPopup.setVisibility(8);
                    SubScreenPopup.this.mCurrentPopup = null;
                }
            }
            SubScreenPopup.this.mExitView.setTranslationY(0.0f);
            SubScreenPopup.this.mValueBottomLine.setAlpha(1.0f);
            SubScreenPopup.this.mValueBottomLine.setTranslationY(0.0f);
            SubScreenPopup.this.setLayerType(this.mLayerType, null);
        }

        public void onAnimationStart(Animator animator) {
            this.mLayerType = SubScreenPopup.this.getLayerType();
            if (this.mLayerType != 2) {
                SubScreenPopup.this.setLayerType(2, null);
            }
            Log.m5v("V6ManualPopup", "onAnimationStart: layerType=" + this.mLayerType + ",type=" + this.mType + ",animation=" + animator + ",popup=" + SubScreenPopup.this.mCurrentPopup);
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            float animatedFraction = 1.0f - valueAnimator.getAnimatedFraction();
            SubScreenPopup.this.mExitView.setTranslationY(floatValue);
            SubScreenPopup.this.mValueBottomLine.setAlpha(animatedFraction);
            SubScreenPopup.this.mValueBottomLine.setTranslationY(floatValue);
        }

        public void setAnimationType(AnimationType animationType) {
            this.mType = animationType;
        }
    }

    public SubScreenPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private boolean addOnPreDrawListener() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (!viewTreeObserver.isAlive()) {
            return false;
        }
        viewTreeObserver.addOnPreDrawListener(this.mOnPreDrawListener);
        return true;
    }

    private int computeTransY() {
        return this.mSubPopupParent.getHeight();
    }

    private List<String> getItemKeys() {
        List<String> arrayList = new ArrayList();
        if (this.mPreference == null) {
            if (Device.IS_MI3TD || Device.IS_HM3Y || Device.IS_HM3Z) {
                arrayList.add("pref_skin_beautify_enlarge_eye_key");
            }
            arrayList.add("pref_skin_beautify_slim_face_key");
            if (!Device.isGlobalBuild()) {
                arrayList.add("pref_skin_beautify_skin_color_key");
            }
            arrayList.add("pref_skin_beautify_skin_smooth_key");
        } else {
            arrayList.add("pref_camera_whitebalance_key");
            if (Device.isSupportedManualFunction()) {
                arrayList.add("pref_focus_position_key");
                arrayList.add("pref_qc_camera_exposuretime_key");
            }
            arrayList.add("pref_qc_camera_iso_key");
            if (CameraSettings.isSupportedOpticalZoom()) {
                arrayList.add("pref_camera_zoom_mode_key");
            }
        }
        return arrayList;
    }

    private int getTransY(V6AbstractSettingPopup v6AbstractSettingPopup) {
        Integer num = (Integer) this.mPopupTranslationMap.get(v6AbstractSettingPopup);
        return num == null ? 0 : num.intValue();
    }

    private boolean removeOnPreDrawListener() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (!viewTreeObserver.isAlive()) {
            return false;
        }
        viewTreeObserver.removeOnPreDrawListener(this.mOnPreDrawListener);
        return true;
    }

    private void setTransY(V6AbstractSettingPopup v6AbstractSettingPopup, int i) {
        if (v6AbstractSettingPopup != null) {
            this.mPopupTranslationMap.put(v6AbstractSettingPopup, Integer.valueOf(i));
        }
    }

    private ValueAnimator setupAnimation(int i, AnimationType animationType) {
        ValueAnimator valueAnimator = (ValueAnimator) this.mTranslationAnimationMap.get(i);
        if (valueAnimator != null) {
            for (AnimatorListener animatorListener : valueAnimator.getListeners()) {
                if (animatorListener instanceof CustomAnimatorListener) {
                    ((CustomAnimatorListener) animatorListener).setAnimationType(animationType);
                }
            }
            Log.m5v("V6ManualPopup", "setupAnimation: reuse transY=" + i + " -> anim=" + valueAnimator);
            return valueAnimator;
        }
        Object customAnimatorListener = new CustomAnimatorListener(this);
        customAnimatorListener.setAnimationType(animationType);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat("translationY", new float[]{0.0f, (float) i});
        valueAnimator = ObjectAnimator.ofPropertyValuesHolder(this.mSubPopupParent, new PropertyValuesHolder[]{ofFloat});
        valueAnimator.addListener(customAnimatorListener);
        valueAnimator.addUpdateListener(customAnimatorListener);
        this.mTranslationAnimationMap.put(i, valueAnimator);
        Log.m5v("V6ManualPopup", "setupAnimation: new transY=" + i + " -> anim=" + valueAnimator);
        return valueAnimator;
    }

    private boolean shouldAnimatePopup(V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (this.mSubPopupParent != null) {
            int childCount = this.mSubPopupParent.getChildCount();
            if (childCount == 0) {
                return true;
            }
            for (int i = 0; i < childCount; i++) {
                View childAt = this.mSubPopupParent.getChildAt(i);
                if (v6AbstractSettingPopup != childAt && childAt.getVisibility() == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startAnimation(ValueAnimator valueAnimator, AnimationType animationType) {
        this.mPendingAnimationType = null;
        if (!(this.mRunningAnimation == null || this.mRunningAnimation == valueAnimator || !this.mRunningAnimation.isRunning())) {
            this.mRunningAnimation.cancel();
        }
        this.mRunningAnimation = valueAnimator;
        if (animationType == AnimationType.SLIDE_DOWN_POPUP) {
            valueAnimator.start();
        } else {
            valueAnimator.reverse();
        }
    }

    public void dismiss(boolean z) {
        super.dismiss(z);
        this.mSettingView.onDismiss();
    }

    public boolean dismissChildPopup(V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (v6AbstractSettingPopup == null || v6AbstractSettingPopup.getVisibility() != 0) {
            return false;
        }
        if (shouldAnimatePopup(v6AbstractSettingPopup)) {
            int transY = getTransY(v6AbstractSettingPopup);
            Log.m5v("V6ManualPopup", "dismissChildPopup: transY=" + transY + ",popup=" + v6AbstractSettingPopup);
            if (transY == 0) {
                this.mPendingAnimationType = AnimationType.SLIDE_DOWN_POPUP;
                if (!addOnPreDrawListener()) {
                    if (this.mCurrentPopup == v6AbstractSettingPopup) {
                        this.mValueBottomLine.setVisibility(8);
                    }
                    v6AbstractSettingPopup.dismiss(false);
                }
            } else {
                startAnimation(setupAnimation(transY, AnimationType.SLIDE_DOWN_POPUP), AnimationType.SLIDE_DOWN_POPUP);
            }
        } else {
            if (this.mCurrentPopup == v6AbstractSettingPopup) {
                this.mValueBottomLine.setVisibility(8);
            }
            v6AbstractSettingPopup.dismiss(false);
        }
        return true;
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        List itemKeys = getItemKeys();
        this.mSettingView.initializeSettingScreen(this.mPreferenceGroup, itemKeys, itemKeys.size(), this.mMessageDispacher, this.mSubPopupParent, this);
    }

    public void onDestroy() {
        super.onDestroy();
        removeOnPreDrawListener();
        this.mSettingView.onDestroy();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mExitView = ((ActivityBase) this.mContext).getUIController().getModeExitView();
        this.mValueBottomLine = findViewById(C0049R.id.manual_popup_parent_upper_line);
        this.mSubPopupParent = (FrameLayout) findViewById(C0049R.id.setting_view_popup_parent);
        this.mSettingView = (SettingView) findViewById(C0049R.id.setting_view);
    }

    public void reloadPreference() {
        if (this.mSettingView != null) {
            this.mSettingView.reloadPreferences();
            if (this.mCurrentPopup != null && this.mCurrentPopup.getVisibility() == 0) {
                this.mSettingView.setPressed(this.mCurrentPopup.getKey(), true);
            }
        }
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mSettingView != null) {
            this.mSettingView.setEnabled(z);
        }
    }

    public void setOrientation(int i, boolean z) {
        this.mSettingView.setOrientation(i, z);
    }

    public void showChildPopup(V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (v6AbstractSettingPopup != null && v6AbstractSettingPopup.getVisibility() != 0) {
            this.mCurrentPopup = v6AbstractSettingPopup;
            this.mValueBottomLine.setVisibility(0);
            v6AbstractSettingPopup.show(false);
            if (shouldAnimatePopup(v6AbstractSettingPopup)) {
                int transY = getTransY(v6AbstractSettingPopup);
                Log.m5v("V6ManualPopup", "showChildPopup: transY=" + transY + ",popup=" + v6AbstractSettingPopup);
                if (transY == 0) {
                    this.mPendingAnimationType = AnimationType.SLIDE_UP_POPUP;
                    addOnPreDrawListener();
                    return;
                }
                startAnimation(setupAnimation(transY, AnimationType.SLIDE_UP_POPUP), AnimationType.SLIDE_UP_POPUP);
            }
        }
    }

    public void updateBackground() {
        if (((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
            this.mSettingView.setBackgroundResource(C0049R.color.fullscreen_background);
        } else {
            this.mSettingView.setBackgroundResource(C0049R.color.halfscreen_background);
        }
    }
}
