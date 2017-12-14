package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;

public class V6BottomAnimationViewGroup extends V6RelativeLayout implements AnimationListener {
    private Animation mAnimationIn;
    private Runnable mAnimationInCallback;
    private Animation mAnimationOut;
    private Runnable mAnimationOutCallback;

    public V6BottomAnimationViewGroup(Context context) {
        super(context);
    }

    public V6BottomAnimationViewGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void animateIn(Runnable runnable, int i, boolean z) {
        if (this.mAnimationInCallback != null) {
            this.mAnimationInCallback.run();
            this.mAnimationInCallback = null;
        }
        this.mAnimationInCallback = runnable;
        if (z) {
            setVisibility(0);
        }
        if (getVisibility() == 0) {
            clearAnimation();
            this.mAnimationIn.setDuration((long) i);
            startAnimation(this.mAnimationIn);
        } else if (this.mAnimationInCallback != null) {
            this.mAnimationInCallback.run();
            this.mAnimationInCallback = null;
        }
    }

    public void animateOut(final Runnable runnable, int i, boolean z) {
        if (this.mAnimationOutCallback != null) {
            this.mAnimationOutCallback.run();
            this.mAnimationOutCallback = null;
        }
        if (z) {
            this.mAnimationOutCallback = new Runnable() {
                public void run() {
                    if (runnable != null) {
                        runnable.run();
                    }
                    V6BottomAnimationViewGroup.this.setVisibility(8);
                }
            };
        } else {
            this.mAnimationOutCallback = runnable;
        }
        if (getVisibility() == 0) {
            clearAnimation();
            this.mAnimationOut.setDuration((long) i);
            startAnimation(this.mAnimationOut);
        } else if (this.mAnimationOutCallback != null) {
            this.mAnimationOutCallback.run();
            this.mAnimationOutCallback = null;
        }
    }

    public void onAnimationEnd(Animation animation) {
        if (this.mAnimationIn == animation) {
            if (this.mAnimationInCallback != null) {
                this.mAnimationInCallback.run();
                this.mAnimationInCallback = null;
            }
        } else if (this.mAnimationOut == animation && this.mAnimationOutCallback != null) {
            this.mAnimationOutCallback.run();
            this.mAnimationOutCallback = null;
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAnimationIn = new AlphaAnimation(0.0f, 1.0f);
        this.mAnimationIn.setDuration(200);
        this.mAnimationIn.setInterpolator(new DecelerateInterpolator());
        this.mAnimationIn.setAnimationListener(this);
        this.mAnimationOut = new AlphaAnimation(1.0f, 0.0f);
        this.mAnimationOut.setDuration(200);
        this.mAnimationOut.setInterpolator(new DecelerateInterpolator());
        this.mAnimationOut.setAnimationListener(this);
    }

    public void onResume() {
        super.onResume();
        clearAnimation();
    }
}
