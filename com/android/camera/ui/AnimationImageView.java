package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationImageView extends RotateImageView implements V6FunctionUI, AnimateView {
    private boolean mIsEnable;
    protected MessageDispacher mMessageDispacher;

    private class AnimationImageViewListener extends SimpleAnimationListener {
        public AnimationImageViewListener(AnimationImageView animationImageView, boolean z) {
            super(animationImageView, z);
        }

        public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            AnimationImageView.this.setEnabled(AnimationImageView.this.mIsEnable);
        }

        public void onAnimationStart(Animation animation) {
            super.onAnimationStart(animation);
            AnimationImageView.this.setEnabled(false);
        }
    }

    public AnimationImageView(Context context) {
        super(context);
    }

    public AnimationImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private Animation initAnimation(boolean z) {
        if (z) {
            Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.show);
            loadAnimation.setAnimationListener(new AnimationImageViewListener(this, true));
            return loadAnimation;
        }
        loadAnimation = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.dismiss);
        loadAnimation.setAnimationListener(new AnimationImageViewListener(this, false));
        return loadAnimation;
    }

    public void enableControls(boolean z) {
        if (getAnimation() == null || getAnimation().hasEnded()) {
            setEnabled(z);
        }
        this.mIsEnable = z;
    }

    public void hide(boolean z) {
        if (z) {
            clearAnimation();
            startAnimation(initAnimation(false));
            setEnabled(false);
            return;
        }
        setVisibility(8);
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void show(boolean z) {
        setVisibility(0);
        if (z) {
            clearAnimation();
            startAnimation(initAnimation(true));
            setEnabled(false);
        }
    }
}
