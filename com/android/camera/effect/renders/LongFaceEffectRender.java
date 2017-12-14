package com.android.camera.effect.renders;

import android.opengl.GLES20;
import com.android.camera.effect.EffectController;
import com.android.gallery3d.ui.GLCanvas;

public class LongFaceEffectRender extends PixelEffectRender {
    private int mUniformDirectionH;

    public LongFaceEffectRender(GLCanvas gLCanvas, int i) {
        super(gLCanvas, i);
    }

    private int getDirection(boolean z) {
        int orientation = ((z ? this.mJpegOrientation - EffectController.getInstance().getOrientation() : 0) + 360) % 360;
        if (z && this.mMirror) {
            orientation = (orientation + 180) % 360;
        }
        return orientation == 270 ? 3 : orientation == 180 ? 2 : orientation == 90 ? 1 : 0;
    }

    public String getFragShaderString() {
        return "precision mediump float; \nuniform sampler2D sTexture; \nvarying vec2 vTexCoord; \nuniform int uDir; \nuniform float uAlpha; \nvec4 longface() { \n    float sgnv; \n    vec2 coord; \n    if (uDir == 0 || uDir == 2) { \n        coord = vec2(vTexCoord[0], vTexCoord[1]); \n    } else { \n        coord = vec2(vTexCoord[1], vTexCoord[0]); \n    } \n    if(coord[1]>0.5) { \n        sgnv = 1.0;  \n    } else { \n        sgnv = -1.0; \n    } \n    float new_radius = sgnv * pow(abs(coord[1]-0.5)*2.0, 1.5)/2.0; \n    vec2 newCoord; \n    if (uDir == 0 || uDir == 2) { \n       newCoord = vec2(coord[0], 0.5 + new_radius); \n    } else { \n       newCoord = vec2(0.5 + new_radius, coord[0]); \n    } \n    if (newCoord.x > 1.0 || newCoord.x < 0.0 || newCoord.y > 1.0 || newCoord.y < 0.0) { \n        return vec4(0.0, 0.0, 0.0, 1.0); \n    } else { \n        return texture2D(sTexture, newCoord); \n    } \n} \nvoid main() { \n    gl_FragColor = vec4(longface().rgb, 1.0) * uAlpha; \n}";
    }

    protected void initShader() {
        super.initShader();
        this.mUniformDirectionH = GLES20.glGetUniformLocation(this.mProgram, "uDir");
    }

    protected void initShaderValue(boolean z) {
        super.initShaderValue(z);
        GLES20.glUniform1i(this.mUniformDirectionH, getDirection(z));
    }
}
