package com.android.camera.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.recyclerview.C0049R;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;
import com.android.camera.ActivityBase;
import com.android.camera.Camera;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.LocationManager;
import com.android.camera.OnScreenHint;
import com.android.camera.RotateDialogController;
import com.android.camera.aosp_porting.animation.CubicEaseInOutInterpolator;
import com.android.camera.permission.PermissionManager;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.Storage;

public class ScreenHint {
    private static final CubicEaseInOutInterpolator sCubicEaseInOutInterpolator = new CubicEaseInOutInterpolator();
    private final Activity mActivity;
    private PopupWindow mFrontCameraFirstUseHintPopup;
    private AnimatorListener mPortraitUseHintAnimatorListener = new C01471();
    private Animator mPortraitUseHintHideAnimator;
    private AnimatorSet mPortraitUseHintShowAnimator;
    private OnScreenHint mStorageHint;
    private long mStorageSpace;

    class C01471 extends AnimatorListenerAdapter {

        class C01461 implements OnClickListener {
            C01461() {
            }

            public void onClick(View view) {
                ((ActivityBase) ScreenHint.this.mActivity).getUIController().getPortraitUseHintView().setOnClickListener(null);
                ScreenHint.this.mPortraitUseHintHideAnimator.start();
            }
        }

        C01471() {
        }

        public void onAnimationEnd(Animator animator) {
            if (animator == ScreenHint.this.mPortraitUseHintShowAnimator) {
                ((ActivityBase) ScreenHint.this.mActivity).getUIController().getPortraitUseHintView().setOnClickListener(new C01461());
            } else if (animator == ScreenHint.this.mPortraitUseHintHideAnimator) {
                ((ActivityBase) ScreenHint.this.mActivity).getUIController().getPortraitUseHintView().setAlpha(1.0f);
                ((ActivityBase) ScreenHint.this.mActivity).getUIController().getPortraitUseHintView().setVisibility(8);
            }
        }
    }

    class C01482 implements Runnable {
        C01482() {
        }

        public void run() {
            ScreenHint.this.recordLocation(true);
        }
    }

    class C01493 implements Runnable {
        C01493() {
        }

        public void run() {
            ScreenHint.this.recordLocation(false);
        }
    }

