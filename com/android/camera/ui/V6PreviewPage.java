package com.android.camera.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.Device;
import com.android.camera.Log;

public class V6PreviewPage extends V6RelativeLayout implements OnClickListener {
    private ActivityBase mActivity;
    private ValueAnimator mAnimPopup;
    private CustomAnimatorListener mAnimatorListener;
    public ImageView mAsdIndicatorView;
    private TimeInterpolator mCollapseInterpolator = new OvershootInterpolator(1.0f);
    private TimeInterpolator mExpandInterpolator = new AccelerateDecelerateInterpolator();
    private OnLayoutChangeListener mLayoutChangeListener = new C01681();
    public OrientationIndicator mLyingOriFlag;
    private View mModeExitButton;
    public V6ModeExitView mModeExitView;
    private RotateLayout mOrientationArea;
    private RelativeLayout mOrientationParent;
    public RelativeLayout mPanoramaViewRoot;
    private boolean mPopupGroupVisible = true;
    private View mPopupIndicator;
    public View mPopupIndicatorLayout;
    public ViewGroup mPopupParent;
    public ViewGroup mPopupParentLayout;
    private boolean mPopupVisible = true;
    public PortraitButton mPortraitButton;
    public TextView mPortraitHintTextView;
    public StereoButton mStereoButton;
    public TopPopupParent mTopPopupParent;
    private boolean mVisible = true;
    public LinearLayout mWarningMessageLayout;
    public TextView mWarningView;
    public ZoomButton mZoomButton;

