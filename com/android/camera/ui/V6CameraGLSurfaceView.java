package com.android.camera.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import com.android.camera.ActivityBase;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.gallery3d.ui.BasicTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLCanvasImpl;
import com.android.gallery3d.ui.UploadedTexture;
import com.android.gallery3d.ui.Utils;
import java.util.concurrent.locks.ReentrantLock;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class V6CameraGLSurfaceView extends GLSurfaceView implements Renderer {
    private final ActivityBase mActivity;
    private GLCanvas mCanvas;
    private EGLContext mEGLContext;
    private final MyEGLConfigChooser mEglConfigChooser;
    private int mFrameCount;
    private long mFrameCountingStart;
    private GL11 mGL;
    protected int mHeight;
    private final ReentrantLock mRenderLock;
    private volatile boolean mRenderRequested;
    protected int mWidth;

    private class MyEGLConfigChooser implements EGLConfigChooser {
        private final int[] ATTR_ID;
        private final String[] ATTR_NAME;
        private final boolean USE_RGB888;
        private final int[] mConfigSpec;

        private MyEGLConfigChooser() {
            this.USE_RGB888 = !Device.IS_H3C ? Device.IS_D3 : true;
            int[] iArr = new int[11];
            iArr[0] = 12324;
            iArr[1] = this.USE_RGB888 ? 8 : 5;
            iArr[2] = 12323;
            iArr[3] = this.USE_RGB888 ? 8 : 6;
            iArr[4] = 12322;
            iArr[5] = this.USE_RGB888 ? 8 : 5;
            iArr[6] = 12321;
            iArr[7] = 0;
            iArr[8] = 12352;
            iArr[9] = 4;
            iArr[10] = 12344;
            this.mConfigSpec = iArr;
            this.ATTR_ID = new int[]{12324, 12323, 12322, 12321, 12325, 12326, 12328, 12327};
            this.ATTR_NAME = new String[]{"R", "G", "B", "A", "D", "S", "ID", "CAVEAT"};
        }

        private EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr) {
            EGLConfig eGLConfig = null;
            int i = Integer.MAX_VALUE;
            int[] iArr = new int[1];
            int length = eGLConfigArr.length;
            for (int i2 = 0; i2 < length; i2++) {
                if (!egl10.eglGetConfigAttrib(eGLDisplay, eGLConfigArr[i2], 12324, iArr) || iArr[0] != 8) {
                    if (!egl10.eglGetConfigAttrib(eGLDisplay, eGLConfigArr[i2], 12326, iArr)) {
                        throw new RuntimeException("eglGetConfigAttrib error: " + egl10.eglGetError());
                    } else if (iArr[0] != 0 && iArr[0] < r1) {
                        i = iArr[0];
                        eGLConfig = eGLConfigArr[i2];
                    }
                }
            }
            if (eGLConfig == null) {
                eGLConfig = eGLConfigArr[0];
            }
            egl10.eglGetConfigAttrib(eGLDisplay, eGLConfig, 12326, iArr);
            logConfig(egl10, eGLDisplay, eGLConfig);
            return eGLConfig;
        }

        private void logConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig) {
            int[] iArr = new int[1];
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < this.ATTR_ID.length; i++) {
                egl10.eglGetConfigAttrib(eGLDisplay, eGLConfig, this.ATTR_ID[i], iArr);
                stringBuilder.append(this.ATTR_NAME[i]).append(iArr[0]).append(" ");
            }
            Log.i("GLRootView", "Config chosen: " + stringBuilder.toString());
        }

        public EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay) {
            int[] iArr = new int[1];
            if (!egl10.eglChooseConfig(eGLDisplay, this.mConfigSpec, null, 0, iArr)) {
                throw new RuntimeException("eglChooseConfig failed");
            } else if (iArr[0] <= 0) {
                throw new RuntimeException("No configs match configSpec");
            } else {
                EGLConfig[] eGLConfigArr = new EGLConfig[iArr[0]];
                if (egl10.eglChooseConfig(eGLDisplay, this.mConfigSpec, eGLConfigArr, eGLConfigArr.length, iArr)) {
                    return chooseConfig(egl10, eGLDisplay, eGLConfigArr);
                }
                throw new RuntimeException();
            }
        }
    }

    public V6CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public V6CameraGLSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFrameCount = 0;
        this.mFrameCountingStart = 0;
        this.mRenderRequested = false;
        this.mEglConfigChooser = new MyEGLConfigChooser();
        this.mRenderLock = new ReentrantLock();
        setEGLContextClientVersion(2);
        setEGLConfigChooser(this.mEglConfigChooser);
        setRenderer(this);
        setRenderMode(0);
        getHolder().setFormat(4);
        if (Device.isSurfaceSizeLimited()) {
            getHolder().setFixedSize(720, (Util.sWindowHeight * 720) / Util.sWindowWidth);
        }
        this.mActivity = (ActivityBase) context;
    }

    public EGLContext getEGLContext() {
        return this.mEGLContext;
    }

    public GLCanvas getGLCanvas() {
        return this.mCanvas;
    }

    public boolean isBusy() {
        return this.mRenderRequested;
    }

    public void onDrawFrame(GL10 gl10) {
        this.mCanvas.recycledResources();
        UploadedTexture.resetUploadLimit();
        this.mRenderRequested = false;
        synchronized (this.mCanvas) {
            this.mCanvas.getState().pushState();
            this.mActivity.getCameraScreenNail().draw(this.mCanvas);
            this.mCanvas.getState().popState();
        }
        if (UploadedTexture.uploadLimitReached()) {
            requestRender();
        }
        this.mCanvas.recycledResources();
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        Log.i("GLRootView", "onSurfaceChanged: " + i + "x" + i2 + ", gl10: " + gl10.toString());
        Process.setThreadPriority(-4);
        Utils.assertTrue(this.mGL == ((GL11) gl10));
        this.mWidth = i;
        this.mHeight = i2;
        this.mCanvas.setSize(i, i2);
        this.mEGLContext = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        GL11 gl11 = (GL11) gl10;
        if (this.mGL != null) {
            Log.i("GLRootView", "GLObject has changed from " + this.mGL + " to " + gl11);
        }
        this.mRenderLock.lock();
        try {
            this.mGL = gl11;
            BasicTexture.invalidateAllTextures(this.mCanvas);
            this.mCanvas = new GLCanvasImpl();
            setRenderMode(0);
        } finally {
            this.mRenderLock.unlock();
        }
    }

    public void requestRender() {
        if (!this.mRenderRequested) {
            this.mRenderRequested = true;
            super.requestRender();
        }
    }
}
