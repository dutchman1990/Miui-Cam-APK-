package com.android.camera.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView.ScaleType;

public class RotateImageView extends TwoStateImageView implements Rotatable {
    private long mAnimationEndTime = 0;
    private long mAnimationStartTime = 0;
    private int mAxisCurrentDegree = 0;
    private boolean mClockwise = false;
    private boolean mEnableAnimation = true;
    private OnRotateFinishedListener mOnRotateFinishedListener = null;
    private boolean mOverturn = false;
    private int mPointCurrentDegree = 0;
    private int mPointStartDegree = 0;
    private int mPointTargetDegree = 0;
    private Runnable mSwitchEnd = new C01441();
    private Bitmap mThumb;
    private TransitionDrawable mThumbTransition;
    private Drawable[] mThumbs;

    class C01441 implements Runnable {
        C01441() {
        }

        public void run() {
            if (RotateImageView.this.mThumbs != null && RotateImageView.this.mThumbs[1] != null) {
                RotateImageView.this.setImageDrawable(RotateImageView.this.mThumbs[1]);
            }
        }
    }

    public interface OnRotateFinishedListener {
        void OnRotateAxisFinished();

        void OnRotatePointFinished();
    }

    public RotateImageView(Context context) {
        super(context);
    }

    public RotateImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        Matrix matrix = new Matrix();
        Camera camera = new Camera();
        if (drawable != null) {
            Rect bounds = drawable.getBounds();
            int i = bounds.right - bounds.left;
            int i2 = bounds.bottom - bounds.top;
            if (i != 0 && i2 != 0) {
                if (!this.mOverturn) {
                    if (this.mPointCurrentDegree != this.mPointTargetDegree) {
                        long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
                        if (currentAnimationTimeMillis < this.mAnimationEndTime) {
                            int i3 = (int) (currentAnimationTimeMillis - this.mAnimationStartTime);
                            int i4 = this.mPointStartDegree;
                            if (!this.mClockwise) {
                                i3 = -i3;
                            }
                            int i5 = i4 + ((i3 * 270) / 1000);
                            this.mPointCurrentDegree = i5 >= 0 ? i5 % 360 : (i5 % 360) + 360;
                            invalidate();
                        } else {
                            this.mPointCurrentDegree = this.mPointTargetDegree;
                            if (this.mOnRotateFinishedListener != null) {
                                this.mOnRotateFinishedListener.OnRotatePointFinished();
                            }
                        }
                    } else {
                        this.mEnableAnimation = true;
                    }
                } else if (this.mAxisCurrentDegree == 180) {
                    this.mOverturn = false;
                    this.mAxisCurrentDegree = 0;
                    if (this.mOnRotateFinishedListener != null) {
                        this.mOnRotateFinishedListener.OnRotateAxisFinished();
                    }
                } else {
                    this.mAxisCurrentDegree += 10;
                    invalidate();
                }
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int width = (getWidth() - paddingLeft) - getPaddingRight();
                int height = (getHeight() - paddingTop) - getPaddingBottom();
                int saveCount = canvas.getSaveCount();
                camera.save();
                if (this.mPointCurrentDegree == 0 || this.mPointCurrentDegree == 180) {
                    camera.rotateY((float) this.mAxisCurrentDegree);
                } else {
                    camera.rotateX((float) this.mAxisCurrentDegree);
                }
                camera.getMatrix(matrix);
                camera.restore();
                matrix.preTranslate((float) (-(i >> 1)), (float) (-(i2 >> 1)));
                matrix.postTranslate((float) (i >> 1), (float) (i2 >> 1));
                canvas.concat(matrix);
                if (getScaleType() == ScaleType.FIT_CENTER && (width < i || height < i2)) {
                    float min = Math.min(((float) width) / ((float) i), ((float) height) / ((float) i2));
                    canvas.scale(min, min, ((float) width) / 2.0f, ((float) height) / 2.0f);
                }
                canvas.translate((float) ((width / 2) + paddingLeft), (float) ((height / 2) + paddingTop));
                canvas.rotate((float) (-this.mPointCurrentDegree));
                canvas.translate((float) ((-i) / 2), (float) ((-i2) / 2));
                drawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    public void setBitmap(Bitmap bitmap) {
        removeCallbacks(this.mSwitchEnd);
        if (bitmap == null) {
            this.mThumb = null;
            this.mThumbs = null;
            setImageResource(C0049R.drawable.ic_thumbnail_background);
            return;
        }
        LayoutParams layoutParams = getLayoutParams();
        this.mThumb = ThumbnailUtils.extractThumbnail(bitmap, (layoutParams.width - getPaddingLeft()) - getPaddingRight(), (layoutParams.height - getPaddingTop()) - getPaddingBottom());
        if (this.mThumbs != null && this.mEnableAnimation && isShown()) {
            this.mThumbs[0] = this.mThumbs[1];
            this.mThumbs[1] = new BitmapDrawable(getContext().getResources(), this.mThumb);
            this.mThumbTransition = new TransitionDrawable(this.mThumbs);
            setImageDrawable(this.mThumbTransition);
            this.mThumbTransition.startTransition(500);
            postDelayed(this.mSwitchEnd, 520);
        } else {
            this.mThumbs = new Drawable[2];
            this.mThumbs[1] = new BitmapDrawable(getContext().getResources(), this.mThumb);
            setImageDrawable(this.mThumbs[1]);
        }
        setVisibility(0);
    }

    public void setOrientation(int i, boolean z) {
        boolean z2 = false;
        i = i >= 0 ? i % 360 : (i % 360) + 360;
        if (i != this.mPointTargetDegree) {
            this.mEnableAnimation = z;
            this.mPointTargetDegree = i;
            if (this.mEnableAnimation) {
                this.mPointStartDegree = this.mPointCurrentDegree;
                this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
                int i2 = this.mPointTargetDegree - this.mPointCurrentDegree;
                if (i2 < 0) {
                    i2 += 360;
                }
                if (i2 > 180) {
                    i2 -= 360;
                }
                if (i2 >= 0) {
                    z2 = true;
                }
                this.mClockwise = z2;
                this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((Math.abs(i2) * 1000) / 270));
            } else {
                this.mPointCurrentDegree = this.mPointTargetDegree;
            }
            invalidate();
        }
    }
}
