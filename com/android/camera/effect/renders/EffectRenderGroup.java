package com.android.camera.effect.renders;

import android.graphics.Color;
import android.opengl.Matrix;
import android.util.Log;
import com.android.camera.Device;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.EffectController.SurfacePosition;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.camera.effect.draw_mode.DrawBasicTexAttribute;
import com.android.camera.effect.draw_mode.DrawExtTexAttribute;
import com.android.camera.effect.draw_mode.FillRectAttribute;
import com.android.gallery3d.ui.GLCanvas;

public class EffectRenderGroup extends RenderGroup {
    private DrawBasicTexAttribute mBasicTextureAttri = new DrawBasicTexAttribute();
    private int mEffectIndex = 0;
    private RenderGroup mEffectRenders;
    private Render mFocusPeakingRender;
    private int mIgnoreTimes;
    private Render mNoneEffectRender;
    private PipeRenderPair mPreviewPeakRender;
    private PipeRenderPair mPreviewPipeRender;
    private float[] mTexMatrix;

    public EffectRenderGroup(GLCanvas gLCanvas) {
        super(gLCanvas);
        this.mPreviewPipeRender = new PipeRenderPair(gLCanvas);
        addRender(this.mPreviewPipeRender);
        this.mPreviewPipeRender.setFirstRender(new SurfaceTextureRender(gLCanvas));
        this.mPreviewPeakRender = new PipeRenderPair(gLCanvas);
        this.mEffectRenders = gLCanvas.getEffectRenderGroup();
    }

    private void drawAnimationMask(DrawAttribute drawAttribute) {
        int blurAnimationValue = EffectController.getInstance().getBlurAnimationValue();
        if (blurAnimationValue > 0) {
            this.mGLCanvas.draw(new FillRectAttribute(0.0f, 0.0f, (float) ((DrawExtTexAttribute) drawAttribute).mWidth, (float) ((DrawExtTexAttribute) drawAttribute).mHeight, Color.argb(blurAnimationValue, 0, 0, 0)));
        }
    }

    private void drawDisplay(DrawAttribute drawAttribute) {
        DrawExtTexAttribute drawExtTexAttribute = (DrawExtTexAttribute) drawAttribute;
        if (!Device.isSupportedShaderEffect() || !EffectController.getInstance().isDisplayShow()) {
            this.mIgnoreTimes = 0;
        } else if (this.mIgnoreTimes > 0) {
            this.mIgnoreTimes--;
        } else if (this.mPreviewPipeRender.getTexture() != null) {
            this.mGLCanvas.prepareEffectRenders(true, -1);
            if (this.mTexMatrix == null) {
                float f = drawExtTexAttribute.mHeight * 9 == drawExtTexAttribute.mWidth * 16 ? 0.5625f : 0.75f;
                this.mTexMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
                Matrix.translateM(this.mTexMatrix, 0, 0.5f, 0.5f, 0.0f);
                Matrix.scaleM(this.mTexMatrix, 0, 1.0f, f, 1.0f);
                Matrix.translateM(this.mTexMatrix, 0, -0.5f, -0.5f, 0.0f);
            }
            if (EffectController.getInstance().isDisplayShow()) {
                long currentTimeMillis = System.currentTimeMillis();
                drawEffectTexture();
                Log.d("EffectRenderGroup", "Camera preview drawEffectTexture time =" + (System.currentTimeMillis() - currentTimeMillis));
            }
        }
    }

