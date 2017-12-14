package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class TopPopupParent extends FrameLayout implements V6FunctionUI {
    public TopPopupParent(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void dismissPopupExcept(View view, boolean z) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (view != childAt && childAt.getVisibility() == 0 && (childAt instanceof V6AbstractSettingPopup)) {
                dismissPopup((V6AbstractSettingPopup) childAt, z);
            }
        }
    }

    public void dismissAllPopup(boolean z) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if ((childAt instanceof V6AbstractSettingPopup) && childAt.getVisibility() == 0) {
                dismissPopup((V6AbstractSettingPopup) childAt, z);
            }
        }
    }

    public void dismissAllPopupExceptSkinBeauty(boolean z) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if ((childAt instanceof V6AbstractSettingPopup) && childAt.getVisibility() == 0) {
                V6AbstractSettingPopup v6AbstractSettingPopup = (V6AbstractSettingPopup) childAt;
                if (!"pref_camera_face_beauty_switch_key".equals(v6AbstractSettingPopup.getKey())) {
                    dismissPopup(v6AbstractSettingPopup, z);
                }
            }
        }
    }

    public void dismissPopup(V6AbstractSettingPopup v6AbstractSettingPopup, boolean z) {
        v6AbstractSettingPopup.dismiss(z);
    }

    public void enableControls(boolean z) {
    }

    public void onCameraOpen() {
        dismissAllPopup(false);
    }

    public void onCreate() {
    }

    public void onPause() {
        dismissAllPopup(false);
    }

    public void onPreviewPageShown(boolean z) {
        if (!z) {
            dismissAllPopup(true);
        }
    }

    public void onResume() {
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
    }

    public void showPopup(V6AbstractSettingPopup v6AbstractSettingPopup, boolean z) {
        dismissPopupExcept(v6AbstractSettingPopup, z);
        v6AbstractSettingPopup.show(z);
    }
}
