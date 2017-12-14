package com.android.camera.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.FrameLayout;

public class V6SurfaceViewFrame extends FrameLayout implements OnLayoutChangeListener, V6FunctionUI {
    private MessageDispacher mMessageDispacher;
    private V6SurfaceManager mSurfaceManager;

    public V6SurfaceViewFrame(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void enableControls(boolean z) {
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceManager.getSurfaceHolder();
    }

    public void initSurfaceView() {
        this.mSurfaceManager.initializeSurfaceView();
    }

    public boolean isSurfaceViewAvailable() {
        return isSurfaceViewVisible() && getSurfaceHolder() != null;
    }

    public boolean isSurfaceViewVisible() {
        return this.mSurfaceManager.isSurfaceViewVisible();
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        addOnLayoutChangeListener(this);
        this.mSurfaceManager = new V6SurfaceManager(this.mContext, this);
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_frame_layout, 2, view, new Rect(i, i2, i3, i4));
            this.mMessageDispacher.dispacherMessage(1, C0049R.id.v6_frame_layout, 3, view, new Rect(i, i2, i3, i4));
        }
    }

    public void onPause() {
    }

    public void onResume() {
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_frame_layout, 2, new Point(i, i2), new Point(i3, i4));
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
        this.mSurfaceManager.setMessageDispacher(messageDispacher);
    }

    public void setSurfaceViewVisible(boolean z) {
        this.mSurfaceManager.setSurfaceViewVisible(z);
    }
}
