package com.android.gallery3d.ui;

import android.opengl.GLES20;
import com.android.camera.Device;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.FrameBuffer;
import com.android.camera.effect.GLCanvasState;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.camera.effect.renders.BasicRender;
import com.android.camera.effect.renders.EffectRenderGroup;
import com.android.camera.effect.renders.RenderGroup;
import java.util.ArrayList;

public class GLCanvasImpl implements GLCanvas {
    private static final int PRELOAD_UPPER_BOUND;
    public static int sMaxTextureSize = 4096;
    private final IntArray mDeleteBuffers = new IntArray();
    private final IntArray mDeleteFrameBuffers = new IntArray();
    private final ArrayList<Integer> mDeletePrograms = new ArrayList();
    private final IntArray mDeleteTextures = new IntArray();
    private RenderGroup mEffectRenders;
    private int mHeight;
    private int mPreloadedRenders = 0;
    private RenderGroup mRenderGroup;
    private GLCanvasState mState = new GLCanvasState();
    private int mWidth;

    static {
        int i = 0;
        int i2 = (Device.isSupportedTiltShift() ? 4 : 0) + 26;
        if (Device.isSupportedPeakingMF()) {
            i = 1;
        }
        PRELOAD_UPPER_BOUND = i2 + i;
    }

    public GLCanvasImpl() {
        int[] iArr = new int[1];
        GLES20.glGetIntegerv(3379, iArr, 0);
        sMaxTextureSize = iArr[0];
        this.mRenderGroup = new RenderGroup(this);
        this.mEffectRenders = EffectController.getInstance().getEffectGroup(this, null, false, false, -1);
        this.mRenderGroup.addRender(new EffectRenderGroup(this));
        this.mRenderGroup.addRender(new BasicRender(this));
        initialize();
    }

    private void initialize() {
        GLES20.glEnable(3024);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClearStencil(0);
        GLES20.glLineWidth(1.0f);
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
                this.mDeleteTextures.add(basicTexture.mId);
                return true;
            }
            return false;
        }
    }

    public void draw(DrawAttribute drawAttribute) {
        if (this.mPreloadedRenders < PRELOAD_UPPER_BOUND) {
            prepareEffectRenders(false, -1);
            this.mPreloadedRenders++;
        }
        this.mRenderGroup.draw(drawAttribute);
    }

    public void endBindFrameBuffer() {
        this.mRenderGroup.endBindFrameBuffer();
    }

    public RenderGroup getEffectRenderGroup() {
        return this.mEffectRenders;
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
        if (Device.isSupportedShaderEffect() && this.mRenderGroup.getRender(2) == null) {
            if (this.mEffectRenders.getRender(EffectController.sBackgroundBlurIndex) == null) {
                prepareEffectRenders(false, EffectController.sBackgroundBlurIndex);
            }
            this.mRenderGroup.addRender(this.mEffectRenders.getRender(EffectController.sBackgroundBlurIndex));
        }
    }

    public void prepareEffectRenders(boolean z, int i) {
        if (Device.isSupportedShaderEffect() && this.mEffectRenders.isNeedInit(i)) {
            EffectController.getInstance().getEffectGroup(this, this.mEffectRenders, z, false, i);
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
        boolean z = false;
        if (i >= 0 && i2 >= 0) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mWidth = i;
        this.mHeight = i2;
        this.mRenderGroup.setViewportSize(i, i2);
        this.mRenderGroup.setPreviewSize(i, i2);
        this.mState.indentityAllM();
        this.mState.setAlpha(1.0f);
        this.mState.translate(0.0f, (float) i2, 0.0f);
        this.mState.scale(1.0f, -1.0f, 1.0f);
    }
}
