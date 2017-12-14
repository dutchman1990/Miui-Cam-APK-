package com.android.camera.effect.renders;

import android.util.Log;
import com.android.camera.Device;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.FrameBuffer;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.camera.effect.draw_mode.DrawBasicTexAttribute;
import com.android.camera.effect.draw_mode.DrawExtTexAttribute;
import com.android.camera.effect.draw_mode.DrawIntTexAttribute;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.RawTexture;
import com.android.gallery3d.ui.Utils;
import java.util.ArrayList;
import java.util.Locale;

public final class PipeRenderPair extends RenderGroup {
    private DrawBasicTexAttribute mBasicTexureAttri = new DrawBasicTexAttribute();
    private FrameBuffer mBlurFrameBuffer;
    private int mBufferHeight = -1;
    private int mBufferWidth = -1;
    private DrawExtTexAttribute mExtTexture = new DrawExtTexAttribute();
    private Render mFirstRender;
    private FrameBuffer mFrameBuffer;
    private ArrayList<FrameBuffer> mFrameBuffers = new ArrayList();
    private FrameBuffer mMiddleFrameBuffer;
    private Render mSecondRender;
    private boolean mTextureFilled = false;
    private boolean mUseMiddleBuffer = false;

    public PipeRenderPair(GLCanvas gLCanvas) {
        super(gLCanvas);
    }

    public PipeRenderPair(GLCanvas gLCanvas, Render render, Render render2, boolean z) {
        super(gLCanvas);
        setRenderPairs(render, render2);
        this.mUseMiddleBuffer = z;
    }

    private int getEffectBufferRatio() {
        return EffectController.getInstance().isDisplayShow() ? 2 : 1;
    }

    public void addRender(Render render) {
        throw new RuntimeException("Not supportted addRender in PipeRenderPair !");
    }

    public void copyBlurTexture(DrawExtTexAttribute drawExtTexAttribute) {
        if (EffectController.getInstance().isBackGroundBlur() && !this.mTextureFilled) {
            if (this.mBlurFrameBuffer != null && this.mBlurFrameBuffer.getWidth() == drawExtTexAttribute.mWidth) {
                if (this.mBlurFrameBuffer.getHeight() != drawExtTexAttribute.mHeight) {
                }
                beginBindFrameBuffer(this.mBlurFrameBuffer);
                this.mSecondRender.draw(this.mBasicTexureAttri.init(this.mUseMiddleBuffer ? this.mMiddleFrameBuffer.getTexture() : this.mFrameBuffer.getTexture(), drawExtTexAttribute.mX, drawExtTexAttribute.mY, drawExtTexAttribute.mWidth, drawExtTexAttribute.mHeight));
                endBindFrameBuffer();
                this.mTextureFilled = true;
            }
            this.mBlurFrameBuffer = new FrameBuffer(this.mGLCanvas, drawExtTexAttribute.mWidth, drawExtTexAttribute.mHeight, this.mParentFrameBufferId);
            beginBindFrameBuffer(this.mBlurFrameBuffer);
            if (this.mUseMiddleBuffer) {
            }
            this.mSecondRender.draw(this.mBasicTexureAttri.init(this.mUseMiddleBuffer ? this.mMiddleFrameBuffer.getTexture() : this.mFrameBuffer.getTexture(), drawExtTexAttribute.mX, drawExtTexAttribute.mY, drawExtTexAttribute.mWidth, drawExtTexAttribute.mHeight));
            endBindFrameBuffer();
            this.mTextureFilled = true;
        }
    }

