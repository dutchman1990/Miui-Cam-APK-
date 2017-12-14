package com.android.camera.ui;

import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.recyclerview.C0049R;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.camera.Camera;
import com.android.camera.Device;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.preferences.PreferenceInflater;

public class UIController extends V6ModuleUI implements MessageDispacher {
    public BottomControlPanel mBottomControlPanel;
    public TextView mDebugInfoView;
    public V6EdgeShutterView mEdgeShutterView;
    public V6CameraGLSurfaceView mGLView;
    private TextView mHibernateHintView;
    private MutexView mLastMutexView;
    private View mMainContent;
    private Runnable mMutexRecover = new C01591();
    private View mPortraitUseHintView;
    private PreferenceGroup mPreferenceGroup;
    public V6PreviewPage mPreviewPage;
    public V6PreviewPanel mPreviewPanel;
    public V6SettingPage mSettingPage;
    public V6SettingsStatusBar mSettingsStatusBar;
    public V6SmartShutterButton mSmartShutterButton;
    public V6SurfaceViewFrame mSurfaceViewParent;
    public TopControlPanel mTopControlPanel;

    class C01591 implements Runnable {
        C01591() {
        }

        public void run() {
            if (UIController.this.mLastMutexView != null) {
                UIController.this.mLastMutexView.show();
                UIController.this.mLastMutexView = null;
            } else {
                UIController.this.mPreviewPage.updateOrientationLayout(false);
            }
            UIController.this.getPortraitButton().show();
        }
    }

    class C01602 implements OnApplyWindowInsetsListener {
        C01602() {
        }

