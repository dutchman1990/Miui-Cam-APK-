package com.android.camera.effect.renders;

import android.opengl.GLES20;
import com.android.camera.effect.EffectController;
import com.android.gallery3d.ui.GLCanvas;

public class MirrorEffectRender extends ConvolutionEffectRender {
    private int mUniformDirectionH;

    public MirrorEffectRender(GLCanvas gLCanvas, int i) {
        super(gLCanvas, i);
    }

    private int getDirection(boolean z) {
        int orientation = z ? this.mJpegOrientation : EffectController.getInstance().getOrientation();
        if (z && this.mMirror) {
            orientation = (orientation + 180) % 360;
        }
        return orientation == 270 ? 3 : orientation == 180 ? 2 : orientation == 90 ? 1 : 0;
    }

    public String getFragShaderString() {
        return "precision mediump float;  \nvarying vec2 vTexCoord;  \nuniform sampler2D sTexture;  \nuniform int uDir; \nuniform float uAlpha; \nuniform vec2 uStep;  \nvoid main()               \n{                         \n    if (uDir == 0)    \n    { \n          gl_FragColor=texture2D(sTexture, vec2(vTexCoord.s>0.5 ? (1.0-vTexCoord.s) : vTexCoord.s, vTexCoord.t));\n    } \n    else if (uDir == 1)   \n    { \n          gl_FragColor=texture2D(sTexture, vec2(vTexCoord.s, vTexCoord.t<0.5 ? (1.0-vTexCoord.t) : vTexCoord.t));\n    } \n    else if (uDir == 2)   \n    { \n          gl_FragColor=texture2D(sTexture, vec2(vTexCoord.s<0.5 ? (1.0-vTexCoord.s) : vTexCoord.s, vTexCoord.t));\n    } \n    else if (uDir == 3)   \n    { \n          gl_FragColor=texture2D(sTexture, vec2(vTexCoord.s, vTexCoord.t>0.5 ? (1.0-vTexCoord.t) : vTexCoord.t));\n    } \n    gl_FragColor = gl_FragColor*uAlpha; \n}";
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