    public boolean draw(DrawAttribute drawAttribute) {
        if (this.mRenders.size() == 0) {
            return false;
        }
        if (this.mRenders.size() == 1 || this.mFirstRender == this.mSecondRender) {
            return ((Render) this.mRenders.get(0)).draw(drawAttribute);
        }
        if (drawAttribute.getTarget() == 8) {
            DrawExtTexAttribute drawExtTexAttribute = (DrawExtTexAttribute) drawAttribute;
            this.mFrameBuffer = getFrameBuffer(this.mPreviewWidth / getEffectBufferRatio(), this.mPreviewHeight / getEffectBufferRatio());
            beginBindFrameBuffer(this.mFrameBuffer);
            this.mFirstRender.draw(this.mExtTexture.init(drawExtTexAttribute.mExtTexture, drawExtTexAttribute.mTextureTransform, 0, 0, this.mFrameBuffer.getTexture().getTextureWidth(), this.mFrameBuffer.getTexture().getTextureHeight()));
            endBindFrameBuffer();
            if (this.mUseMiddleBuffer) {
                updateMiddleBuffer(this.mPreviewWidth, this.mPreviewHeight);
                this.mMiddleFrameBuffer = getFrameBuffer(this.mBufferWidth, this.mBufferHeight);
                beginBindFrameBuffer(this.mMiddleFrameBuffer);
                this.mFirstRender.draw(this.mExtTexture.init(drawExtTexAttribute.mExtTexture, drawExtTexAttribute.mTextureTransform, 0, 0, this.mBufferWidth, this.mBufferHeight));
                endBindFrameBuffer();
            }
            if (EffectController.getInstance().isMainFrameDisplay()) {
                if (Device.isHoldBlurBackground() && EffectController.getInstance().isBackGroundBlur()) {
                    copyBlurTexture(drawExtTexAttribute);
                    drawBlurTexture(drawExtTexAttribute);
                } else {
                    this.mSecondRender.draw(this.mBasicTexureAttri.init(this.mUseMiddleBuffer ? this.mMiddleFrameBuffer.getTexture() : this.mFrameBuffer.getTexture(), drawExtTexAttribute.mX, drawExtTexAttribute.mY, drawExtTexAttribute.mWidth, drawExtTexAttribute.mHeight));
                }
            }
            return true;
        } else if (drawAttribute.getTarget() == 5 || drawAttribute.getTarget() == 10) {
            DrawBasicTexAttribute drawBasicTexAttribute = (DrawBasicTexAttribute) drawAttribute;
            updateMiddleBuffer(drawBasicTexAttribute.mWidth, drawBasicTexAttribute.mHeight);
            this.mFrameBuffer = getFrameBuffer(this.mBufferWidth, this.mBufferHeight);
            beginBindFrameBuffer(this.mFrameBuffer);
            this.mFirstRender.draw(this.mBasicTexureAttri.init(drawBasicTexAttribute.mBasicTexture, 0, 0, this.mFrameBuffer.getTexture().getTextureWidth(), this.mFrameBuffer.getTexture().getTextureHeight()));
            endBindFrameBuffer();
            this.mSecondRender.draw(this.mBasicTexureAttri.init(this.mFrameBuffer.getTexture(), drawBasicTexAttribute.mX, drawBasicTexAttribute.mY, drawBasicTexAttribute.mWidth, drawBasicTexAttribute.mHeight));
            return true;
        } else if (drawAttribute.getTarget() != 6) {
            return false;
        } else {
            DrawIntTexAttribute drawIntTexAttribute = (DrawIntTexAttribute) drawAttribute;
            this.mFrameBuffer = getFrameBuffer(drawIntTexAttribute.mWidth, drawIntTexAttribute.mHeight);
            beginBindFrameBuffer(this.mFrameBuffer);
            this.mFirstRender.draw(new DrawIntTexAttribute(drawIntTexAttribute.mTexId, 0, 0, drawIntTexAttribute.mWidth, drawIntTexAttribute.mHeight));
            endBindFrameBuffer();
            this.mSecondRender.draw(this.mBasicTexureAttri.init(this.mFrameBuffer.getTexture(), drawIntTexAttribute.mX, drawIntTexAttribute.mY, drawIntTexAttribute.mWidth, drawIntTexAttribute.mHeight, true));
            return true;
        }
    }

