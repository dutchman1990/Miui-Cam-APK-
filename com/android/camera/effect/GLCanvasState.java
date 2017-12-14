package com.android.camera.effect;

import android.opengl.Matrix;
import com.android.gallery3d.ui.Utils;
import java.util.Stack;

public class GLCanvasState {
    private float mAlpha = 1.0f;
    private float mBlendAlpha = -1.0f;
    private Stack<CanvasStateConfig> mCanvaStateStack = new Stack();
    private final float[] mIdentityMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mTexMatrix = new float[16];
    private float[] mViewMatrix = new float[16];

    private class CanvasStateConfig {
        float mAlpha = 1.0f;
        float mBlendAlpha = -1.0f;
        float[] mModelMatrix = new float[16];
        float[] mTexMatrix = new float[16];

        public CanvasStateConfig(float[] fArr, float[] fArr2, float f, float f2) {
            int i;
            for (i = 0; i < 16; i++) {
                this.mModelMatrix[i] = fArr[i];
            }
            for (i = 0; i < 16; i++) {
                this.mTexMatrix[i] = fArr2[i];
            }
            this.mAlpha = f;
            this.mBlendAlpha = f2;
        }

        public float getAlpha() {
            return this.mAlpha;
        }

        public float getBlendAlpha() {
            return this.mBlendAlpha;
        }

        public float[] getModelMatrix() {
            return this.mModelMatrix;
        }

        public float[] getTexMatrix() {
            return this.mTexMatrix;
        }
    }

    public GLCanvasState() {
        Matrix.setIdentityM(this.mModelMatrix, 0);
        Matrix.setIdentityM(this.mViewMatrix, 0);
        Matrix.setIdentityM(this.mProjectionMatrix, 0);
        Matrix.setIdentityM(this.mTexMatrix, 0);
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public float getBlendAlpha() {
        return this.mBlendAlpha;
    }

    public float[] getFinalMatrix() {
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mViewMatrix, 0, this.mModelMatrix, 0);
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjectionMatrix, 0, this.mMVPMatrix, 0);
        return this.mMVPMatrix;
    }

    public float[] getTexMaxtrix() {
        return this.mTexMatrix;
    }

    public void indentityAllM() {
        Matrix.setIdentityM(this.mModelMatrix, 0);
        Matrix.setIdentityM(this.mTexMatrix, 0);
        Matrix.setIdentityM(this.mViewMatrix, 0);
        Matrix.setIdentityM(this.mProjectionMatrix, 0);
    }

    public void indentityTexM() {
        Matrix.setIdentityM(this.mTexMatrix, 0);
    }

    public void ortho(float f, float f2, float f3, float f4) {
        Matrix.orthoM(this.mProjectionMatrix, 0, f, f2, f3, f4, -1.0f, 1.0f);
    }

    public void popState() {
        if (!this.mCanvaStateStack.isEmpty()) {
            CanvasStateConfig canvasStateConfig = (CanvasStateConfig) this.mCanvaStateStack.pop();
            if (canvasStateConfig == null) {
                throw new IllegalStateException();
            }
            this.mModelMatrix = canvasStateConfig.getModelMatrix();
            this.mTexMatrix = canvasStateConfig.getTexMatrix();
            this.mAlpha = canvasStateConfig.getAlpha();
            this.mBlendAlpha = canvasStateConfig.getBlendAlpha();
        }
    }

    public void pushState() {
        this.mCanvaStateStack.push(new CanvasStateConfig(this.mModelMatrix, this.mTexMatrix, this.mAlpha, this.mBlendAlpha));
    }

    public void rotate(float f, float f2, float f3, float f4) {
        if (f != 0.0f) {
            Matrix.rotateM(this.mModelMatrix, 0, f, f2, f3, f4);
        }
    }

    public void scale(float f, float f2, float f3) {
        Matrix.scaleM(this.mModelMatrix, 0, f, f2, f3);
    }

    public void setAlpha(float f) {
        boolean z = false;
        if (f >= 0.0f && f <= 1.0f) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mAlpha = f;
    }

    public void setBlendAlpha(float f) {
        boolean z = false;
        if (f >= 0.0f && f <= 1.0f) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mBlendAlpha = f;
    }

    public void setTexMatrix(float f, float f2, float f3, float f4) {
        Matrix.setIdentityM(this.mTexMatrix, 0);
        this.mTexMatrix[0] = f3 - f;
        this.mTexMatrix[5] = f4 - f2;
        this.mTexMatrix[10] = 1.0f;
        this.mTexMatrix[12] = f;
        this.mTexMatrix[13] = f2;
        this.mTexMatrix[15] = 1.0f;
    }

    public void setTexMatrix(float[] fArr) {
        for (int i = 0; i < 16; i++) {
            this.mTexMatrix[i] = fArr[i];
        }
    }

    public void translate(float f, float f2) {
        Matrix.translateM(this.mModelMatrix, 0, f, f2, 0.0f);
    }

    public void translate(float f, float f2, float f3) {
        Matrix.translateM(this.mModelMatrix, 0, f, f2, f3);
    }
}
