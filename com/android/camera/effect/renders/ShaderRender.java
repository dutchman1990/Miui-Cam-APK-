package com.android.camera.effect.renders;

import android.opengl.GLES20;
import android.util.Log;
import com.android.camera.effect.ShaderUtil;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.gallery3d.ui.BasicTexture;
import com.android.gallery3d.ui.GLCanvas;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public abstract class ShaderRender extends Render {
    private static final String VERTEX = ShaderUtil.loadFromAssetsFile("vertex_normal.txt");
    protected ArrayList<Integer> mAttriSupportedList = new ArrayList();
    protected int mAttributePositionH;
    protected int mAttributeTexCoorH;
    protected boolean mBlendEnabled = true;
    protected float[] mPreviewEffectRect = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
    protected int mProgram = 0;
    protected float[] mSnapshotEffectRect = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
    protected FloatBuffer mTexCoorBuffer;
    protected int mUniformAlphaH;
    protected int mUniformBlendAlphaH;
    protected int mUniformMVPMatrixH;
    protected int mUniformSTMatrixH;
    protected int mUniformTextureH;
    protected FloatBuffer mVertexBuffer;

    public ShaderRender(GLCanvas gLCanvas) {
        super(gLCanvas);
        initShader();
        initVertexData();
        initSupportAttriList();
    }

    public ShaderRender(GLCanvas gLCanvas, int i) {
        super(gLCanvas, i);
        initShader();
        initVertexData();
        initSupportAttriList();
    }

    public static ByteBuffer allocateByteBuffer(int i) {
        return ByteBuffer.allocateDirect(i).order(ByteOrder.nativeOrder());
    }

    protected boolean bindTexture(int i, int i2) {
        GLES20.glActiveTexture(i2);
        GLES20.glBindTexture(3553, 0);
        GLES20.glBindTexture(3553, i);
        return true;
    }

    protected boolean bindTexture(BasicTexture basicTexture, int i) {
        if (!basicTexture.onBind(this.mGLCanvas)) {
            return false;
        }
        GLES20.glActiveTexture(i);
        GLES20.glBindTexture(basicTexture.getTarget(), 0);
        GLES20.glBindTexture(basicTexture.getTarget(), basicTexture.getId());
        return true;
    }

    public abstract boolean draw(DrawAttribute drawAttribute);

    protected void finalize() throws Throwable {
        if (!(this.mProgram == 0 || this.mGLCanvas == null)) {
            Log.d("Camera", "delete mProgram = " + this.mProgram);
            this.mGLCanvas.deleteProgram(this.mProgram);
            this.mProgram = 0;
        }
        super.finalize();
    }

    public abstract String getFragShaderString();

    public String getVertexShaderString() {
        return VERTEX;
    }

    protected abstract void initShader();

    protected abstract void initSupportAttriList();

    protected abstract void initVertexData();

    public boolean isAttriSupported(int i) {
        return this.mAttriSupportedList.contains(Integer.valueOf(i));
    }

    protected void setBlendEnabled(boolean z) {
        if (z) {
            GLES20.glEnable(3042);
            GLES20.glBlendFunc(770, 771);
            return;
        }
        GLES20.glDisable(3042);
    }
}