    public void drawBlurTexture(DrawExtTexAttribute drawExtTexAttribute) {
        if (EffectController.getInstance().isBackGroundBlur() && this.mTextureFilled) {
            this.mGLCanvas.draw(new DrawBasicTexAttribute(this.mBlurFrameBuffer.getTexture(), drawExtTexAttribute.mX, drawExtTexAttribute.mY, drawExtTexAttribute.mWidth, drawExtTexAttribute.mHeight));
        }
    }

    public FrameBuffer getFrameBuffer(int i, int i2) {
        FrameBuffer frameBuffer = null;
        if (!this.mFrameBuffers.isEmpty()) {
            for (int size = this.mFrameBuffers.size() - 1; size >= 0; size--) {
                int width = ((FrameBuffer) this.mFrameBuffers.get(size)).getWidth();
                int height = ((FrameBuffer) this.mFrameBuffers.get(size)).getHeight();
                if ((i < i2 ? Math.abs((((double) height) / ((double) width)) - (((double) i2) / ((double) i))) : Math.abs((((double) width) / ((double) height)) - (((double) i) / ((double) i2)))) <= 0.1d && Utils.nextPowerOf2(width) == Utils.nextPowerOf2(i) && Utils.nextPowerOf2(height) == Utils.nextPowerOf2(i2)) {
                    frameBuffer = (FrameBuffer) this.mFrameBuffers.get(size);
                    break;
                }
            }
        }
        if (frameBuffer == null) {
            frameBuffer = new FrameBuffer(this.mGLCanvas, i, i2, this.mParentFrameBufferId);
            Log.d("PipeRenderPair", String.format(Locale.ENGLISH, "Camera new framebuffer thread = %d  w = %d, h= %d", new Object[]{Long.valueOf(Thread.currentThread().getId()), Integer.valueOf(i), Integer.valueOf(i2)}));
            if (this.mFrameBuffers.size() > 5) {
                this.mFrameBuffers.remove(this.mFrameBuffers.size() - 1);
            }
            this.mFrameBuffers.add(frameBuffer);
        }
        return frameBuffer;
    }

    public RawTexture getTexture() {
        return this.mFrameBuffer == null ? null : this.mFrameBuffer.getTexture();
    }

    public void prepareCopyBlurTexture() {
        this.mTextureFilled = false;
    }

    public void setFirstRender(Render render) {
        this.mRenders.clear();
        if (render != null) {
            this.mRenders.add(render);
        }
        this.mFirstRender = render;
        if (this.mSecondRender != null) {
            this.mRenders.add(this.mSecondRender);
        }
    }

    public void setPreviewSize(int i, int i2) {
        super.setPreviewSize(i, i2);
        this.mBufferWidth = this.mUseMiddleBuffer ? this.mPreviewWidth / 12 : this.mPreviewWidth;
        this.mBufferHeight = this.mUseMiddleBuffer ? this.mPreviewHeight / 12 : this.mPreviewHeight;
    }

    public void setRenderPairs(Render render, Render render2) {
        if (render != this.mFirstRender || render2 != this.mSecondRender) {
            this.mRenders.clear();
            if (render != null) {
                this.mRenders.add(render);
            }
            if (render2 != null) {
                this.mRenders.add(render2);
            }
            this.mFirstRender = render;
            this.mSecondRender = render2;
        }
    }

    public void setSecondRender(Render render) {
        this.mRenders.clear();
        if (this.mFirstRender != null) {
            this.mRenders.add(this.mFirstRender);
        }
        if (render != null) {
            this.mRenders.add(render);
        }
        this.mSecondRender = render;
    }

    public void setUsedMiddleBuffer(boolean z) {
        this.mUseMiddleBuffer = z;
    }

    public void updateMiddleBuffer(int i, int i2) {
        if (this.mUseMiddleBuffer) {
            this.mBufferWidth = i / 12;
            this.mBufferHeight = i2 / 12;
            return;
        }
        this.mBufferWidth = i;
        this.mBufferHeight = i2;
    }
}
