package com.android.camera.effect.renders;

import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLES20;
import com.android.camera.effect.ShaderUtil;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.camera.effect.draw_mode.DrawBasicTexAttribute;
import com.android.camera.effect.draw_mode.DrawLineAttribute;
import com.android.camera.effect.draw_mode.DrawMeshAttribute;
import com.android.camera.effect.draw_mode.DrawMixedAttribute;
import com.android.camera.effect.draw_mode.DrawRectAttribute;
import com.android.camera.effect.draw_mode.DrawRectFTexAttribute;
import com.android.camera.effect.draw_mode.FillRectAttribute;
import com.android.gallery3d.ui.BasicTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLPaint;

public class BasicRender extends ShaderRender {
    private static final float[] TEXTURES = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    private static final float[] VERTICES = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    private int mUniformBlendFactorH;
    private int mUniformPaintColorH;

    public BasicRender(GLCanvas gLCanvas) {
        super(gLCanvas);
    }

    private void convertCoordinate(RectF rectF, RectF rectF2, BasicTexture basicTexture) {
        int width = basicTexture.getWidth();
        int height = basicTexture.getHeight();
        int textureWidth = basicTexture.getTextureWidth();
        int textureHeight = basicTexture.getTextureHeight();
        rectF.left /= (float) textureWidth;
        rectF.right /= (float) textureWidth;
        rectF.top /= (float) textureHeight;
        rectF.bottom /= (float) textureHeight;
        float f = ((float) width) / ((float) textureWidth);
        if (rectF.right > f) {
            rectF2.right = rectF2.left + ((rectF2.width() * (f - rectF.left)) / rectF.width());
            rectF.right = f;
        }
        float f2 = ((float) height) / ((float) textureHeight);
        if (rectF.bottom > f2) {
            rectF2.bottom = rectF2.top + ((rectF2.height() * (f2 - rectF.top)) / rectF.height());
            rectF.bottom = f2;
        }
    }