    class C01681 implements OnLayoutChangeListener {
        C01681() {
        }

        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            if (view == V6PreviewPage.this.mModeExitView) {
                V6PreviewPage.this.mZoomButton.updateLayoutLocation();
            }
            if (i != i5 || i3 != i7 || i2 != i6 || i4 != i8) {
                if (view == V6PreviewPage.this.mModeExitView || view == V6PreviewPage.this.mModeExitButton || view == V6PreviewPage.this.mPopupIndicatorLayout || view == V6PreviewPage.this.mPopupIndicator) {
                    V6PreviewPage.this.createOrUpdateAnimatorListener();
                } else if (view == V6PreviewPage.this.mPopupParent) {
                    V6PreviewPage.this.createAnimation();
                }
            }
        }
    }

    private enum AnimationType {
        COLLAPSE,
        EXPAND
    }

    private class CustomAnimatorListener extends AnimatorListenerAdapter implements AnimatorUpdateListener {
        private AnimationType mAnimationType;
        private int mIndicatorAndExitDeltaCenter;
        private int mIndicatorLayoutMaxY;
        private int mIndicatorLayoutTransY;
        private int mLayerType;
        private float mModeExitButtonHalfWidth;
        private int mModeExitButtonLeft;
        private int mModeExitButtonRight;

        public CustomAnimatorListener(V6PreviewPage v6PreviewPage) {
            this(AnimationType.COLLAPSE);
        }

        public CustomAnimatorListener(AnimationType animationType) {
            this.mAnimationType = animationType;
            updateParameters();
        }

        public AnimationType getAnimationType() {
            return this.mAnimationType;
        }

        public void onAnimationCancel(Animator animator) {
            Log.m5v("V6PreviewPage", "onAnimationCancel: type=" + this.mAnimationType);
        }

        public void onAnimationEnd(Animator animator) {
            Log.m5v("V6PreviewPage", "onAnimationEnd: type=" + this.mAnimationType);
            V6PreviewPage.this.setLayerType(this.mLayerType, null);
            V6PreviewPage.this.mModeExitView.setTranslationY(0.0f);
            V6PreviewPage.this.mZoomButton.setTranslationY(0.0f);
            V6PreviewPage.this.mPopupParent.setTranslationY(0.0f);
            V6PreviewPage.this.mPopupIndicatorLayout.setTranslationY(0.0f);
            V6PreviewPage.this.mModeExitButton.setLeft(this.mModeExitButtonLeft);
            V6PreviewPage.this.mModeExitButton.setRight(this.mModeExitButtonRight);
            if (AnimationType.EXPAND == this.mAnimationType) {
                V6PreviewPage.this.mPopupIndicatorLayout.setVisibility(4);
                V6PreviewPage.this.mPopupIndicator.setAlpha(1.0f);
            } else {
                V6PreviewPage.this.mModeExitView.hide();
                V6PreviewPage.this.mModeExitButton.setAlpha(1.0f);
                V6PreviewPage.this.mPopupParent.setVisibility(4);
            }
            V6PreviewPage.this.mZoomButton.updateLayoutLocation();
        }

        public void onAnimationStart(Animator animator) {
            this.mLayerType = V6PreviewPage.this.getLayerType();
            if (this.mLayerType != 2) {
                V6PreviewPage.this.setLayerType(2, null);
            }
            Log.m5v("V6PreviewPage", "onAnimationStart: type=" + this.mAnimationType + ",layerType=" + this.mLayerType);
            if (AnimationType.EXPAND == this.mAnimationType) {
                V6PreviewPage.this.mModeExitView.show();
                V6PreviewPage.this.mPopupParent.setVisibility(0);
            } else {
                V6PreviewPage.this.mPopupIndicatorLayout.setVisibility(0);
            }
            V6PreviewPage.this.mZoomButton.updateLayoutLocation();
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();
            float f = ((float) this.mIndicatorLayoutTransY) * animatedFraction;
            V6PreviewPage.this.mModeExitView.setTranslationY(f);
            V6PreviewPage.this.mZoomButton.setTranslationY(f);
            int i = (int) (((double) (this.mModeExitButtonHalfWidth * animatedFraction)) + 0.5d);
            int i2 = this.mModeExitButtonRight - i;
            V6PreviewPage.this.mModeExitButton.setLeft(this.mModeExitButtonLeft + i);
            V6PreviewPage.this.mModeExitButton.setRight(i2);
            V6PreviewPage.this.mModeExitButton.setAlpha(Math.max(1.0f - animatedFraction, 0.0f));
            V6PreviewPage.this.mPopupIndicatorLayout.setY(Math.min(V6PreviewPage.this.mModeExitView.getY() - ((float) this.mIndicatorAndExitDeltaCenter), (float) this.mIndicatorLayoutMaxY));
            V6PreviewPage.this.mPopupIndicator.setAlpha(animatedFraction);
        }

        public void setAnimationType(AnimationType animationType) {
            this.mAnimationType = animationType;
        }

        public void updateParameters() {
            this.mIndicatorAndExitDeltaCenter = (V6PreviewPage.this.mPopupIndicator.getTop() - V6PreviewPage.this.mModeExitButton.getTop()) + ((V6PreviewPage.this.mPopupIndicator.getHeight() - V6PreviewPage.this.mModeExitButton.getHeight()) / 2);
            this.mIndicatorLayoutMaxY = V6PreviewPage.this.mPopupIndicatorLayout.getTop() + ((V6PreviewPage.this.mPopupIndicatorLayout.getHeight() - V6PreviewPage.this.mPopupIndicator.getBottom()) + V6PreviewPage.this.mPopupIndicator.getPaddingBottom());
            this.mModeExitButtonLeft = V6PreviewPage.this.mModeExitButton.getLeft();
            this.mModeExitButtonRight = V6PreviewPage.this.mModeExitButton.getRight();
            this.mModeExitButtonHalfWidth = ((float) (this.mModeExitButtonRight - this.mModeExitButtonLeft)) * 0.5f;
            int -wrap0 = V6PreviewPage.this.getChildY(V6PreviewPage.this.mModeExitView);
            int -wrap02 = V6PreviewPage.this.getChildY(V6PreviewPage.this.mPopupIndicatorLayout);
            this.mIndicatorLayoutTransY = (-wrap02 - -wrap0) + this.mIndicatorAndExitDeltaCenter;
            Log.m5v("V6PreviewPage", "updateParameters: exitView=" + V6PreviewPage.this.mModeExitView + ",exitButton=" + V6PreviewPage.this.mModeExitButton + ",exitViewY=" + -wrap0);
            Log.m5v("V6PreviewPage", "updateParameters: indicatorLayout=" + V6PreviewPage.this.mPopupIndicatorLayout + ",indicator=" + V6PreviewPage.this.mPopupIndicator + ",indicatorLayoutY=" + -wrap02);
        }
    }

    public V6PreviewPage(Context context) {
        super(context);
        this.mActivity = (ActivityBase) context;
    }

    public V6PreviewPage(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mActivity = (ActivityBase) context;
    }

    private void createAnimation() {
        Log.m5v("V6PreviewPage", "createAnimation: popupHeight=" + this.mPopupParent.getHeight());
        if (this.mAnimatorListener == null) {
            createOrUpdateAnimatorListener();
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat("translationY", new float[]{0.0f, (float) this.mPopupParent.getHeight()});
        this.mAnimPopup = ObjectAnimator.ofPropertyValuesHolder(this.mPopupParent, new PropertyValuesHolder[]{ofFloat});
        this.mAnimPopup.addListener(this.mAnimatorListener);
        this.mAnimPopup.addUpdateListener(this.mAnimatorListener);
    }

    private void createOrUpdateAnimatorListener() {
        if (this.mAnimatorListener == null) {
            this.mAnimatorListener = new CustomAnimatorListener(this);
        } else {
            this.mAnimatorListener.updateParameters();
        }
    }

    private void doViewAnimation(View view, boolean z) {
        if ((view.getVisibility() == 0) == z) {
            return;
        }
        if (z) {
            if (view == this.mModeExitView) {
                this.mModeExitView.show();
            } else {
                view.setVisibility(0);
            }
        } else if (view == this.mModeExitView) {
            this.mModeExitView.hide();
        } else {
            view.setVisibility(4);
        }
    }

    private int getChildY(View view) {
        int top = view.getTop();
        V6PreviewPage parent = view.getParent();
        while ((parent instanceof View) && parent != this) {
            View view2 = parent;
            top += view2.getTop();
            parent = view2.getParent();
        }
        return top;
    }

    private View getCurrentPopupShownView() {
        if (this.mPopupParent.isShown()) {
            for (int i = 0; i < this.mPopupParent.getChildCount(); i++) {
                if (this.mPopupParent.getChildAt(i).isShown()) {
                    return this.mPopupParent.getChildAt(i);
                }
            }
        }
        return null;
    }

    private boolean hasCollapsedPopup() {
        return ((ActivityBase) this.mContext).getUIController().getSettingPage().getCurrentPopup() == null ? ((ActivityBase) this.mContext).getUIController().getStereoButton().isPopupVisible() : true;
    }

    private void hidePopupView() {
        if (getCurrentPopupShownView() instanceof StereoPopup) {
            ((ActivityBase) this.mContext).getUIController().getStereoButton().dismissPopup(true);
            return;
        }
        this.mAnimatorListener.setAnimationType(AnimationType.COLLAPSE);
        this.mAnimPopup.setInterpolator(this.mCollapseInterpolator);
        this.mAnimPopup.start();
    }

    private Animation initAnimation(V6AbstractSettingPopup v6AbstractSettingPopup, boolean z) {
        Animation animation = v6AbstractSettingPopup.getAnimation(z);
        if (animation == null) {
            animation = z ? AnimationUtils.loadAnimation(this.mContext, C0049R.anim.slide_up) : AnimationUtils.loadAnimation(this.mContext, C0049R.anim.slide_down);
        }
        if (!z) {
            animation.setAnimationListener(new SimpleAnimationListener(v6AbstractSettingPopup, false));
        }
        return animation;
    }

    private void setupOnLayoutChangeListener() {
        this.mPopupParent.addOnLayoutChangeListener(this.mLayoutChangeListener);
        this.mPopupIndicatorLayout.addOnLayoutChangeListener(this.mLayoutChangeListener);
        this.mPopupIndicator.addOnLayoutChangeListener(this.mLayoutChangeListener);
        this.mModeExitView.addOnLayoutChangeListener(this.mLayoutChangeListener);
        this.mModeExitButton.addOnLayoutChangeListener(this.mLayoutChangeListener);
    }

    private boolean shouldAnimatePopup(V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (((ActivityBase) this.mContext).isPaused()) {
            return false;
        }
        View view = null;
        for (int i = 0; i < this.mPopupParent.getChildCount(); i++) {
            if (this.mPopupParent.getChildAt(i).getVisibility() == 0) {
                view = this.mPopupParent.getChildAt(i);
                if (view != v6AbstractSettingPopup) {
                    return false;
                }
            }
        }
        if (view == null) {
            return true;
        }
        if (v6AbstractSettingPopup == null || v6AbstractSettingPopup != view) {
            return false;
        }
        return PopupManager.getInstance(this.mContext).getLastOnOtherPopupShowedListener() == null;
    }

    private void updatePopupIndicatorImageResource() {
        if (!(this.mPopupIndicator instanceof ImageView)) {
            return;
        }
        if (((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
            ((ImageView) this.mPopupIndicator).setImageResource(C0049R.drawable.ic_popup_indicator_full_screen);
        } else {
            ((ImageView) this.mPopupIndicator).setImageResource(C0049R.drawable.ic_popup_indicator);
        }
    }

    private void updatePopupVisibility(boolean z, boolean z2, boolean z3) {
        int i = 0;
        if (z) {
            this.mModeExitView.show();
        } else {
            this.mModeExitView.hide();
        }
        this.mPopupParent.setVisibility(z2 ? 0 : 4);
        View view = this.mPopupIndicatorLayout;
        if (!z3) {
            i = 4;
        }
        view.setVisibility(i);
    }

    private void updateRotateLayout(int i, View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.removeRule(9);
        layoutParams.removeRule(10);
        layoutParams.removeRule(11);
        layoutParams.removeRule(12);
        switch (i) {
            case 0:
                layoutParams.addRule(10, -1);
                break;
            case 90:
                layoutParams.addRule(9, -1);
                break;
            case 180:
                layoutParams.addRule(12, -1);
                break;
            case 270:
                layoutParams.addRule(11, -1);
                break;
        }
        view.setLayoutParams(layoutParams);
    }

    public void dismissPopup(V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (shouldAnimatePopup(v6AbstractSettingPopup)) {
            v6AbstractSettingPopup.clearAnimation();
            v6AbstractSettingPopup.startAnimation(initAnimation(v6AbstractSettingPopup, false));
            return;
        }
        v6AbstractSettingPopup.dismiss(false);
    }

    public void inflatePanoramaView() {
        if (this.mPanoramaViewRoot == null) {
            ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(C0049R.layout.pano_view, this);
            this.mPanoramaViewRoot = (RelativeLayout) findChildrenById(C0049R.id.pano_capture_view_layout);
        }
    }

    public boolean isPopupShown() {
        return getCurrentPopupShownView() != null;
    }

    public boolean isPreviewPageVisible() {
        return this.mVisible;
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        this.mWarningView.setVisibility(0);
        this.mAsdIndicatorView.setVisibility(8);
        this.mVisible = true;
        updatePopupIndicatorImageResource();
    }

    public void onClick(View view) {
        simplifyPopup(false, true);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPopupParentLayout = (ViewGroup) findChildrenById(C0049R.id.v6_setting_popup_parent_layout);
        this.mPopupParent = (ViewGroup) findChildrenById(C0049R.id.v6_setting_popup_parent);
        this.mTopPopupParent = (TopPopupParent) findChildrenById(C0049R.id.setting_expanded_popup_parent);
        this.mModeExitView = (V6ModeExitView) findChildrenById(C0049R.id.camera_mode_exit_view);
        this.mModeExitButton = findViewById(C0049R.id.camera_mode_exit_button);
        this.mWarningView = (TextView) findViewById(C0049R.id.warning_message);
        this.mWarningMessageLayout = (LinearLayout) findViewById(C0049R.id.warning_message_layout);
        this.mPopupIndicatorLayout = findViewById(C0049R.id.popup_indicator_layout);
        this.mPopupIndicator = findChildrenById(C0049R.id.popup_indicator);
        this.mAsdIndicatorView = (ImageView) findViewById(C0049R.id.asd_indicator_image);
        this.mOrientationParent = (RelativeLayout) findViewById(C0049R.id.orientation_indicator_area_parent);
        this.mOrientationArea = (RotateLayout) findChildrenById(C0049R.id.orientation_indicator_area);
        this.mLyingOriFlag = (OrientationIndicator) findChildrenById(C0049R.id.orientation_indicator);
        this.mStereoButton = (StereoButton) findChildrenById(C0049R.id.stereo_switch_image);
        this.mZoomButton = (ZoomButton) findChildrenById(C0049R.id.zoom_button);
        this.mPortraitButton = (PortraitButton) findChildrenById(C0049R.id.portrait_switch_image);
        this.mPortraitHintTextView = (TextView) findChildrenById(C0049R.id.portrait_hint_text);
        this.mPopupIndicator.setOnClickListener(this);
        setupOnLayoutChangeListener();
    }

    public void onPopupChange() {
        Log.m5v("V6PreviewPage", "onPopupChange");
        this.mPopupVisible = true;
        this.mPopupIndicatorLayout.setVisibility(4);
        this.mZoomButton.updateLayoutLocation();
    }

    public void setOrientation(int i, boolean z) {
        super.setOrientation(i, z);
        updateRotateLayout(i, this.mOrientationArea);
    }

    public void setPopupVisible(boolean z) {
        if (this.mPopupGroupVisible != z) {
            this.mPopupGroupVisible = z;
            updatePopupIndicator();
        }
    }

    public void showPopup(V6AbstractSettingPopup v6AbstractSettingPopup) {
        if (shouldAnimatePopup(v6AbstractSettingPopup)) {
            v6AbstractSettingPopup.show(false);
            v6AbstractSettingPopup.clearAnimation();
            v6AbstractSettingPopup.startAnimation(initAnimation(v6AbstractSettingPopup, true));
            return;
        }
        v6AbstractSettingPopup.show(false);
    }

    public void showPopupWithoutExitView() {
        if (hasCollapsedPopup()) {
            this.mPopupVisible = true;
            updatePopupVisibility(false, true, false);
        }
    }

    public void simplifyPopup(boolean z, boolean z2) {
        Log.m5v("V6PreviewPage", "simplifyPopup: simplify=" + z + ",animation=" + z2);
        if (this.mPopupVisible || !z) {
            if (z && hasCollapsedPopup()) {
                this.mPopupVisible = false;
                if (z2) {
                    hidePopupView();
                } else {
                    updatePopupVisibility(false, false, true);
                }
            } else if (!z) {
                this.mPopupVisible = true;
                if (!z2) {
                    updatePopupVisibility(true, true, false);
                } else if (!(this.mAnimPopup.isStarted() && this.mAnimatorListener.getAnimationType() == AnimationType.EXPAND)) {
                    this.mAnimatorListener.setAnimationType(AnimationType.EXPAND);
                    this.mAnimPopup.setInterpolator(this.mExpandInterpolator);
                    this.mAnimPopup.reverse();
                }
            }
        }
    }

    public void switchWithAnimation(boolean z) {
        Log.m5v("Camera10", "switchWithAnimation: toPreviewPage=" + z + ",popupVisible=" + this.mPopupVisible + ",groupVisible=" + this.mPopupGroupVisible);
        if (z) {
            doViewAnimation(this.mWarningView, true);
            doViewAnimation(this.mOrientationArea, true);
            if (this.mPopupGroupVisible) {
                if (this.mPopupVisible) {
                    doViewAnimation(this.mModeExitView, true);
                    doViewAnimation(this.mPopupParent, true);
                } else if (hasCollapsedPopup()) {
                    doViewAnimation(this.mPopupIndicatorLayout, true);
                }
            }
            this.mStereoButton.updateVisible();
            this.mZoomButton.updateVisible();
            this.mPortraitButton.updateVisible();
        } else {
            doViewAnimation(this.mModeExitView, false);
            doViewAnimation(this.mWarningView, false);
            doViewAnimation(this.mAsdIndicatorView, false);
            doViewAnimation(this.mPopupParent, false);
            doViewAnimation(this.mPopupIndicatorLayout, false);
            doViewAnimation(this.mOrientationArea, false);
            doViewAnimation(this.mStereoButton, false);
            doViewAnimation(this.mZoomButton, false);
            doViewAnimation(this.mPortraitButton, false);
            doViewAnimation(this.mPortraitHintTextView, false);
        }
        this.mTopPopupParent.onPreviewPageShown(z);
        this.mVisible = z;
    }

    public void updateOrientationLayout(boolean z) {
        if (Device.isOrientationIndicatorEnabled()) {
            LayoutParams layoutParams = (LayoutParams) this.mOrientationParent.getLayoutParams();
            if (z != (layoutParams.topMargin != 0)) {
                layoutParams.setMargins(0, z ? getResources().getDimensionPixelSize(C0049R.dimen.top_control_panel_height) : 0, 0, 0);
                this.mOrientationParent.setLayoutParams(layoutParams);
            }
        }
    }

    public void updatePopupIndicator() {
        boolean hasCollapsedPopup = hasCollapsedPopup();
        Log.m5v("V6PreviewPage", "updatePopupIndicator: groupVisible=" + this.mPopupGroupVisible + ",popupVisible=" + this.mPopupVisible + ",hasSettingPopup=" + hasCollapsedPopup);
        if (this.mPopupGroupVisible) {
            boolean z = this.mPopupVisible;
            boolean z2 = this.mPopupVisible;
            if (this.mPopupVisible) {
                hasCollapsedPopup = false;
            }
            updatePopupVisibility(z, z2, hasCollapsedPopup);
            return;
        }
        updatePopupVisibility(false, false, false);
    }
}