    private void drawEffectTexture() {
        long currentTimeMillis = System.currentTimeMillis();
        int displayStartIndex = EffectController.getInstance().getDisplayStartIndex();
        int displayEndIndex = EffectController.getInstance().getDisplayEndIndex();
        SurfacePosition surfacePosition = EffectController.getInstance().getSurfacePosition();
        int i = surfacePosition.mWidth;
        int i2 = surfacePosition.mHonSpace;
        int i3 = surfacePosition.mVerSpace;
        int i4 = surfacePosition.mIsRtl ? -1 : 1;
        this.mGLCanvas.getState().setTexMatrix(this.mTexMatrix);
        for (int i5 = displayStartIndex; i5 < displayEndIndex; i5++) {
            this.mBasicTextureAttri.init(this.mPreviewPipeRender.getTexture(), surfacePosition.mStartX + ((((i5 - displayStartIndex) % EffectController.COLUMN_COUNT) * (i + i2)) * i4), surfacePosition.mStartY + (((i5 - displayStartIndex) / EffectController.COLUMN_COUNT) * (i + i3)), i, i);
            if (i5 == 0) {
                this.mNoneEffectRender.draw(this.mBasicTextureAttri);
            } else {
                this.mEffectRenders.getRender(i5).draw(this.mBasicTextureAttri);
            }
        }
        if (System.currentTimeMillis() - currentTimeMillis > 100) {
            this.mIgnoreTimes = 1;
        }
    }

    private boolean drawPreview(DrawAttribute drawAttribute) {
        if ((EffectController.getInstance().hasEffect() || EffectController.getInstance().isDisplayShow() || EffectController.getInstance().isBackGroundBlur()) && this.mNoneEffectRender == null && Device.isSupportedShaderEffect()) {
            this.mGLCanvas.prepareEffectRenders(false, this.mEffectIndex);
            this.mNoneEffectRender = new NoneEffectRender(this.mGLCanvas);
            addRender(this.mNoneEffectRender);
            addRender(this.mEffectRenders);
            setViewportSize(this.mViewportWidth, this.mViewportHeight);
            setPreviewSize(this.mPreviewWidth, this.mPreviewHeight);
        }
        this.mPreviewPipeRender.setSecondRender(getPreviewSecondRender(((DrawExtTexAttribute) drawAttribute).mEffectPopup));
        this.mPreviewPipeRender.setUsedMiddleBuffer(EffectController.getInstance().isBackGroundBlur());
        this.mPreviewPipeRender.draw(drawAttribute);
        drawAnimationMask(drawAttribute);
        drawDisplay(drawAttribute);
        return true;
    }

    private Render getPreviewSecondRender(boolean z) {
        Render render;
        if (!Device.isSupportedShaderEffect() || this.mRenders.size() == 1 || this.mEffectRenders == null) {
            render = null;
        } else if (this.mEffectIndex == 0) {
            render = EffectController.getInstance().isDisplayShow() ? this.mNoneEffectRender : null;
        } else {
            render = this.mEffectRenders.getRender(this.mEffectIndex);
            if (render == null) {
                this.mGLCanvas.prepareEffectRenders(false, this.mEffectIndex);
                render = this.mEffectRenders.getRender(this.mEffectIndex);
            }
        }
        if (!EffectController.getInstance().isNeedDrawPeaking() || z) {
            return render;
        }
        if (this.mFocusPeakingRender == null) {
            this.mGLCanvas.prepareEffectRenders(false, EffectController.sPeakingMFIndex);
            this.mFocusPeakingRender = this.mEffectRenders.getRender(EffectController.sPeakingMFIndex);
        }
        if (render == null) {
            return this.mFocusPeakingRender;
        }
        this.mPreviewPeakRender.setRenderPairs(render, this.mFocusPeakingRender);
        return this.mPreviewPeakRender;
    }

    public boolean draw(DrawAttribute drawAttribute) {
        int i = this.mEffectIndex;
        this.mEffectIndex = EffectController.getInstance().getEffect(true);
        if (this.mEffectIndex != i && EffectController.getInstance().isBackGroundBlur()) {
            this.mPreviewPipeRender.prepareCopyBlurTexture();
        }
        switch (drawAttribute.getTarget()) {
            case 8:
                return drawPreview(drawAttribute);
            default:
                return false;
        }
    }

    public void setPreviewSize(int i, int i2) {
        super.setPreviewSize(i, i2);
        this.mTexMatrix = null;
    }
}
