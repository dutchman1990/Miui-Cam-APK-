package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import com.android.camera.ActivityBase;

public class BottomControlPanel extends V6RelativeLayout {
    private View mBackground;
    private RelativeLayout mControl;
    private boolean mControlVisible;
    public CaptureControlPanel mIntentControlPanel;
    private RelativeLayout mLowerControlGroup;
    public BottomControlLowerPanel mLowerPanel;
    public BottomControlUpperPanel mUpperPanel;

    public BottomControlPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void animateIn(Runnable runnable) {
        if (this.mControl.getVisibility() != 0 || !this.mControlVisible) {
            if (this.mControl.getVisibility() != 0) {
                this.mControl.setVisibility(0);
                if (!V6ModulePicker.isPanoramaModule()) {
                    setBackgroundVisible(((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen());
                }
            }
            this.mControl.animate().withLayer().alpha(1.0f).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(runnable).start();
            this.mControlVisible = true;
        }
    }

    public void animateOut(final Runnable runnable) {
        this.mControlVisible = false;
        if (this.mControl.getVisibility() == 0) {
            this.mControl.animate().withLayer().alpha(0.0f).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                public void run() {
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!BottomControlPanel.this.mControlVisible) {
                        BottomControlPanel.this.mControl.setVisibility(8);
                        BottomControlPanel.this.setBackgroundVisible(false);
                    }
                    BottomControlPanel.this.mControl.setAlpha(1.0f);
                }
            }).start();
        }
    }

    public RelativeLayout getLowerGroup() {
        return this.mLowerControlGroup;
    }

    public V6BottomAnimationImageView getReviewCanceledView() {
        return this.mIntentControlPanel.getReviewCanceledView();
    }

    public V6BottomAnimationImageView getReviewDoneView() {
        return this.mIntentControlPanel.getReviewDoneView();
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        this.mControl.setVisibility(0);
        setBackgroundVisible(((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen());
        this.mControlVisible = getVisibility() == 0;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mUpperPanel = (BottomControlUpperPanel) findChildrenById(C0049R.id.bottom_control_upper_panel);
        this.mLowerPanel = (BottomControlLowerPanel) findChildrenById(C0049R.id.bottom_control_lower_panel);
        this.mIntentControlPanel = (CaptureControlPanel) findChildrenById(C0049R.id.capture_control_panel);
        this.mBackground = getRootView().findViewById(C0049R.id.bottom_control_background);
        this.mControl = (RelativeLayout) findChildrenById(C0049R.id.bottom_control);
        this.mLowerControlGroup = (RelativeLayout) findChildrenById(C0049R.id.lower_control_group);
    }

    public void setBackgroundVisible(boolean z) {
        if (this.mParent != null) {
            View view = this.mBackground;
            int i = (!z || V6ModulePicker.isPanoramaModule()) ? 8 : 0;
            view.setVisibility(i);
        }
    }
}
