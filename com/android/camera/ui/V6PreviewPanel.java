package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.aosp_porting.FeatureParser;

public class V6PreviewPanel extends V6RelativeLayout implements OnClickListener {
    public RotateTextView mCaptureDelayNumber;
    public RelativeLayout mCaptureDelayNumberParent;
    public V6EffectCropView mCropView;
    public FaceView mFaceView;
    public FocusView mFocusView;
    private Runnable mHidePreviewCover = new C01691();
    private boolean mIsDelayNumInCenter = true;
    private MessageDispacher mMessageDispacher;
    public RotateTextView mMultiSnapNum;
    public ObjectView mObjectView;
    private View mPreviewCover;
    public V6PreviewFrame mPreviewFrame;
    public V6RecordingTimeView mVideoRecordingTimeView;
    public ImageView mVideoReviewImage;
    public RotateImageView mVideoReviewPlay;

    class C01691 implements Runnable {
        C01691() {
        }

        public void run() {
            V6PreviewPanel.this.mPreviewCover.setVisibility(8);
        }
    }

    public V6PreviewPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private int getLeftMarginAccordingDevice() {
        return (int) (((float) Util.sWindowWidth) * FeatureParser.getFloat("camera_front_count_down_margin", 0.0f).floatValue());
    }

    private boolean isDelayNumberInCenter() {
        boolean z = true;
        if (CameraSettings.isBackCamera()) {
            return true;
        }
        if ((!Device.IS_A8 ? Device.IS_D5 : true) != (!Util.isActivityInvert((ActivityBase) this.mContext))) {
            z = false;
        }
        return z;
    }

    private void updateCaptureDelayView(boolean z) {
        if (this.mIsDelayNumInCenter != z) {
            this.mIsDelayNumInCenter = z;
            LayoutParams layoutParams = (LayoutParams) this.mCaptureDelayNumberParent.getLayoutParams();
            int[] rules = layoutParams.getRules();
            if (this.mIsDelayNumInCenter) {
                rules[13] = -1;
                rules[10] = 0;
                layoutParams.leftMargin = 0;
                this.mCaptureDelayNumber.setTextSize(79.67f);
            } else {
                rules[13] = 0;
                rules[10] = -1;
                layoutParams.leftMargin = getLeftMarginAccordingDevice();
                this.mCaptureDelayNumber.setTextSize(50.0f);
            }
            this.mCaptureDelayNumberParent.setLayoutParams(layoutParams);
            this.mCaptureDelayNumber.requestLayout();
        }
    }

    public void hideDelayNumber() {
        this.mCaptureDelayNumber.setVisibility(8);
    }

    public void onCameraOpen() {
        updateCaptureDelayView(isDelayNumberInCenter());
        super.onCameraOpen();
    }

    public void onCapture() {
        this.mPreviewCover.setBackgroundResource(C0049R.color.preview_cover_capture);
        this.mPreviewCover.setVisibility(0);
        removeCallbacks(this.mHidePreviewCover);
        postDelayed(this.mHidePreviewCover, 120);
    }

    public void onClick(View view) {
        if (this.mMessageDispacher != null && this.mVideoReviewPlay == view) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_video_btn_play, 2, Integer.valueOf(0), Integer.valueOf(0));
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFaceView = (FaceView) findChildrenById(C0049R.id.v6_faceview);
        this.mObjectView = (ObjectView) findChildrenById(C0049R.id.object_view);
        this.mCaptureDelayNumber = (RotateTextView) findChildrenById(C0049R.id.v6_capture_delay_number);
        this.mCaptureDelayNumberParent = (RelativeLayout) findChildrenById(C0049R.id.v6_capture_delay_number_parent);
        this.mMultiSnapNum = (RotateTextView) findChildrenById(C0049R.id.v6_multi_snap_number);
        this.mMultiSnapNum.setTypeface(Util.getMiuiTypeface(this.mContext));
        this.mVideoReviewPlay = (RotateImageView) findChildrenById(C0049R.id.v6_video_btn_play);
        this.mVideoRecordingTimeView = (V6RecordingTimeView) findChildrenById(C0049R.id.v6_recording_time_view);
        this.mVideoRecordingTimeView.setTypeface(Util.getMiuiTypeface(this.mContext));
        this.mFocusView = (FocusView) findChildrenById(C0049R.id.v6_focus_view);
        this.mPreviewFrame = (V6PreviewFrame) findChildrenById(C0049R.id.v6_frame_layout);
        this.mCropView = (V6EffectCropView) findChildrenById(C0049R.id.v6_effect_crop_view);
        this.mVideoReviewImage = (ImageView) findViewById(C0049R.id.v6_video_review_image);
        this.mPreviewCover = findViewById(C0049R.id.preview_cover);
        this.mVideoReviewImage.setBackgroundColor(-16777216);
    }

    public void onPause() {
        super.onPause();
        this.mFaceView.clear();
        this.mObjectView.clear();
        removeCallbacks(this.mHidePreviewCover);
        this.mPreviewCover.setVisibility(8);
    }

    public void onResume() {
        super.onResume();
        this.mFaceView.setVisibility(8);
        this.mObjectView.setVisibility(8);
        this.mMultiSnapNum.setVisibility(8);
        this.mCaptureDelayNumber.setVisibility(8);
        this.mVideoReviewImage.setVisibility(8);
        this.mVideoReviewPlay.setVisibility(8);
        this.mVideoRecordingTimeView.setVisibility(8);
        if (((ActivityBase) this.mContext).isScanQRCodeIntent()) {
            setVisibility(4);
        } else {
            setVisibility(0);
        }
        this.mVideoReviewPlay.setOnClickListener(this);
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        super.setMessageDispacher(messageDispacher);
        this.mMessageDispacher = messageDispacher;
    }

    public void setOrientation(int i, boolean z) {
        super.setOrientation(i, z);
        updateCaptureDelayView(isDelayNumberInCenter());
    }

    public void showDelayNumber(String str) {
        if (this.mCaptureDelayNumberParent.getVisibility() != 0) {
            this.mCaptureDelayNumberParent.setVisibility(0);
        }
        if (this.mCaptureDelayNumber.getVisibility() != 0) {
            this.mCaptureDelayNumber.setVisibility(0);
        }
        Util.setNumberText(this.mCaptureDelayNumber, str);
    }
}