    class C01504 implements OnTouchListener {
        C01504() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            return x < 0 || x >= view.getWidth() || y < 0 || y >= view.getHeight();
        }
    }

    class C01515 implements OnClickListener {
        C01515() {
        }

        public void onClick(View view) {
            ScreenHint.this.mFrontCameraFirstUseHintPopup.dismiss();
        }
    }

    public ScreenHint(Activity activity) {
        this.mActivity = activity;
    }

    private void initPortraitUseHintAnimator() {
        if (this.mPortraitUseHintShowAnimator == null) {
            ObjectAnimator.ofInt(((ActivityBase) this.mActivity).getUIController().getPortraitUseHintView().getBackground(), "alpha", new int[]{0, 216}).setDuration(300);
            Animator ofFloat = ObjectAnimator.ofFloat(((ActivityBase) this.mActivity).getUIController().getPortraitUseHintView().findViewById(C0049R.id.portrait_use_hint_layout), "alpha", new float[]{0.0f, 1.0f});
            ofFloat.setStartDelay(50);
            ofFloat.setDuration(250);
            this.mPortraitUseHintShowAnimator = new AnimatorSet();
            this.mPortraitUseHintShowAnimator.playTogether(new Animator[]{r0, ofFloat});
            this.mPortraitUseHintShowAnimator.setInterpolator(sCubicEaseInOutInterpolator);
            this.mPortraitUseHintShowAnimator.addListener(this.mPortraitUseHintAnimatorListener);
        }
        if (this.mPortraitUseHintHideAnimator == null) {
            this.mPortraitUseHintHideAnimator = ObjectAnimator.ofFloat(((ActivityBase) this.mActivity).getUIController().getPortraitUseHintView(), "alpha", new float[]{1.0f, 0.0f});
            this.mPortraitUseHintHideAnimator.setDuration(400);
            this.mPortraitUseHintHideAnimator.setInterpolator(sCubicEaseInOutInterpolator);
            this.mPortraitUseHintHideAnimator.addListener(this.mPortraitUseHintAnimatorListener);
        }
    }

    private void recordLocation(boolean z) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putBoolean("pref_camera_recordlocation_key", z);
        edit.apply();
        LocationManager.instance().recordLocation(z);
        if (V6ModulePicker.isCameraModule() && CameraSettings.isSupportedPortrait() && CameraSettings.isBackCamera()) {
            showPortraitUseHint();
        }
    }

    private void showPortraitUseHint() {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putBoolean("pref_camera_first_portrait_use_hint_shown_key", false);
        edit.apply();
        initPortraitUseHintAnimator();
        ((ActivityBase) this.mActivity).getUIController().getPortraitUseHintView().findViewById(C0049R.id.portrait_use_hint_layout).setAlpha(0.0f);
        ((ActivityBase) this.mActivity).getUIController().getPortraitUseHintView().setVisibility(0);
        this.mPortraitUseHintShowAnimator.start();
    }

    public void cancelHint() {
        if (this.mStorageHint != null) {
            this.mStorageHint.cancel();
            this.mStorageHint = null;
        }
    }

    public void dismissFrontCameraFirstUseHintPopup() {
        if (this.mFrontCameraFirstUseHintPopup != null) {
            this.mFrontCameraFirstUseHintPopup.dismiss();
            this.mFrontCameraFirstUseHintPopup = null;
        }
    }

    public long getStorageSpace() {
        return Storage.getAvailableSpace();
    }

    public void hideToast() {
        RotateTextToast instance = RotateTextToast.getInstance();
        if (instance != null) {
            instance.show(0, 0);
        }
    }

    public boolean isScreenHintVisible() {
        return this.mStorageHint != null && this.mStorageHint.getHintViewVisibility() == 0;
    }

    public boolean isShowingFrontCameraFirstUseHintPopup() {
        return this.mFrontCameraFirstUseHintPopup != null ? this.mFrontCameraFirstUseHintPopup.isShowing() : false;
    }

    public void showConfirmMessage(int i, int i2) {
        RotateDialogController.showSystemAlertDialog(this.mActivity, this.mActivity.getString(i), this.mActivity.getString(i2), this.mActivity.getString(17039370), null, null, null);
    }

    public void showFirstUseHint() {
        CameraSettingPreferences instance = CameraSettingPreferences.instance();
        boolean z = instance.getBoolean("pref_camera_first_use_hint_shown_key", true);
        if (PermissionManager.checkCameraLocationPermissions()) {
            Editor edit = instance.edit();
            edit.putBoolean("pref_camera_first_use_hint_shown_key", false);
            edit.putBoolean("pref_camera_confirm_location_shown_key", false);
            edit.apply();
        } else {
            z = false;
        }
        boolean z2 = instance.getBoolean("pref_camera_first_portrait_use_hint_shown_key", CameraSettings.isSupportedPortrait());
        if (z || z2) {
            boolean contains = instance.contains("pref_camera_recordlocation_key");
            if (Device.isSupportedGPS() && !contains && z) {
                RotateDialogController.showSystemChoiceDialog(this.mActivity, this.mActivity.getString(C0049R.string.confirm_location_title), this.mActivity.getString(C0049R.string.confirm_location_message), this.mActivity.getString(C0049R.string.confirm_location_alert), this.mActivity.getString(C0049R.string.start_capture), new C01482(), new C01493());
            } else if (z2 && V6ModulePicker.isCameraModule() && CameraSettings.isBackCamera()) {
                showPortraitUseHint();
            }
        }
    }

    public void showFrontCameraFirstUseHintPopup() {
        if (this.mFrontCameraFirstUseHintPopup == null) {
            View inflate = View.inflate(this.mActivity, C0049R.layout.front_camera_hint_popup, null);
            this.mFrontCameraFirstUseHintPopup = new PopupWindow(inflate, -2, -2, true);
            this.mFrontCameraFirstUseHintPopup.setTouchInterceptor(new C01504());
            inflate.findViewById(C0049R.id.front_camera_hint_text_confirm).setOnClickListener(new C01515());
            ((AnimationDrawable) inflate.findViewById(C0049R.id.front_camera_hint_animation).getBackground()).start();
            this.mFrontCameraFirstUseHintPopup.showAtLocation(((Camera) this.mActivity).getUIController().getGLView(), 49, 0, this.mActivity.getResources().getDimensionPixelSize(C0049R.dimen.front_camera_hint_popup_margin));
        }
    }

    public void showObjectTrackHint(CameraSettingPreferences cameraSettingPreferences) {
        Editor edit = cameraSettingPreferences.edit();
        edit.putBoolean("pref_camera_first_tap_screen_hint_shown_key", false);
        edit.apply();
        RotateTextToast.getInstance(this.mActivity).show(C0049R.string.object_track_enable_toast, 0);
    }

    public void updateHint() {
        Storage.switchStoragePathIfNeeded();
        this.mStorageSpace = Storage.getAvailableSpace();
        CharSequence charSequence = null;
        if (this.mStorageSpace == -1) {
            charSequence = this.mActivity.getString(C0049R.string.no_storage);
        } else if (this.mStorageSpace == -2) {
            charSequence = this.mActivity.getString(C0049R.string.preparing_sd);
        } else if (this.mStorageSpace == -3) {
            charSequence = this.mActivity.getString(C0049R.string.access_sd_fail);
        } else if (this.mStorageSpace < 52428800) {
            charSequence = Storage.isPhoneStoragePriority() ? this.mActivity.getString(C0049R.string.spaceIsLow_content_primary_storage_priority) : this.mActivity.getString(C0049R.string.spaceIsLow_content_external_storage_priority);
        }
        if (charSequence != null) {
            if (this.mStorageHint == null) {
                this.mStorageHint = OnScreenHint.makeText(this.mActivity, charSequence);
            } else {
                this.mStorageHint.setText(charSequence);
            }
            this.mStorageHint.show();
        } else if (this.mStorageHint != null) {
            this.mStorageHint.cancel();
            this.mStorageHint = null;
        }
    }
}
