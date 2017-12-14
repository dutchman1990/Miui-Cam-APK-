package com.android.camera.effect.renders;

import android.opengl.GLES20;
import com.android.camera.effect.EffectController.EffectRectAttribute;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.gallery3d.ui.GLCanvas;

public abstract class Render {
    protected GLCanvas mGLCanvas;
    protected int mId = -1;
    protected int mJpegOrientation;
    protected boolean mMirror;
    protected int mOldViewportHeight;
    protected int mOldViewportWidth;
    protected int mOrientation;
    protected int mParentFrameBufferId;
    protected int mPreviewHeight;
    protected int mPreviewWidth;
    protected float mShootRotation;
    protected int mSnapshotOriginHeight;
    protected int mSnapshotOriginWidth;
    protected int mViewportHeight;
    protected int mViewportWidth;

    public Render(GLCanvas gLCanvas) {
        this.mGLCanvas = gLCanvas;
        this.mParentFrameBufferId = 0;
    }

    public Render(GLCanvas gLCanvas, int i) {
        this.mGLCanvas = gLCanvas;
        this.mParentFrameBufferId = 0;
        this.mId = i;
    }

    public abstract boolean draw(DrawAttribute drawAttribute);

    public int getId() {
        return this.mId;
    }

    public void setEffectRangeAttribute(EffectRectAttribute effectRectAttribute) {
    }

    public void setJpegOrientation(int i) {
        this.mJpegOrientation = i;
    }

    public void setMirror(boolean z) {
        this.mMirror = z;
    }

    public void setOrientation(int i) {
        this.mOrientation = i;
    }

    protected void setParentFrameBufferId(int i) {
        this.mParentFrameBufferId = i;
    }

    public void setPreviewSize(int i, int i2) {
        this.mPreviewWidth = i;
        this.mPreviewHeight = i2;
    }

    public void setShootRotation(float f) {
        this.mShootRotation = f;
    }

    public void setSnapshotSize(int i, int i2) {
        this.mSnapshotOriginWidth = i;
        this.mSnapshotOriginHeight = i2;
    }

    public void setViewportSize(int i, int i2) {
        this.mViewportWidth = i;
        this.mViewportHeight = i2;
        if (this.mOldViewportWidth == 0) {
            this.mOldViewportWidth = i;
            this.mOldViewportHeight = i2;
        }
    }

    protected void updateViewport() {
        GLES20.glViewport(0, 0, this.mViewportWidth, this.mViewportHeight);
        this.mGLCanvas.getState().ortho(0.0f, (float) this.mViewportWidth, 0.0f, (float) this.mViewportHeight);
    }
}
