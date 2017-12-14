package com.android.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class V6SurfaceManager implements Callback {
    private Activity mActivity;
    private boolean mInitialized;
    private MessageDispacher mMessageDispacher;
    private SurfaceHolder mSurfaceHolder;
    private FrameLayout mSurfaceParent;
    private SurfaceView mSurfaceView;

    public V6SurfaceManager(Context context, FrameLayout frameLayout) {
        this.mActivity = (Activity) context;
        this.mSurfaceParent = frameLayout;
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }

    public void initializeSurfaceView() {
        Log.v("V6SurfaceManager", "initializeSurfaceView mSurfaceView=" + this.mSurfaceView + " mInitialized=" + this.mInitialized);
        this.mSurfaceParent.setVisibility(0);
        if (this.mSurfaceView == null) {
            this.mSurfaceView = (SurfaceView) this.mSurfaceParent.findViewById(C0049R.id.v6_surfaceview);
            if (this.mSurfaceView == null) {
                this.mActivity.getLayoutInflater().inflate(C0049R.layout.v6_surface_view, this.mSurfaceParent);
                this.mSurfaceView = (SurfaceView) this.mSurfaceParent.findViewById(C0049R.id.v6_surfaceview);
            }
            if (!this.mInitialized && this.mSurfaceView != null) {
                this.mSurfaceView.setVisibility(0);
                SurfaceHolder holder = this.mSurfaceView.getHolder();
                holder.addCallback(this);
                holder.setType(3);
                holder.setFormat(-2);
                this.mInitialized = true;
                Log.v("V6SurfaceManager", "Using mdp_preview_content (MDP path)");
                return;
            }
            return;
        }
        this.mSurfaceView.setVisibility(0);
    }

    public boolean isSurfaceViewVisible() {
        return this.mSurfaceView != null && this.mSurfaceView.getVisibility() == 0;
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setSurfaceViewVisible(boolean z) {
        int i = 0;
        this.mSurfaceParent.setVisibility(z ? 0 : 8);
        if (this.mSurfaceView != null) {
            SurfaceView surfaceView = this.mSurfaceView;
            if (!z) {
                i = 8;
            }
            surfaceView.setVisibility(i);
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        Log.v("V6SurfaceManager", "surfaceChanged: width = " + i2 + ", height = " + i3 + " mSurfaceHolder=" + this.mSurfaceHolder + " holder=" + surfaceHolder);
        this.mSurfaceHolder = surfaceHolder;
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.v("V6SurfaceManager", "surfaceCreated");
        this.mSurfaceHolder = surfaceHolder;
        this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_surfaceview, 2, null, null);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.v("V6SurfaceManager", "surfaceDestroyed");
        this.mSurfaceHolder = null;
    }
}
