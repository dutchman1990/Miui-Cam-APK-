package com.android.camera.effect;

import android.opengl.GLES20;
import android.util.Log;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLId;
import com.android.gallery3d.ui.RawTexture;
import java.util.Locale;

public class FrameBuffer {
    private int[] mFrameBufferID;
    private GLCanvas mGLCanvas;
    private RawTexture mTexture;

    public FrameBuffer(GLCanvas gLCanvas, int i, int i2, int i3) {
        this.mFrameBufferID = new int[1];
        this.mTexture = new RawTexture(i, i2, true);
        this.mTexture.prepare(gLCanvas);
        GLId.glGenFrameBuffers(1, this.mFrameBufferID, 0);
        GLES20.glBindFramebuffer(36160, this.mFrameBufferID[0]);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.mTexture.getId(), 0);
        GLES20.glBindFramebuffer(36160, i3);
        this.mGLCanvas = gLCanvas;
    }

    public FrameBuffer(GLCanvas gLCanvas, RawTexture rawTexture, int i) {
        this.mFrameBufferID = new int[1];
        if (!rawTexture.isLoaded()) {
            rawTexture.prepare(gLCanvas);
        }
        GLId.glGenFrameBuffers(1, this.mFrameBufferID, 0);
        GLES20.glBindFramebuffer(36160, this.mFrameBufferID[0]);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, rawTexture.getId(), 0);
        GLES20.glBindFramebuffer(36160, i);
        this.mTexture = rawTexture;
        this.mGLCanvas = gLCanvas;
    }

    protected void finalize() {
        if (this.mGLCanvas != null) {
            Log.d("FrameBuffer", String.format(Locale.ENGLISH, "Camera delete framebuffer thread = %d", new Object[]{Long.valueOf(Thread.currentThread().getId())}));
            this.mGLCanvas.deleteFrameBuffer(getId());
        }
    }

    public int getHeight() {
        return this.mTexture.getHeight();
    }

    public int getId() {
        return this.mFrameBufferID[0];
    }

    public RawTexture getTexture() {
        return this.mTexture;
    }

    public int getWidth() {
        return this.mTexture.getWidth();
    }
}
