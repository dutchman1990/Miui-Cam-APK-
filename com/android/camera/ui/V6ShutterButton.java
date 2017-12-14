package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;

public class V6ShutterButton extends V6BottomAnimationViewGroup {
    private V6ShutterButtonAudioSound mAudioSound;
    private V6ShutterButtonInternal mShutterButton;

    public V6ShutterButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void changeImageWithAnimation(int i, long j) {
        this.mShutterButton.changeImageWithAnimation(i, j);
    }

    public Drawable getDrawable() {
        return this.mShutterButton.getDrawable();
    }

    public boolean isCanceled() {
        return this.mShutterButton.isCanceled();
    }

    public boolean isEnabled() {
        return this.mShutterButton.isEnabled();
    }

    public boolean isPressed() {
        return this.mShutterButton.isPressed();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mShutterButton = (V6ShutterButtonInternal) findChildrenById(C0049R.id.v6_shutter_button_internal);
        this.mAudioSound = (V6ShutterButtonAudioSound) findChildrenById(C0049R.id.v6_shutter_button_audio_sound);
        if (this.mAudioSound != null) {
            this.mAudioSound.setRadius(this.mShutterButton.getDrawable().getIntrinsicWidth() / 2, getResources().getDimensionPixelSize(C0049R.dimen.bottom_control_lower_panel_height) / 2);
        }
    }

    public void onResume() {
        super.onResume();
        setVisibility(0);
    }

    public void setAudioProgress(float f) {
        if (this.mAudioSound != null) {
            this.mAudioSound.setAudioProgress(f);
        }
    }

    public void setEnabled(boolean z) {
        this.mShutterButton.setEnabled(z);
    }

    public void setImageDrawable(Drawable drawable) {
        this.mShutterButton.setImageDrawable(drawable);
    }

    public void setImageResource(int i) {
        this.mShutterButton.setImageResource(i);
    }
}