        public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
            int systemWindowInsetBottom = windowInsetsCompat.getSystemWindowInsetBottom();
            RelativeLayout bottomControlLowerGroup = UIController.this.getBottomControlLowerGroup();
            ((LayoutParams) bottomControlLowerGroup.getLayoutParams()).bottomMargin = systemWindowInsetBottom;
            bottomControlLowerGroup.requestLayout();
            if (V6ModulePicker.isPanoramaModule()) {
                UIController.this.getModeExitView().setLayoutParameters(0, UIController.this.mActivity.getResources().getDimensionPixelSize(C0049R.dimen.pano_mode_exit_button_margin_bottom) - (systemWindowInsetBottom > 0 ? 0 : Util.getNavigationBarHeight(UIController.this.mActivity) / 2));
            }
            return ViewCompat.onApplyWindowInsets(view, windowInsetsCompat);
        }
    }

    public UIController(Camera camera) {
        super(camera);
    }

    private void hideModeSetting() {
        enableControls(false);
        getModeButton().updateRemind();
        this.mSettingPage.dismiss();
    }

    private void onDismissModeSetting() {
        this.mActivity.setBlurFlag(false);
        if (!V6ModulePicker.isPanoramaModule()) {
            getBottomControlUpperPanel().animateIn(null);
        }
        this.mBottomControlPanel.animateIn(null);
        this.mTopControlPanel.animateIn(null);
        getPreviewPage().switchWithAnimation(true);
        this.mPreviewPanel.setVisibility(0);
        this.mSettingsStatusBar.setVisibility(0);
        enableControls(true);
        dispacherMessage(0, C0049R.id.hide_mode_animation_done, 2, null, null);
    }

    private void onMutexViewHide(int i) {
        switch (i) {
            case C0049R.id.v6_flash_mode_button:
            case C0049R.id.v6_hdr:
            case C0049R.id.skin_beatify_button:
                this.mPreviewPage.postDelayed(this.mMutexRecover, 150);
                return;
            case C0049R.id.v6_setting_status_bar:
                this.mPreviewPage.updateOrientationLayout(false);
                getPortraitButton().show();
                return;
            default:
                return;
        }
    }

    private void onMutexViewShow(int i) {
        switch (i) {
            case C0049R.id.v6_flash_mode_button:
            case C0049R.id.v6_hdr:
            case C0049R.id.skin_beatify_button:
                if (getSettingsStatusBar().isSubViewShown()) {
                    this.mLastMutexView = getSettingsStatusBar();
                    this.mLastMutexView.hide();
                } else {
                    this.mLastMutexView = null;
                }
                getPortraitButton().hide();
                break;
            case C0049R.id.v6_setting_status_bar:
                getTopPopupParent().dismissAllPopup(true);
                getPortraitButton().hide();
                break;
        }
        this.mPreviewPage.removeCallbacks(this.mMutexRecover);
        this.mPreviewPage.updateOrientationLayout(true);
    }

    private void onShowModeSetting() {
        enableControls(true);
    }

    private void showModeSetting() {
        this.mActivity.setBlurFlag(true);
        getPreviewPage().switchWithAnimation(false);
        this.mTopControlPanel.onShowModeSettings();
        this.mBottomControlPanel.animateOut(null);
        this.mTopControlPanel.animateOut(null);
        if (!getPreviewFrame().isFullScreen()) {
            this.mGLView.setVisibility(0);
        }
        this.mEdgeShutterView.setVisibility(4);
        this.mPreviewPanel.setVisibility(8);
        this.mSettingsStatusBar.setVisibility(8);
        getSettingPage().show();
        enableControls(false);
        dispacherMessage(0, C0049R.id.mode_button, 2, null, null);
        this.mPreviewPage.removeCallbacks(this.mMutexRecover);
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        switch (i3) {
            case 1:
                return this.mActivity.handleMessage(i, i2, obj, obj2);
            case 2:
                return this.mActivity.getCurrentModule().handleMessage(i, i2, obj, obj2);
            case 3:
                return handleMessage(i, i2, obj, obj2);
            default:
                return false;
        }
    }

    public ImageView getAsdIndicator() {
        return this.mPreviewPage.mAsdIndicatorView;
    }

    public RelativeLayout getBottomControlLowerGroup() {
        return this.mBottomControlPanel.getLowerGroup();
    }

    public BottomControlLowerPanel getBottomControlLowerPanel() {
        return this.mBottomControlPanel.mLowerPanel;
    }

    public BottomControlPanel getBottomControlPanel() {
        return this.mBottomControlPanel;
    }

    public BottomControlUpperPanel getBottomControlUpperPanel() {
        return this.mBottomControlPanel.mUpperPanel;
    }

    public View getCaptureProgressBar() {
        return getBottomControlLowerPanel().getProgressBar();
    }

    public V6EdgeShutterView getEdgeShutterView() {
        return this.mEdgeShutterView;
    }

    public EffectButton getEffectButton() {
        return getBottomControlUpperPanel().getEffectButton();
    }

    public V6EffectCropView getEffectCropView() {
        return this.mPreviewPanel.mCropView;
    }

    public FaceView getFaceView() {
        return this.mPreviewPanel.mFaceView;
    }

    public FlashButton getFlashButton() {
        return this.mTopControlPanel.getFlashButton();
    }

    public FocusView getFocusView() {
        return this.mPreviewPanel.mFocusView;
    }

    public V6CameraGLSurfaceView getGLView() {
        return this.mGLView;
    }

    public HdrButton getHdrButton() {
        return this.mTopControlPanel.getHdrButton();
    }

    public TextView getHibernateHintView() {
        return this.mHibernateHintView;
    }

    public ModeButton getModeButton() {
        return this.mBottomControlPanel.mUpperPanel.mModeButton;
    }

    public V6ModeExitView getModeExitView() {
        return this.mPreviewPage.mModeExitView;
    }

    public V6ModulePicker getModulePicker() {
        return getBottomControlLowerPanel().getModulePicker();
    }

    public RotateTextView getMultiSnapNum() {
        return this.mPreviewPanel.mMultiSnapNum;
    }

    public ObjectView getObjectView() {
        return this.mPreviewPanel.mObjectView;
    }

    public OrientationIndicator getOrientationIndicator() {
        return this.mPreviewPage.mLyingOriFlag;
    }

    public RelativeLayout getPanoramaViewRoot() {
        return this.mPreviewPage.mPanoramaViewRoot;
    }

    public V6PauseRecordingButton getPauseRecordingButton() {
        return getBottomControlLowerPanel().getVideoPauseButton();
    }

    public PeakButton getPeakButton() {
        return this.mTopControlPanel.getPeakButton();
    }

    public View getPopupIndicatorLayout() {
        return this.mPreviewPage.mPopupIndicatorLayout;
    }

    public ViewGroup getPopupParent() {
        return this.mPreviewPage.mPopupParent;
    }

    public ViewGroup getPopupParentLayout() {
        return this.mPreviewPage.mPopupParentLayout;
    }

    public PortraitButton getPortraitButton() {
        return this.mPreviewPage.mPortraitButton;
    }

    public TextView getPortraitHintTextView() {
        return this.mPreviewPage.mPortraitHintTextView;
    }

    public View getPortraitUseHintView() {
        return this.mPortraitUseHintView;
    }

    public PreferenceGroup getPreferenceGroup() {
        synchronized (this) {
            if (this.mPreferenceGroup == null) {
                updatePreferenceGroup();
            }
        }
        return this.mPreferenceGroup;
    }

    public V6PreviewFrame getPreviewFrame() {
        return this.mPreviewPanel.mPreviewFrame;
    }

    public V6PreviewPage getPreviewPage() {
        return this.mPreviewPage;
    }

    public V6PreviewPanel getPreviewPanel() {
        return this.mPreviewPanel;
    }

    public V6BottomAnimationImageView getReviewCanceledView() {
        return this.mBottomControlPanel.getReviewCanceledView();
    }

    public V6BottomAnimationImageView getReviewDoneView() {
        return this.mBottomControlPanel.getReviewDoneView();
    }

    public ImageView getReviewImageView() {
        return this.mPreviewPanel.mVideoReviewImage;
    }

    public RotateImageView getReviewPlayView() {
        return this.mPreviewPanel.mVideoReviewPlay;
    }

    public V6SettingButton getSettingButton() {
        return this.mSettingPage.mSettingButton;
    }

    public V6SettingPage getSettingPage() {
        return this.mSettingPage;
    }

    public V6SettingsStatusBar getSettingsStatusBar() {
        return this.mSettingsStatusBar;
    }

    public V6ShutterButton getShutterButton() {
        return getBottomControlLowerPanel().getShutterButton();
    }

    public SkinBeautyButton getSkinBeautyButton() {
        return this.mTopControlPanel.getSkinBeautyButton();
    }

    public V6SmartShutterButton getSmartShutterButton() {
        return this.mSmartShutterButton;
    }

    public StereoButton getStereoButton() {
        return this.mPreviewPage.mStereoButton;
    }

    public V6SurfaceViewFrame getSurfaceViewFrame() {
        return this.mSurfaceViewParent;
    }

    public V6ThumbnailButton getThumbnailButton() {
        return getBottomControlLowerPanel().getThumbnailButton();
    }

    public TopControlPanel getTopControlPanel() {
        return this.mTopControlPanel;
    }

    public TopPopupParent getTopPopupParent() {
        return this.mPreviewPage.mTopPopupParent;
    }

    public V6VideoCaptureButton getVideoCaptureButton() {
        return getBottomControlLowerPanel().getVideoCaptureButton();
    }

    public V6RecordingTimeView getVideoRecordingTimeView() {
        return this.mPreviewPanel.mVideoRecordingTimeView;
    }

    public LinearLayout getWarningMessageParent() {
        return this.mPreviewPage.mWarningMessageLayout;
    }

    public TextView getWarningMessageView() {
        return this.mPreviewPage.mWarningView;
    }

    public ZoomButton getZoomButton() {
        return this.mPreviewPage.mZoomButton;
    }

    public boolean handleMessage(int i, int i2, Object obj, Object obj2) {
        if (i != 4) {
            switch (i2) {
                case C0049R.id.show_mode_animation_done:
                    onShowModeSetting();
                    return true;
                case C0049R.id.hide_mode_animation_done:
                    onDismissModeSetting();
                    return true;
                case C0049R.id.mode_button:
                    showModeSetting();
                    return true;
                case C0049R.id.v6_frame_layout:
                    getModeExitView().updateBackground();
                    return true;
                case C0049R.id.v6_focus_view:
                    if (i != 2) {
                        return false;
                    }
                    getEffectCropView().removeTiltShiftMask();
                    return this.mActivity.getCurrentModule().handleMessage(i, i2, obj, obj2);
                case C0049R.id.dismiss_setting:
                    hideModeSetting();
                    return true;
                default:
                    break;
            }
        } else if (((Boolean) obj).booleanValue()) {
            onMutexViewShow(i2);
        } else {
            onMutexViewHide(i2);
        }
        return false;
    }

    public boolean onBack() {
        if (!this.mSettingPage.isShown()) {
            return false;
        }
        hideModeSetting();
        return true;
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        getPreviewPage().updatePopupIndicator();
    }

    public void onCreate() {
        this.mBottomControlPanel = (BottomControlPanel) findViewById(C0049R.id.bottom_control_panel);
        this.mTopControlPanel = (TopControlPanel) findViewById(C0049R.id.top_control_panel);
        this.mSettingPage = (V6SettingPage) findViewById(C0049R.id.v6_setting_page);
        this.mPreviewPage = (V6PreviewPage) findViewById(C0049R.id.v6_preview_page);
        this.mPreviewPanel = (V6PreviewPanel) findViewById(C0049R.id.v6_preview_panel);
        this.mSmartShutterButton = (V6SmartShutterButton) findViewById(C0049R.id.v6_smart_shutter_button);
        this.mSettingsStatusBar = (V6SettingsStatusBar) findViewById(C0049R.id.v6_setting_status_bar);
        this.mGLView = (V6CameraGLSurfaceView) findViewById(C0049R.id.v6_gl_surface_view);
        this.mSurfaceViewParent = (V6SurfaceViewFrame) findViewById(C0049R.id.v6_surface_view_parent);
        this.mEdgeShutterView = (V6EdgeShutterView) findViewById(C0049R.id.edge_shutter_view);
        this.mDebugInfoView = (TextView) findViewById(C0049R.id.camera_debug_content);
        this.mMainContent = findViewById(C0049R.id.main_content);
        this.mHibernateHintView = (TextView) findViewById(C0049R.id.hibernate_hint_view);
        this.mPortraitUseHintView = findViewById(C0049R.id.portrait_use_hint_cover);
        this.mGLView.setVisibility(0);
        setMessageDispacher(this);
        updatePreferenceGroup();
        super.onCreate();
        if (Util.checkDeviceHasNavigationBar(this.mActivity)) {
            ViewCompat.setOnApplyWindowInsetsListener(this.mMainContent, new C01602());
        }
    }

    public void onPause() {
        super.onPause();
        this.mLastMutexView = null;
    }

    public void onResume() {
        super.onResume();
        EffectController.getInstance().setBlurEffect(false);
        getHibernateHintView().setVisibility(8);
    }

    public void showDebugInfo(String str) {
        if (this.mDebugInfoView != null) {
            this.mDebugInfoView.setText(str);
        }
    }

    public void showDebugView() {
        if (this.mDebugInfoView != null) {
            this.mDebugInfoView.setVisibility(0);
        }
    }

    public void updatePreferenceGroup() {
        this.mPreferenceGroup = (PreferenceGroup) new PreferenceInflater(this.mActivity).inflate(V6ModulePicker.isVideoModule() ? C0049R.xml.video_preferences : C0049R.xml.camera_preferences);
    }

    public void updateThumbnailView(Thumbnail thumbnail) {
        getBottomControlLowerPanel().updateThumbnailView(thumbnail);
    }

    public void useProperView() {
        if (Device.isMDPRender() && V6ModulePicker.isVideoModule()) {
            getSurfaceViewFrame().initSurfaceView();
        } else {
            getGLView().setVisibility(0);
        }
    }
}
