package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.android.camera.ActivityBase;
import com.android.camera.Device;

public class TopControlPanel extends V6RelativeLayout implements MessageDispacher {
    public SkinBeautyButton mBeautyButton;
    private boolean mControlVisible;
    public FlashButton mFlashButton;
    public HdrButton mHdrButton;
    private MessageDispacher mMessageDispacher;
    public PeakButton mPeakButton;

    public TopControlPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void hideSubViewExcept(View view, boolean z) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (!(childAt == view || childAt.getVisibility() == 8)) {
                if (childAt instanceof AnimateView) {
                    ((AnimateView) childAt).hide(z);
                } else {
                    childAt.setVisibility(8);
                }
            }
        }
    }

    public void animateIn(Runnable runnable) {
        if (getVisibility() != 0 || !this.mControlVisible) {
            if (getVisibility() != 0) {
                setVisibility(0);
            }
            animate().withLayer().alpha(1.0f).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(runnable).start();
            this.mControlVisible = true;
        }
    }

    public void animateOut(final Runnable runnable) {
        this.mControlVisible = false;
        if (getVisibility() == 0) {
            animate().withLayer().alpha(0.0f).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                public void run() {
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!TopControlPanel.this.mControlVisible) {
                        TopControlPanel.this.setVisibility(8);
                    }
                    TopControlPanel.this.setAlpha(1.0f);
                }
            }).start();
        }
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        if (i == 4) {
            if (!((Boolean) obj).booleanValue()) {
                switch (i2) {
                    case C0049R.id.v6_flash_mode_button:
                        if (!this.mHdrButton.couldBeVisible()) {
                            if (!this.mBeautyButton.couldBeVisible()) {
                                if (this.mPeakButton.couldBeVisible()) {
                                    this.mPeakButton.show(true);
                                    break;
                                }
                            }
                            this.mBeautyButton.show(true);
                            break;
                        }
                        this.mHdrButton.overrideSettings(null);
                        this.mHdrButton.show(true);
                        break;
                        break;
                    case C0049R.id.v6_hdr:
                    case C0049R.id.skin_beatify_button:
                        this.mFlashButton.show(true);
                        break;
                    default:
                        break;
                }
            }
            switch (i2) {
                case C0049R.id.v6_flash_mode_button:
                    hideSubViewExcept(this.mFlashButton, true);
                    break;
                case C0049R.id.v6_hdr:
                    hideSubViewExcept(this.mHdrButton, true);
                    break;
                case C0049R.id.skin_beatify_button:
                    hideSubViewExcept(this.mBeautyButton, true);
                    break;
            }
        }
        return this.mMessageDispacher != null ? this.mMessageDispacher.dispacherMessage(i, i2, i3, obj, obj2) : false;
    }

    public FlashButton getFlashButton() {
        return this.mFlashButton;
    }

    public HdrButton getHdrButton() {
        return this.mHdrButton;
    }

    public PeakButton getPeakButton() {
        return this.mPeakButton;
    }

    public SkinBeautyButton getSkinBeautyButton() {
        return this.mBeautyButton;
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        if (((ActivityBase) this.mContext).isScanQRCodeIntent()) {
            setVisibility(4);
        } else if (((ActivityBase) this.mContext).getUIController().getReviewDoneView().getVisibility() == 0 || V6ModulePicker.isPanoramaModule()) {
            setVisibility(8);
        } else {
            setVisibility(0);
        }
        this.mControlVisible = getVisibility() == 0;
        updateBackground();
    }

    protected void onFinishInflate() {
        this.mFlashButton = (FlashButton) findChildrenById(C0049R.id.v6_flash_mode_button);
        this.mHdrButton = (HdrButton) findChildrenById(C0049R.id.v6_hdr);
        this.mBeautyButton = (SkinBeautyButton) findChildrenById(C0049R.id.skin_beatify_button);
        this.mPeakButton = (PeakButton) findChildrenById(C0049R.id.v6_peak);
    }

    public void onShowModeSettings() {
        this.mFlashButton.dismissPopup();
        this.mHdrButton.dismissPopup();
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        super.setMessageDispacher(this);
        this.mMessageDispacher = messageDispacher;
    }

    public void updateBackground() {
        if (!Device.IS_D5) {
            return;
        }
        if (((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
            setBackgroundResource(C0049R.color.fullscreen_background);
        } else {
            setBackgroundResource(17170444);
        }
    }
}
