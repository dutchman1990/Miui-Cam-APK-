package com.android.camera.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.preferences.CameraSettingPreferences;

public class V6PreviewFrame extends RelativeLayout implements OnLayoutChangeListener, V6FunctionUI {
    private float mAspectRatio = 1.7777778f;
    private MessageDispacher mMessageDispacher;
    public SplitLineDrawer mReferenceGrid;

    public V6PreviewFrame(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private boolean isReferenceLineEnabled() {
        return CameraSettingPreferences.instance().getBoolean("pref_camera_referenceline_key", false);
    }

    public void enableControls(boolean z) {
    }

    public boolean isFullScreen() {
        return Math.abs(this.mAspectRatio - CameraSettings.getPreviewAspectRatio(Util.sWindowHeight, Util.sWindowWidth)) < 0.1f || Math.abs(((double) this.mAspectRatio) - 1.5d) < 0.10000000149011612d;
    }

    public void onCameraOpen() {
        updateRefenceLineAccordSquare();
        updatePreviewGrid();
    }

    public void onCreate() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mReferenceGrid = (SplitLineDrawer) findViewById(C0049R.id.v6_reference_grid);
        this.mReferenceGrid.initialize(3, 3);
        this.mReferenceGrid.setBorderVisible(false, false);
        this.mReferenceGrid.setLineColor(-2130706433);
        addOnLayoutChangeListener(this);
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i3 - i <= i4 - i2) {
            this.mAspectRatio = CameraSettings.getPreviewAspectRatio(i4 - i2, i3 - i);
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_frame_layout, 2, view, new Rect(i, i2, i3, i4));
                this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_frame_layout, 3, view, new Rect(i, i2, i3, i4));
            }
        }
    }

    public void onPause() {
    }

    public void onResume() {
        if (!(getWidth() == 0 || getHeight() == 0)) {
            this.mAspectRatio = CameraSettings.getPreviewAspectRatio(getHeight(), getWidth());
        }
        if (!V6ModulePicker.isCameraModule()) {
            this.mReferenceGrid.setVisibility(8);
        }
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_frame_layout, 2, new Point(i, i2), new Point(i3, i4));
        }
    }

    public void setAspectRatio(float f) {
        if (((double) f) <= 0.0d) {
            throw new IllegalArgumentException();
        } else if (((double) Math.abs(this.mAspectRatio - f)) > 0.01d) {
            this.mAspectRatio = f;
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void updatePreviewGrid() {
        if (isReferenceLineEnabled() && !((ActivityBase) getContext()).isScanQRCodeIntent() && V6ModulePicker.isCameraModule()) {
            this.mReferenceGrid.setVisibility(0);
        } else {
            this.mReferenceGrid.setVisibility(8);
        }
    }

    public void updateRefenceLineAccordSquare() {
        LayoutParams layoutParams = (LayoutParams) this.mReferenceGrid.getLayoutParams();
        if (CameraSettings.isSwitchOn("pref_camera_square_mode_key")) {
            int i = Util.sWindowWidth / 6;
            layoutParams.topMargin = i;
            layoutParams.bottomMargin = i;
        } else {
            layoutParams.topMargin = 0;
            layoutParams.bottomMargin = 0;
        }
        if (this.mReferenceGrid.getVisibility() == 0) {
            this.mReferenceGrid.requestLayout();
        }
    }
}