    private void drawLine(float f, float f2, float f3, float f4, GLPaint gLPaint) {
        GLES20.glUseProgram(this.mProgram);
        initAttribPointer();
        updateViewport();
        initGLPaint(gLPaint);
        this.mGLCanvas.getState().pushState();
        this.mGLCanvas.getState().translate(f, f2, 0.0f);
        this.mGLCanvas.getState().scale(f3 - f, f4 - f2, 1.0f);
        GLES20.glUniformMatrix4fv(this.mUniformMVPMatrixH, 1, false, this.mGLCanvas.getState().getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(this.mUniformSTMatrixH, 1, false, this.mGLCanvas.getState().getTexMaxtrix(), 0);
        GLES20.glUniform1f(this.mUniformAlphaH, this.mGLCanvas.getState().getAlpha());
        GLES20.glUniform1f(this.mUniformBlendAlphaH, this.mGLCanvas.getState().getBlendAlpha());
        GLES20.glUniform4f(this.mUniformBlendFactorH, 0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(3, 4, 2);
        this.mGLCanvas.getState().popState();
    }

    private void drawMesh(BasicTexture basicTexture, float f, float f2, int i, int i2, int i3, int i4) {
        GLES20.glUseProgram(this.mProgram);
        if (bindTexture(basicTexture, 33984)) {
            boolean z = this.mBlendEnabled && this.mGLCanvas.getState().getAlpha() < 0.95f;
            setBlendEnabled(z);
            GLES20.glBindBuffer(34962, i);
            GLES20.glVertexAttribPointer(this.mAttributePositionH, 2, 5126, false, 0, 0);
            GLES20.glEnableVertexAttribArray(this.mAttributePositionH);
            GLES20.glBindBuffer(34962, i2);
            GLES20.glVertexAttribPointer(this.mAttributeTexCoorH, 2, 5126, false, 0, 0);
            GLES20.glEnableVertexAttribArray(this.mAttributeTexCoorH);
            this.mGLCanvas.getState().pushState();
            this.mGLCanvas.getState().translate(f, f2, 0.0f);
            GLES20.glUniformMatrix4fv(this.mUniformMVPMatrixH, 1, false, this.mGLCanvas.getState().getFinalMatrix(), 0);
            GLES20.glUniformMatrix4fv(this.mUniformSTMatrixH, 1, false, this.mGLCanvas.getState().getTexMaxtrix(), 0);
            GLES20.glUniform1f(this.mUniformAlphaH, this.mGLCanvas.getState().getAlpha());
            GLES20.glUniform1f(this.mUniformBlendAlphaH, this.mGLCanvas.getState().getBlendAlpha());
            GLES20.glUniform4f(this.mUniformBlendFactorH, 1.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glUniform1i(this.mUniformTextureH, 0);
            GLES20.glBindBuffer(34963, i3);
            GLES20.glDrawElements(5, i4, 5121, 0);
            GLES20.glBindBuffer(34963, 0);
            GLES20.glBindBuffer(34962, 0);
            this.mGLCanvas.getState().popState();
        }
    }

    private void drawMixed(BasicTexture basicTexture, int i, float f, float f2, float f3, float f4, float f5) {
        GLES20.glUseProgram(this.mProgram);
        if (bindTexture(basicTexture, 33984)) {
            initAttribPointer();
            initGLPaint(i);
            updateViewport();
            boolean z = this.mBlendEnabled && (!basicTexture.isOpaque() || this.mGLCanvas.getState().getAlpha() < 0.95f);
            setBlendEnabled(z);
            this.mGLCanvas.getState().pushState();
            this.mGLCanvas.getState().translate(f2, f3, 0.0f);
            this.mGLCanvas.getState().scale(f4, f5, 1.0f);
            GLES20.glUniformMatrix4fv(this.mUniformMVPMatrixH, 1, false, this.mGLCanvas.getState().getFinalMatrix(), 0);
            GLES20.glUniformMatrix4fv(this.mUniformSTMatrixH, 1, false, this.mGLCanvas.getState().getTexMaxtrix(), 0);
            GLES20.glUniform1f(this.mUniformAlphaH, this.mGLCanvas.getState().getAlpha());
            GLES20.glUniform4f(this.mUniformBlendFactorH, 1.0f - f, 0.0f, 0.0f, f);
            GLES20.glUniform1i(this.mUniformTextureH, 0);
            GLES20.glUniform1f(this.mUniformBlendAlphaH, this.mGLCanvas.getState().getBlendAlpha());
            GLES20.glDrawArrays(5, 0, 4);
            this.mGLCanvas.getState().popState();
        }
    }

    private void drawRect(float f, float f2, float f3, float f4, GLPaint gLPaint) {
        GLES20.glUseProgram(this.mProgram);
        initAttribPointer();
        updateViewport();
        initGLPaint(gLPaint);
        this.mGLCanvas.getState().pushState();
        this.mGLCanvas.getState().translate(f, f2, 0.0f);
        this.mGLCanvas.getState().scale(f3, f4, 1.0f);
        GLES20.glUniformMatrix4fv(this.mUniformMVPMatrixH, 1, false, this.mGLCanvas.getState().getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(this.mUniformSTMatrixH, 1, false, this.mGLCanvas.getState().getTexMaxtrix(), 0);
        GLES20.glUniform1f(this.mUniformAlphaH, this.mGLCanvas.getState().getAlpha());
        GLES20.glUniform1f(this.mUniformBlendAlphaH, this.mGLCanvas.getState().getBlendAlpha());
        GLES20.glUniform4f(this.mUniformBlendFactorH, 0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(2, 6, 4);
        this.mGLCanvas.getState().popState();
    }

    private void drawTexture(BasicTexture basicTexture, float f, float f2, float f3, float f4) {
        this.mGLCanvas.getState().pushState();
        this.mGLCanvas.getState().indentityTexM();
        drawTextureInternal(basicTexture, f, f2, f3, f4);
        this.mGLCanvas.getState().popState();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void drawTexture(com.android.gallery3d.ui.BasicTexture r7, android.graphics.RectF r8, android.graphics.RectF r9) {
        /*
        r6 = this;
        r1 = 0;
        r0 = r9.width();
        r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r0 <= 0) goto L_0x0011;
    L_0x0009:
        r0 = r9.height();
        r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r0 > 0) goto L_0x0012;
    L_0x0011:
        return;
    L_0x0012:
        r0 = r6.mGLCanvas;
        r0 = r7.onBind(r0);
        if (r0 != 0) goto L_0x001b;
    L_0x001a:
        return;
    L_0x001b:
        r6.convertCoordinate(r8, r9, r7);
        r0 = r6.mGLCanvas;
        r0 = r0.getState();
        r0.pushState();
        r0 = r6.mGLCanvas;
        r0 = r0.getState();
        r1 = r8.left;
        r2 = r8.top;
        r3 = r8.right;
        r4 = r8.bottom;
        r0.setTexMatrix(r1, r2, r3, r4);
        r2 = r9.left;
        r3 = r9.top;
        r4 = r9.width();
        r5 = r9.height();
        r0 = r6;
        r1 = r7;
        r0.drawTextureInternal(r1, r2, r3, r4, r5);
        r0 = r6.mGLCanvas;
        r0 = r0.getState();
        r0.popState();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.effect.renders.BasicRender.drawTexture(com.android.gallery3d.ui.BasicTexture, android.graphics.RectF, android.graphics.RectF):void");
    }

    private void drawTextureInternal(BasicTexture basicTexture, float f, float f2, float f3, float f4) {
        if (f3 > 0.0f && f4 > 0.0f) {
            GLES20.glUseProgram(this.mProgram);
            if (bindTexture(basicTexture, 33984)) {
                GLES20.glUniform4f(this.mUniformBlendFactorH, 1.0f, 0.0f, 0.0f, 0.0f);
                GLES20.glUniform1i(this.mUniformTextureH, 0);
                initAttribPointer();
                updateViewport();
                float alpha = this.mGLCanvas.getState().getAlpha();
                float blendAlpha = this.mGLCanvas.getState().getBlendAlpha();
                boolean z = this.mBlendEnabled && (!basicTexture.isOpaque() || alpha < 0.95f || blendAlpha >= 0.0f);
                setBlendEnabled(z);
                this.mGLCanvas.getState().translate(f, f2, 0.0f);
                this.mGLCanvas.getState().scale(f3, f4, 1.0f);
                GLES20.glUniformMatrix4fv(this.mUniformMVPMatrixH, 1, false, this.mGLCanvas.getState().getFinalMatrix(), 0);
                GLES20.glUniformMatrix4fv(this.mUniformSTMatrixH, 1, false, this.mGLCanvas.getState().getTexMaxtrix(), 0);
                GLES20.glUniform1f(this.mUniformAlphaH, this.mGLCanvas.getState().getAlpha());
                GLES20.glUniform1f(this.mUniformBlendAlphaH, blendAlpha);
                GLES20.glDrawArrays(5, 0, 4);
            }
        }
    }

    private void fillRect(float f, float f2, float f3, float f4, int i) {
        GLES20.glUseProgram(this.mProgram);
        initAttribPointer();
        updateViewport();
        initGLPaint(i);
        this.mGLCanvas.getState().pushState();
        this.mGLCanvas.getState().translate(f, f2, 0.0f);
        this.mGLCanvas.getState().scale(f3, f4, 1.0f);
        GLES20.glUniformMatrix4fv(this.mUniformMVPMatrixH, 1, false, this.mGLCanvas.getState().getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(this.mUniformSTMatrixH, 1, false, this.mGLCanvas.getState().getTexMaxtrix(), 0);
        GLES20.glUniform1f(this.mUniformAlphaH, this.mGLCanvas.getState().getAlpha());
        GLES20.glUniform1f(this.mUniformBlendAlphaH, this.mGLCanvas.getState().getBlendAlpha());
        GLES20.glUniform4f(this.mUniformBlendFactorH, 0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(5, 0, 4);
        this.mGLCanvas.getState().popState();
    }

    private void initAttribPointer() {
        GLES20.glVertexAttribPointer(this.mAttributePositionH, 2, 5126, false, 8, this.mVertexBuffer);
        GLES20.glVertexAttribPointer(this.mAttributeTexCoorH, 2, 5126, false, 8, this.mTexCoorBuffer);
        GLES20.glEnableVertexAttribArray(this.mAttributePositionH);
        GLES20.glEnableVertexAttribArray(this.mAttributeTexCoorH);
    }

    private void initGLPaint(int i) {
        boolean z = true;
        float alpha = ((float) Color.alpha(i)) * 0.003921569f;
        if (!this.mBlendEnabled) {
            z = false;
        } else if (alpha >= 0.95f && this.mGLCanvas.getState().getAlpha() >= 0.95f) {
            z = false;
        }
        setBlendEnabled(z);
        GLES20.glUniform4f(this.mUniformPaintColorH, ((float) Color.red(i)) * 0.003921569f, ((float) Color.green(i)) * 0.003921569f, ((float) Color.blue(i)) * 0.003921569f, alpha);
    }

    private void initGLPaint(GLPaint gLPaint) {
        initGLPaint(gLPaint.getColor());
        GLES20.glLineWidth(gLPaint.getLineWidth());
    }

    public boolean draw(DrawAttribute drawAttribute) {
        if (!isAttriSupported(drawAttribute.getTarget())) {
            return false;
        }
        switch (drawAttribute.getTarget()) {
            case 0:
                DrawLineAttribute drawLineAttribute = (DrawLineAttribute) drawAttribute;
                drawLine(drawLineAttribute.mX1, drawLineAttribute.mY1, drawLineAttribute.mX2, drawLineAttribute.mY2, drawLineAttribute.mGLPaint);
                break;
            case 1:
                DrawRectAttribute drawRectAttribute = (DrawRectAttribute) drawAttribute;
                drawRect(drawRectAttribute.mX, drawRectAttribute.mY, drawRectAttribute.mWidth, drawRectAttribute.mHeight, drawRectAttribute.mGLPaint);
                break;
            case 2:
                DrawMeshAttribute drawMeshAttribute = (DrawMeshAttribute) drawAttribute;
                drawMesh(drawMeshAttribute.mBasicTexture, drawMeshAttribute.mX, drawMeshAttribute.mY, drawMeshAttribute.mXYBuffer, drawMeshAttribute.mUVBuffer, drawMeshAttribute.mIndexBuffer, drawMeshAttribute.mIndexCount);
                break;
            case 3:
                DrawMixedAttribute drawMixedAttribute = (DrawMixedAttribute) drawAttribute;
                drawMixed(drawMixedAttribute.mBasicTexture, drawMixedAttribute.mToColor, drawMixedAttribute.mRatio, drawMixedAttribute.mX, drawMixedAttribute.mY, drawMixedAttribute.mWidth, drawMixedAttribute.mHeight);
                break;
            case 4:
                FillRectAttribute fillRectAttribute = (FillRectAttribute) drawAttribute;
                fillRect(fillRectAttribute.mX, fillRectAttribute.mY, fillRectAttribute.mWidth, fillRectAttribute.mHeight, fillRectAttribute.mColor);
                break;
            case 5:
                DrawBasicTexAttribute drawBasicTexAttribute = (DrawBasicTexAttribute) drawAttribute;
                drawTexture(drawBasicTexAttribute.mBasicTexture, (float) drawBasicTexAttribute.mX, (float) drawBasicTexAttribute.mY, (float) drawBasicTexAttribute.mWidth, (float) drawBasicTexAttribute.mHeight);
                break;
            case 7:
                DrawRectFTexAttribute drawRectFTexAttribute = (DrawRectFTexAttribute) drawAttribute;
                drawTexture(drawRectFTexAttribute.mBasicTexture, drawRectFTexAttribute.mSourceRectF, drawRectFTexAttribute.mTargetRectF);
                break;
        }
        return true;
    }

    public String getFragShaderString() {
        return ShaderUtil.loadFromAssetsFile("frag_normal.txt");
    }

    protected void initShader() {
        this.mProgram = ShaderUtil.createProgram(getVertexShaderString(), getFragShaderString());
        if (this.mProgram != 0) {
            GLES20.glUseProgram(this.mProgram);
            this.mUniformMVPMatrixH = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
            this.mUniformSTMatrixH = GLES20.glGetUniformLocation(this.mProgram, "uSTMatrix");
            this.mUniformTextureH = GLES20.glGetUniformLocation(this.mProgram, "sTexture");
            this.mUniformAlphaH = GLES20.glGetUniformLocation(this.mProgram, "uAlpha");
            this.mUniformBlendAlphaH = GLES20.glGetUniformLocation(this.mProgram, "uMixAlpha");
            this.mUniformBlendFactorH = GLES20.glGetUniformLocation(this.mProgram, "uBlendFactor");
            this.mUniformPaintColorH = GLES20.glGetUniformLocation(this.mProgram, "uPaintColor");
            this.mAttributePositionH = GLES20.glGetAttribLocation(this.mProgram, "aPosition");
            this.mAttributeTexCoorH = GLES20.glGetAttribLocation(this.mProgram, "aTexCoord");
            return;
        }
        throw new IllegalArgumentException(getClass() + ": mProgram = 0");
    }

    protected void initSupportAttriList() {
        this.mAttriSupportedList.add(Integer.valueOf(0));
        this.mAttriSupportedList.add(Integer.valueOf(1));
        this.mAttriSupportedList.add(Integer.valueOf(2));
        this.mAttriSupportedList.add(Integer.valueOf(3));
        this.mAttriSupportedList.add(Integer.valueOf(4));
        this.mAttriSupportedList.add(Integer.valueOf(5));
        this.mAttriSupportedList.add(Integer.valueOf(7));
    }

    protected void initVertexData() {
        this.mVertexBuffer = ShaderRender.allocateByteBuffer((VERTICES.length * 32) / 8).asFloatBuffer();
        this.mVertexBuffer.put(VERTICES);
        this.mVertexBuffer.position(0);
        this.mTexCoorBuffer = ShaderRender.allocateByteBuffer((TEXTURES.length * 32) / 8).asFloatBuffer();
        this.mTexCoorBuffer.put(TEXTURES);
        this.mTexCoorBuffer.position(0);
    }
}
