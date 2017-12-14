package com.android.gallery3d.ui;

import android.util.Log;
import com.android.camera.effect.draw_mode.DrawBasicTexAttribute;
import java.util.Locale;
import java.util.WeakHashMap;

public abstract class BasicTexture implements Texture {
    private static WeakHashMap<BasicTexture, Object> sAllTextures = new WeakHashMap();
    private static ThreadLocal<Object> sInFinalizer = new ThreadLocal();
    protected GLCanvas mCanvasRef;
    private boolean mHasBorder;
    protected int mHeight;
    protected int mId;
    protected int mState;
    private int mTextureHeight;
    private int mTextureWidth;
    protected int mWidth;

    protected BasicTexture() {
        this(null, 0, 0);
    }

    protected BasicTexture(GLCanvas gLCanvas, int i, int i2) {
        this.mWidth = -1;
        this.mHeight = -1;
        this.mCanvasRef = null;
        setAssociatedCanvas(gLCanvas);
        this.mId = i;
        this.mState = i2;
        synchronized (sAllTextures) {
            sAllTextures.put(this, null);
        }
    }

    private void freeResource() {
        GLCanvas gLCanvas = this.mCanvasRef;
        if (gLCanvas != null && isLoaded()) {
            gLCanvas.deleteTexture(this);
        }
        this.mState = 0;
        setAssociatedCanvas(null);
    }

    public static boolean inFinalizer() {
        return sInFinalizer.get() != null;
    }

    public static void invalidateAllTextures(GLCanvas gLCanvas) {
        synchronized (sAllTextures) {
            for (BasicTexture basicTexture : sAllTextures.keySet()) {
                if (basicTexture.mCanvasRef == gLCanvas) {
                    basicTexture.mState = 0;
                    basicTexture.setAssociatedCanvas(null);
                }
            }
        }
    }

    public void draw(GLCanvas gLCanvas, int i, int i2, int i3, int i4) {
        gLCanvas.draw(new DrawBasicTexAttribute(this, i, i2, i3, i4));
    }

    protected void finalize() {
        sInFinalizer.set(BasicTexture.class);
        recycle();
        sInFinalizer.set(null);
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getId() {
        return this.mId;
    }

    public abstract int getTarget();

    public int getTextureHeight() {
        return this.mTextureHeight;
    }

    public int getTextureWidth() {
        return this.mTextureWidth;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public boolean isLoaded() {
        return this.mState == 1;
    }

    public abstract boolean onBind(GLCanvas gLCanvas);

    public void recycle() {
        freeResource();
    }

    protected void setAssociatedCanvas(GLCanvas gLCanvas) {
        this.mCanvasRef = gLCanvas;
    }

    protected void setBorder(boolean z) {
        this.mHasBorder = z;
    }

    public void setSize(int i, int i2) {
        this.mWidth = i;
        this.mHeight = i2;
        this.mTextureWidth = this.mWidth;
        this.mTextureHeight = this.mHeight;
        if (this.mTextureWidth > 4096 || this.mTextureHeight > 4096) {
            Log.w("BasicTexture", String.format(Locale.ENGLISH, "texture is too large: %d x %d", new Object[]{Integer.valueOf(this.mTextureWidth), Integer.valueOf(this.mTextureHeight)}), new Exception());
        }
    }
}
