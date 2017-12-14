package com.android.camera.effect;

import android.opengl.GLES20;
import com.android.camera.Device;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.camera.effect.renders.BasicRender;
import com.android.camera.effect.renders.RenderGroup;
import com.android.gallery3d.ui.BasicTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLId;
import com.android.gallery3d.ui.IntArray;
import java.util.ArrayList;

public class SnapshotCanvas implements GLCanvas {
    private final int BASIC_RENDER_INDEX = 1;
    private final int EFFECT_GROUP_INDEX = 0;
    private final IntArray mDeleteBuffers = new IntArray();
    private final IntArray mDeleteFrameBuffers = new IntArray();
    private final ArrayList<Integer> mDeletePrograms = new ArrayList();
    private final IntArray mDeleteTextures = new IntArray();
    private RenderGroup mEffectRenders = EffectController.getInstance().getEffectGroup(this, null, false, true, -1);
    private int mHeight;
    private RenderGroup mRenderGroup = new RenderGroup(this);
    private GLCanvasState mState = new GLCanvasState();
    private int mWidth;

    public SnapshotCanvas() {
        this.mRenderGroup.addRender(this.mEffectRenders);
        this.mRenderGroup.addRender(new BasicRender(this));
        initialize();
    }

    private void initialize() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClearStencil(0);
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        GLES20.glPixelStorei(3317, 1);
        GLES20.glPixelStorei(3333, 1);
    }

    public void beginBindFrameBuffer(FrameBuffer frameBuffer) {
        this.mRenderGroup.beginBindFrameBuffer(frameBuffer);
    }

    public void clearBuffer() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(16384);
    }

    public void deleteFrameBuffer(int i) {
        synchronized (this.mDeleteFrameBuffers) {
            this.mDeleteFrameBuffers.add(i);
        }
    }

    public void deleteProgram(int i) {
        synchronized (this.mDeletePrograms) {
            this.mDeletePrograms.add(Integer.valueOf(i));
        }
    }

    public boolean deleteTexture(int i) {
        synchronized (this.mDeleteTextures) {
            if (i == 0) {
                return false;
            }
            this.mDeleteTextures.add(i);
            return true;
        }
    }

    public boolean deleteTexture(BasicTexture basicTexture) {
        synchronized (this.mDeleteTextures) {
            if (basicTexture.isLoaded()) {
                this.mDeleteTextures.add(basicTexture.getId());
                return true;
            }
            return false;
        }
    }

    public void draw(DrawAttribute drawAttribute) {
        this.mRenderGroup.draw(drawAttribute);
    }

    public void endBindFrameBuffer() {
        this.mRenderGroup.endBindFrameBuffer();
    }

    public BasicRender getBasicRender() {
        return (BasicRender) this.mRenderGroup.getRender(1);
    }

    public RenderGroup getEffectRenderGroup() {
        return (RenderGroup) this.mRenderGroup.getRender(0);
    }

    public int getHeight() {
        return this.mHeight;
    }

    public GLCanvasState getState() {
        return this.mState;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void prepareBlurRenders() {
    }

    public void prepareEffectRenders(boolean z, int i) {
        if (Device.isSupportedShaderEffect() && this.mEffectRenders.isNeedInit(i)) {
            EffectController.getInstance().getEffectGroup(this, this.mEffectRenders, z, true, i);
        }
    }

    public void recycledResources() {
        synchronized (this.mDeleteTextures) {
            IntArray intArray = this.mDeleteTextures;
            if (intArray.size() > 0) {
                GLId.glDeleteTextures(intArray.size(), intArray.getInternalArray(), 0);
                intArray.clear();
            }
            intArray = this.mDeleteBuffers;
            if (intArray.size() > 0) {
                GLId.glDeleteBuffers(intArray.size(), intArray.getInternalArray(), 0);
                intArray.clear();
            }
            intArray = this.mDeleteFrameBuffers;
            if (intArray.size() > 0) {
                GLId.glDeleteFrameBuffers(intArray.size(), intArray.getInternalArray(), 0);
                intArray.clear();
            }
            while (this.mDeletePrograms.size() > 0) {
                GLES20.glDeleteProgram(((Integer) this.mDeletePrograms.remove(0)).intValue());
            }
        }
    }

    public void setPreviewSize(int i, int i2) {
        this.mRenderGroup.setPreviewSize(i, i2);
    }

    public void setSize(int i, int i2) {
        this.mRenderGroup.setViewportSize(i, i2);
        this.mRenderGroup.setPreviewSize(i, i2);
        this.mState.indentityAllM();
        this.mState.setAlpha(1.0f);
        this.mState.translate(0.0f, (float) i2, 0.0f);
        this.mState.scale(1.0f, -1.0f, 1.0f);
    }
}
