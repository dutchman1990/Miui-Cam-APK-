package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import com.android.camera.ActivityBase;
import com.android.camera.Thumbnail;
import com.android.camera.Util;

public class V6ThumbnailButton extends V6BottomAnimationViewGroup implements OnClickListener {
    private static final int BORDER = Util.dpToPixel(2.0f);
    public RotateImageView mImage;
    private MessageDispacher mMessageDispacher;
    private boolean mValideThumbnail;
    private boolean mVisible;

    public V6ThumbnailButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public void onCameraOpen() {
    }

    public void onClick(View view) {
        if (this.mValideThumbnail) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_thumbnail_button, 2, null, null);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mImage = (RotateImageView) findViewById(C0049R.id.v6_thumbnail_image);
        setOnClickListener(this);
        LayoutParams layoutParams = this.mImage.getLayoutParams();
        int intrinsicWidth = this.mImage.getDrawable().getIntrinsicWidth() - BORDER;
        layoutParams.width = intrinsicWidth;
        layoutParams.height = intrinsicWidth;
        this.mImage.setLayoutParams(layoutParams);
    }

    public void onPause() {
    }

    public void onResume() {
        clearAnimation();
        ActivityBase activityBase = (ActivityBase) this.mContext;
        boolean z = (activityBase.isImageCaptureIntent() || activityBase.isVideoCaptureIntent()) ? false : true;
        this.mVisible = z;
        if (this.mVisible) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setOrientation(int i, boolean z) {
        this.mImage.setOrientation(i, z);
    }

    public void setVisibility(int i) {
        if (!this.mVisible) {
            i = 8;
        }
        super.setVisibility(i);
    }

    public void updateThumbnail(Thumbnail thumbnail) {
        if (thumbnail != null) {
            this.mImage.setBitmap(thumbnail.getBitmap());
            this.mValideThumbnail = true;
            return;
        }
        this.mImage.setBitmap(null);
        this.mValideThumbnail = false;
    }
}
