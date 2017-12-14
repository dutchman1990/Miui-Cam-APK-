package com.android.camera.effect.renders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.FrameBuffer;
import com.android.camera.effect.ShaderNativeUtil;
import com.android.camera.effect.SnapshotCanvas;
import com.android.camera.effect.draw_mode.DrawBasicTexAttribute;
import com.android.camera.effect.draw_mode.DrawIntTexAttribute;
import com.android.camera.effect.draw_mode.DrawJPEGAttribute;
import com.android.camera.google.PhotosSpecialTypesProvider;
import com.android.camera.google.ProcessingMediaManager;
import com.android.camera.google.ProcessingMediaManager.JpegThumbnail;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.ImageSaver;
import com.android.camera.storage.Storage;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.ui.BasicTexture;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvasImpl;
import com.android.gallery3d.ui.GLId;
import com.android.gallery3d.ui.StringTexture;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class SnapshotEffectRender {
    private static final int[] CONFIG_SPEC = new int[]{12352, 4, 12324, 8, 12323, 8, 12322, 8, 12344};
    private ActivityBase mActivity;
    private Bitmap mDualCameraWaterMark;
    private EGL10 mEgl;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLHandler mEglHandler;
    private EGLSurface mEglSurface;
    private HandlerThread mEglThread;
    private ConditionVariable mEglThreadBlockVar = new ConditionVariable();
    private boolean mExifNeeded = true;
    private ImageSaver mImageSaver;
    private boolean mIsImageCaptureIntent;
    private volatile int mJpegQueueSize = 0;
    private final Object mLock = new Object();
    private int mQuality = 85;
    private boolean mRelease;
    private boolean mReleasePending;
    private Map<String, String> mTitleMap = new HashMap(7);

    private class EGLHandler extends Handler {
        private FrameBuffer mFrameBuffer;
        private SnapshotCanvas mGLCanvas;

        public EGLHandler(Looper looper) {
            super(looper);
        }

        private byte[] applyEffect(DrawJPEGAttribute drawJPEGAttribute, int i, boolean z, Size size, Size size2) {
            byte[] thumbnailBytes = z ? drawJPEGAttribute.mExif.getThumbnailBytes() : drawJPEGAttribute.mData;
            if (thumbnailBytes == null) {
                Log.w("SnapshotEffectProcessor", "Null " + (z ? "thumb!" : "jpeg!"));
                return null;
            }
            long currentTimeMillis = System.currentTimeMillis();
            int[] iArr = new int[1];
            GLId.glGenTextures(1, iArr, 0);
            int[] initTexture = ShaderNativeUtil.initTexture(thumbnailBytes, iArr[0], i);
            Log.d("SnapshotEffectProcessor", "initTime=" + (System.currentTimeMillis() - currentTimeMillis));
            int i2 = z ? initTexture[0] : drawJPEGAttribute.mWidth;
            int i3 = z ? initTexture[1] : drawJPEGAttribute.mHeight;
            int i4 = z ? initTexture[0] : drawJPEGAttribute.mPreviewWidth;
            int i5 = z ? initTexture[1] : drawJPEGAttribute.mPreviewHeight;
            if (z && size != null) {
                size.width = i2;
                size.height = i3;
                Log.d("SnapshotEffectProcessor", "thumbSize=" + size.width + "*" + size.height);
            }
            Render effectRender = getEffectRender(drawJPEGAttribute.mEffectIndex);
            effectRender.setPreviewSize(i4, i5);
            effectRender.setEffectRangeAttribute(drawJPEGAttribute.mAttribute);
            effectRender.setMirror(drawJPEGAttribute.mMirror);
            if (z) {
                effectRender.setSnapshotSize(i2, i3);
            } else {
                effectRender.setSnapshotSize(size2.width, size2.height);
            }
            effectRender.setOrientation(drawJPEGAttribute.mOrientation);
            effectRender.setShootRotation(drawJPEGAttribute.mShootRotation);
            effectRender.setJpegOrientation(drawJPEGAttribute.mJpegOrientation);
            checkFrameBuffer(drawJPEGAttribute.mWidth, drawJPEGAttribute.mHeight);
            this.mGLCanvas.beginBindFrameBuffer(this.mFrameBuffer);
            currentTimeMillis = System.currentTimeMillis();
            effectRender.draw(new DrawIntTexAttribute(iArr[0], 0, 0, i2, i3));
            drawWaterMark(0, 0, i2, i3, drawJPEGAttribute.mJpegOrientation);
            Log.d("SnapshotEffectProcessor", "drawTime=" + (System.currentTimeMillis() - currentTimeMillis));
            GLES20.glPixelStorei(3333, 1);
            currentTimeMillis = System.currentTimeMillis();
            byte[] picture = ShaderNativeUtil.getPicture(i2, i3, SnapshotEffectRender.this.mQuality);
            Log.d("SnapshotEffectProcessor", "readTime=" + (System.currentTimeMillis() - currentTimeMillis));
            if (GLES20.glIsTexture(iArr[0])) {
                GLES20.glDeleteTextures(1, iArr, 0);
            }
            this.mGLCanvas.endBindFrameBuffer();
            return picture;
        }

        private void checkFrameBuffer(int i, int i2) {
            if (this.mFrameBuffer != null && this.mFrameBuffer.getWidth() >= i) {
                if (this.mFrameBuffer.getHeight() >= i2) {
                    return;
                }
            }
            this.mFrameBuffer = null;
            this.mFrameBuffer = new FrameBuffer(this.mGLCanvas, i, i2, 0);
        }

        private boolean drawMainJpeg(DrawJPEGAttribute drawJPEGAttribute, boolean z) {
            int i = 1;
            Size size = new Size(drawJPEGAttribute.mWidth, drawJPEGAttribute.mHeight);
            while (true) {
                if (drawJPEGAttribute.mWidth <= GLCanvasImpl.sMaxTextureSize && drawJPEGAttribute.mHeight <= GLCanvasImpl.sMaxTextureSize) {
                    break;
                }
                drawJPEGAttribute.mWidth /= 2;
                drawJPEGAttribute.mHeight /= 2;
                i *= 2;
            }
            if (EffectController.getInstance().needDownScale(drawJPEGAttribute.mEffectIndex)) {
                int i2 = (int) ((((float) drawJPEGAttribute.mWidth) / ((float) drawJPEGAttribute.mPreviewWidth)) + 0.5f);
                if (i2 > i) {
                    i = i2;
                }
            }
            byte[] applyEffect = applyEffect(drawJPEGAttribute, i, false, null, size);
            Log.d("SnapshotEffectProcessor", "mainLen=" + (applyEffect == null ? "null" : Integer.valueOf(applyEffect.length)));
            if (applyEffect != null) {
                drawJPEGAttribute.mData = applyEffect;
            }
            if (z) {
                String str;
                synchronized (SnapshotEffectRender.this) {
                    str = (String) SnapshotEffectRender.this.mTitleMap.get(drawJPEGAttribute.mTitle);
                    SnapshotEffectRender.this.mTitleMap.remove(drawJPEGAttribute.mTitle);
                }
                if (SnapshotEffectRender.this.mImageSaver != null) {
                    SnapshotEffectRender.this.mImageSaver.addImage(drawJPEGAttribute.mData, str == null ? drawJPEGAttribute.mTitle : str, str == null ? null : drawJPEGAttribute.mTitle, drawJPEGAttribute.mDate, drawJPEGAttribute.mUri, drawJPEGAttribute.mLoc, drawJPEGAttribute.mWidth, drawJPEGAttribute.mHeight, drawJPEGAttribute.mExif, drawJPEGAttribute.mJpegOrientation, false, false, str == null ? drawJPEGAttribute.mFinalImage : false, drawJPEGAttribute.mPortrait);
                } else {
                    Uri addImage;
                    if (drawJPEGAttribute.mUri == null) {
                        addImage = Storage.addImage(SnapshotEffectRender.this.mActivity, str == null ? drawJPEGAttribute.mTitle : str, drawJPEGAttribute.mDate, drawJPEGAttribute.mLoc, drawJPEGAttribute.mJpegOrientation, drawJPEGAttribute.mData, drawJPEGAttribute.mWidth, drawJPEGAttribute.mHeight, false, false, false, drawJPEGAttribute.mPortrait);
                    } else {
                        addImage = drawJPEGAttribute.mUri;
                        Storage.updateImage(SnapshotEffectRender.this.mActivity, drawJPEGAttribute.mData, drawJPEGAttribute.mExif, drawJPEGAttribute.mUri, str == null ? drawJPEGAttribute.mTitle : str, drawJPEGAttribute.mLoc, drawJPEGAttribute.mJpegOrientation, drawJPEGAttribute.mWidth, drawJPEGAttribute.mHeight, str == null ? null : drawJPEGAttribute.mTitle, drawJPEGAttribute.mPortrait);
                    }
                    if (drawJPEGAttribute.mPortrait) {
                        PhotosSpecialTypesProvider.markPortraitSpecialType(SnapshotEffectRender.this.mActivity, addImage);
                    }
                    ProcessingMediaManager.instance().removeProcessingMedia(SnapshotEffectRender.this.mActivity, drawJPEGAttribute.mUri);
                }
            } else if (drawJPEGAttribute.mExif != null) {
                OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    drawJPEGAttribute.mExif.writeExif(drawJPEGAttribute.mData, byteArrayOutputStream);
                    byte[] toByteArray = byteArrayOutputStream.toByteArray();
                    if (toByteArray != null) {
                        drawJPEGAttribute.mData = toByteArray;
                    }
                    byteArrayOutputStream.close();
                } catch (Throwable e) {
                    Log.e("SnapshotEffectProcessor", e.getMessage(), e);
                }
            }
            return true;
        }

        private boolean drawThumbJpeg(DrawJPEGAttribute drawJPEGAttribute, boolean z) {
            if (drawJPEGAttribute.mExif == null) {
                drawJPEGAttribute.mExif = SnapshotEffectRender.this.getExif(drawJPEGAttribute.mData);
            }
            Size size = new Size();
            byte[] applyEffect = applyEffect(drawJPEGAttribute, 1, true, size, null);
            Log.d("SnapshotEffectProcessor", "drawThumbJepg: thumbLen=" + (applyEffect == null ? "null" : Integer.valueOf(applyEffect.length)));
            if (applyEffect != null) {
                drawJPEGAttribute.mExif.setCompressedThumbnail(applyEffect);
            }
            boolean z2 = drawJPEGAttribute.mJpegOrientation != 0;
            if (z && drawJPEGAttribute.mExif.getThumbnailBytes() != null) {
                drawJPEGAttribute.mUri = Storage.addImage(SnapshotEffectRender.this.mActivity, drawJPEGAttribute.mTitle, drawJPEGAttribute.mDate, drawJPEGAttribute.mLoc, drawJPEGAttribute.mJpegOrientation, drawJPEGAttribute.mExif.getThumbnailBytes(), size.width, size.height, false, false, false, z2, true);
                if (drawJPEGAttribute.mUri != null) {
                    SnapshotEffectRender.this.mActivity.addSecureUri(drawJPEGAttribute.mUri);
                    ProcessingMediaManager.instance().addProcessingMedia(SnapshotEffectRender.this.mActivity, drawJPEGAttribute.mUri, new JpegThumbnail(drawJPEGAttribute.mJpegOrientation, drawJPEGAttribute.mExif.getThumbnailBytes()));
                }
            }
            return true;
        }

        private void drawWaterMark(int i, int i2, int i3, int i4, int i5) {
            if (Device.isEffectWatermarkFilted() && (CameraSettings.isTimeWaterMarkOpen(CameraSettingPreferences.instance()) || CameraSettings.isDualCameraWaterMarkOpen(CameraSettingPreferences.instance()))) {
                if (CameraSettings.isTimeWaterMarkOpen(CameraSettingPreferences.instance())) {
                    String timeWatermark = Util.getTimeWatermark();
                    drawWaterMark(Device.isSupportedNewStyleTimeWaterMark() ? new NewStyleTextWaterMark(timeWatermark, i3, i4, i5) : new TextWaterMark(timeWatermark, i3, i4, i5), i, i2, i5);
                }
                if (CameraSettings.isDualCameraWaterMarkOpen(CameraSettingPreferences.instance())) {
                    drawWaterMark(new ImageWaterMark(SnapshotEffectRender.this.mDualCameraWaterMark, i3, i4, i5), i, i2, i5);
                }
            }
        }

        private void drawWaterMark(WaterMark waterMark, int i, int i2, int i3) {
            this.mGLCanvas.getState().pushState();
            if (i3 != 0) {
                this.mGLCanvas.getState().translate((float) (waterMark.getCenterX() + i), (float) (waterMark.getCenterY() + i2));
                this.mGLCanvas.getState().rotate((float) (-i3), 0.0f, 0.0f, 1.0f);
                this.mGLCanvas.getState().translate((float) ((-i) - waterMark.getCenterX()), (float) ((-i2) - waterMark.getCenterY()));
            }
            this.mGLCanvas.getBasicRender().draw(new DrawBasicTexAttribute(waterMark.getTexture(), waterMark.getLeft(), waterMark.getTop(), waterMark.getWidth(), waterMark.getHeight()));
            this.mGLCanvas.getState().popState();
        }

        private Render getEffectRender(int i) {
            RenderGroup effectRenderGroup = this.mGLCanvas.getEffectRenderGroup();
            if (effectRenderGroup.getRender(i) == null) {
                this.mGLCanvas.prepareEffectRenders(false, i);
            }
            return effectRenderGroup.getRender(i);
        }

        private void initEGL() {
            SnapshotEffectRender.this.mEgl = (EGL10) EGLContext.getEGL();
            SnapshotEffectRender.this.mEglDisplay = SnapshotEffectRender.this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (SnapshotEffectRender.this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }
            int[] iArr = new int[2];
            if (SnapshotEffectRender.this.mEgl.eglInitialize(SnapshotEffectRender.this.mEglDisplay, iArr)) {
                Log.v("SnapshotEffectProcessor", "EGL version: " + iArr[0] + '.' + iArr[1]);
                int[] iArr2 = new int[]{12440, 2, 12344};
                SnapshotEffectRender.this.mEglConfig = SnapshotEffectRender.chooseConfig(SnapshotEffectRender.this.mEgl, SnapshotEffectRender.this.mEglDisplay);
                SnapshotEffectRender.this.mEglContext = SnapshotEffectRender.this.mEgl.eglCreateContext(SnapshotEffectRender.this.mEglDisplay, SnapshotEffectRender.this.mEglConfig, EGL10.EGL_NO_CONTEXT, iArr2);
                if (SnapshotEffectRender.this.mEglContext == null || SnapshotEffectRender.this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    throw new RuntimeException("failed to createContext");
                }
                SnapshotEffectRender.this.mEglSurface = SnapshotEffectRender.this.mEgl.eglCreatePbufferSurface(SnapshotEffectRender.this.mEglDisplay, SnapshotEffectRender.this.mEglConfig, new int[]{12375, Util.sWindowWidth, 12374, Util.sWindowHeight, 12344});
                if (SnapshotEffectRender.this.mEglSurface == null || SnapshotEffectRender.this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                    throw new RuntimeException("failed to createWindowSurface");
                } else if (!SnapshotEffectRender.this.mEgl.eglMakeCurrent(SnapshotEffectRender.this.mEglDisplay, SnapshotEffectRender.this.mEglSurface, SnapshotEffectRender.this.mEglSurface, SnapshotEffectRender.this.mEglContext)) {
                    throw new RuntimeException("failed to eglMakeCurrent");
                } else {
                    return;
                }
            }
            throw new RuntimeException("eglInitialize failed");
        }

        private void release() {
            SnapshotEffectRender.this.mRelease = true;
            SnapshotEffectRender.this.mReleasePending = false;
            SnapshotEffectRender.this.mEgl.eglDestroySurface(SnapshotEffectRender.this.mEglDisplay, SnapshotEffectRender.this.mEglSurface);
            SnapshotEffectRender.this.mEgl.eglDestroyContext(SnapshotEffectRender.this.mEglDisplay, SnapshotEffectRender.this.mEglContext);
            SnapshotEffectRender.this.mEgl.eglMakeCurrent(SnapshotEffectRender.this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            SnapshotEffectRender.this.mEgl.eglTerminate(SnapshotEffectRender.this.mEglDisplay);
            SnapshotEffectRender.this.mEglSurface = null;
            SnapshotEffectRender.this.mEglContext = null;
            SnapshotEffectRender.this.mEglDisplay = null;
            SnapshotEffectRender.this.mActivity = null;
            this.mFrameBuffer = null;
            System.gc();
            this.mGLCanvas.recycledResources();
            SnapshotEffectRender.this.mEglThread.quit();
            this.mGLCanvas = null;
        }

        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 0:
                    initEGL();
                    this.mGLCanvas = new SnapshotCanvas();
                    this.mGLCanvas.setSize(SnapshotEffectRender.this.mActivity.getCameraScreenNail().getWidth(), SnapshotEffectRender.this.mActivity.getCameraScreenNail().getHeight());
                    SnapshotEffectRender.this.mEglThreadBlockVar.open();
                    return;
                case 1:
                    drawMainJpeg((DrawJPEGAttribute) message.obj, true);
                    this.mGLCanvas.recycledResources();
                    if (SnapshotEffectRender.this.mReleasePending && !hasMessages(1)) {
                        release();
                    }
                    synchronized (SnapshotEffectRender.this.mLock) {
                        SnapshotEffectRender snapshotEffectRender = SnapshotEffectRender.this;
                        snapshotEffectRender.mJpegQueueSize = snapshotEffectRender.mJpegQueueSize - 1;
                    }
                    return;
                case 2:
                    DrawJPEGAttribute drawJPEGAttribute = (DrawJPEGAttribute) message.obj;
                    if (message.arg1 <= 0) {
                        z = false;
                    }
                    if (z) {
                        drawThumbJpeg(drawJPEGAttribute, false);
                    }
                    drawMainJpeg(drawJPEGAttribute, false);
                    this.mGLCanvas.recycledResources();
                    SnapshotEffectRender.this.mEglThreadBlockVar.open();
                    return;
                case 3:
                    drawThumbJpeg((DrawJPEGAttribute) message.obj, true);
                    return;
                case 4:
                    drawThumbJpeg((DrawJPEGAttribute) message.obj, true);
                    SnapshotEffectRender.this.mEglThreadBlockVar.open();
                    return;
                case 5:
                    release();
                    return;
                case 6:
                    this.mGLCanvas.prepareEffectRenders(false, message.arg1);
                    return;
                default:
                    return;
            }
        }

        public void sendMessageSync(int i) {
            SnapshotEffectRender.this.mEglThreadBlockVar.close();
            sendEmptyMessage(i);
            SnapshotEffectRender.this.mEglThreadBlockVar.block();
        }
    }

    private abstract class WaterMark {
        protected int mOrientation;
        protected int mPictureHeight;
        protected int mPictureWidth;

        public WaterMark(int i, int i2, int i3) {
            this.mPictureWidth = i;
            this.mPictureHeight = i2;
            this.mOrientation = i3;
        }

        public abstract int getCenterX();

        public abstract int getCenterY();

        public abstract int getHeight();

        public int getLeft() {
            return getCenterX() - (getWidth() / 2);
        }

        public abstract BasicTexture getTexture();

        public int getTop() {
            return getCenterY() - (getHeight() / 2);
        }

        public abstract int getWidth();
    }

    private class ImageWaterMark extends WaterMark {
        private int mCenterX;
        private int mCenterY;
        private int mHeight;
        private BitmapTexture mImageTexture;
        private int mPadding;
        private int mWidth;

        public ImageWaterMark(Bitmap bitmap, int i, int i2, int i3) {
            super(i, i2, i3);
            float min = ((float) Math.min(i, i2)) / 1080.0f;
            this.mHeight = ((int) Math.round(((double) min) * 57.294429708d)) & -2;
            this.mWidth = ((this.mHeight * bitmap.getWidth()) / bitmap.getHeight()) & -2;
            this.mPadding = ((int) Math.round(((double) min) * 46.551724138d)) & -2;
            this.mImageTexture = new BitmapTexture(bitmap);
            this.mImageTexture.setOpaque(false);
            calcCenterAxis();
        }

        private void calcCenterAxis() {
            switch (this.mOrientation) {
                case 0:
                    this.mCenterX = this.mPadding + (getWidth() / 2);
                    this.mCenterY = (this.mPictureHeight - this.mPadding) - (getHeight() / 2);
                    return;
                case 90:
                    this.mCenterX = (this.mPictureWidth - this.mPadding) - (getHeight() / 2);
                    this.mCenterY = (this.mPictureHeight - this.mPadding) - (getWidth() / 2);
                    return;
                case 180:
                    this.mCenterX = (this.mPictureWidth - this.mPadding) - (getWidth() / 2);
                    this.mCenterY = this.mPadding + (getHeight() / 2);
                    return;
                case 270:
                    this.mCenterX = this.mPadding + (getHeight() / 2);
                    this.mCenterY = this.mPadding + (getWidth() / 2);
                    return;
                default:
                    return;
            }
        }

        public int getCenterX() {
            return this.mCenterX;
        }

        public int getCenterY() {
            return this.mCenterY;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public BasicTexture getTexture() {
            return this.mImageTexture;
        }

        public int getWidth() {
            return this.mWidth;
        }
    }

    private class NewStyleTextWaterMark extends WaterMark {
        private final float TEXT_PIXEL_SIZE;
        private int mCenterX;
        private int mCenterY;
        private int mCharMargin;
        private int mHorizontalPadding;
        private int mPadding;
        private int mVerticalPadding;
        private int mWaterHeight;
        private String mWaterText;
        private BasicTexture mWaterTexture;
        private int mWaterWidth;

        private NewStyleTextWaterMark(String str, int i, int i2, int i3) {
            super(i, i2, i3);
            this.TEXT_PIXEL_SIZE = 30.079576f;
            float min = ((float) Math.min(i, i2)) / 1080.0f;
            this.mWaterText = str;
            this.mWaterTexture = StringTexture.newInstance(this.mWaterText, 30.079576f * min, -1, 2);
            this.mWaterWidth = this.mWaterTexture.getWidth();
            this.mWaterHeight = this.mWaterTexture.getHeight();
            this.mPadding = (int) Math.round(((double) min) * 43.687002653d);
            this.mCharMargin = (int) ((((float) this.mWaterHeight) * 0.13f) / 2.0f);
            this.mHorizontalPadding = this.mPadding & -2;
            this.mVerticalPadding = (this.mPadding - this.mCharMargin) & -2;
            calcCenterAxis();
            if (Util.sIsDumpLog) {
                print();
            }
        }

        private void calcCenterAxis() {
            switch (this.mOrientation) {
                case 0:
                    this.mCenterX = (this.mPictureWidth - this.mHorizontalPadding) - (this.mWaterWidth / 2);
                    this.mCenterY = (this.mPictureHeight - this.mVerticalPadding) - (this.mWaterHeight / 2);
                    return;
                case 90:
                    this.mCenterX = (this.mPictureWidth - this.mVerticalPadding) - (this.mWaterHeight / 2);
                    this.mCenterY = this.mHorizontalPadding + (this.mWaterWidth / 2);
                    return;
                case 180:
                    this.mCenterX = this.mHorizontalPadding + (this.mWaterWidth / 2);
                    this.mCenterY = this.mVerticalPadding + (this.mWaterHeight / 2);
                    return;
                case 270:
                    this.mCenterX = this.mVerticalPadding + (this.mWaterHeight / 2);
                    this.mCenterY = (this.mPictureHeight - this.mHorizontalPadding) - (this.mWaterWidth / 2);
                    return;
                default:
                    return;
            }
        }

        private void print() {
            Log.v("SnapshotEffectProcessor", "WaterMark mPictureWidth=" + this.mPictureWidth + " mPictureHeight =" + this.mPictureHeight + " mWaterText=" + this.mWaterText + " mCenterX=" + this.mCenterX + " mCenterY=" + this.mCenterY + " mWaterWidth=" + this.mWaterWidth + " mWaterHeight=" + this.mWaterHeight + " mPadding=" + this.mPadding);
        }

        public int getCenterX() {
            return this.mCenterX;
        }

        public int getCenterY() {
            return this.mCenterY;
        }

        public int getHeight() {
            return this.mWaterHeight;
        }

        public BasicTexture getTexture() {
            return this.mWaterTexture;
        }

        public int getWidth() {
            return this.mWaterWidth;
        }
    }

    private class Size {
        public int height;
        public int width;

        Size(int i, int i2) {
            this.width = i;
            this.height = i2;
        }
    }

    private class TextWaterMark extends WaterMark {
        private final int[][] PIC_WIDTHS;
        private final int[][] WATERMARK_FONT_SIZES;
        private int mCenterX;
        private int mCenterY;
        private int mCharMargin;
        private int mFontIndex;
        private int mPadding;
        private int mWaterHeight;
        private String mWaterText;
        private BasicTexture mWaterTexture;
        private int mWaterWidth;

        private TextWaterMark(String str, int i, int i2, int i3) {
            super(i, i2, i3);
            this.WATERMARK_FONT_SIZES = new int[][]{new int[]{5, 4, 2, 4, 3, 7}, new int[]{8, 6, 2, 6, 3, 7}, new int[]{11, 6, 5, 6, 5, 12}, new int[]{12, 7, 5, 7, 5, 12}, new int[]{50, 32, 11, 31, 20, 47}, new int[]{58, 36, 19, 38, 24, 55}, new int[]{65, 41, 24, 42, 27, 63}, new int[]{80, 50, 24, 50, 32, 75}, new int[]{83, 52, 25, 52, 33, 78}, new int[]{104, 65, 33, 65, 42, 98}, new int[]{128, 80, 40, 80, 48, 132}};
            this.PIC_WIDTHS = new int[][]{new int[]{0, 149}, new int[]{150, 239}, new int[]{240, 279}, new int[]{280, 400}, new int[]{401, 1439}, new int[]{1440, 1511}, new int[]{1512, 1799}, new int[]{1800, 1899}, new int[]{1900, 2299}, new int[]{2300, 3120}, new int[]{3121, 4000}};
            this.mWaterText = str;
            this.mWaterTexture = StringTexture.newInstance(this.mWaterText, 144.0f, -262152, 0.0f, false, 1);
            this.mFontIndex = getFontIndex(i, i2);
            this.mWaterWidth = getWaterMarkWidth(this.mWaterText, this.mFontIndex);
            this.mWaterHeight = (int) (((float) this.WATERMARK_FONT_SIZES[this.mFontIndex][0]) / 0.82f);
            this.mPadding = this.WATERMARK_FONT_SIZES[this.mFontIndex][5];
            this.mCharMargin = (int) ((((float) this.mWaterHeight) * 0.18f) / 2.0f);
            calcCenterAxis();
            if (Util.sIsDumpLog) {
                print();
            }
        }

        private void calcCenterAxis() {
            switch (this.mOrientation) {
                case 0:
                    this.mCenterX = (this.mPictureWidth - this.mPadding) - (this.mWaterWidth / 2);
                    this.mCenterY = ((this.mPictureHeight - this.mPadding) - (this.mWaterHeight / 2)) + this.mCharMargin;
                    return;
                case 90:
                    this.mCenterX = ((this.mPictureWidth - this.mPadding) - (this.mWaterHeight / 2)) + this.mCharMargin;
                    this.mCenterY = this.mPadding + (this.mWaterWidth / 2);
                    return;
                case 180:
                    this.mCenterX = this.mPadding + (this.mWaterWidth / 2);
                    this.mCenterY = (this.mPadding + (this.mWaterHeight / 2)) - this.mCharMargin;
                    return;
                case 270:
                    this.mCenterX = (this.mPadding + (this.mWaterHeight / 2)) - this.mCharMargin;
                    this.mCenterY = (this.mPictureHeight - this.mPadding) - (this.mWaterWidth / 2);
                    return;
                default:
                    return;
            }
        }

        private int getFontIndex(int i, int i2) {
            int min = Math.min(i, i2);
            int length = this.WATERMARK_FONT_SIZES.length - 1;
            int i3 = 0;
            while (i3 < this.PIC_WIDTHS.length) {
                if (min >= this.PIC_WIDTHS[i3][0] && min <= this.PIC_WIDTHS[i3][1]) {
                    return i3;
                }
                i3++;
            }
            return length;
        }

        private int getWaterMarkWidth(String str, int i) {
            int i2 = this.WATERMARK_FONT_SIZES[i][1];
            int i3 = this.WATERMARK_FONT_SIZES[i][2];
            int i4 = this.WATERMARK_FONT_SIZES[i][3];
            int i5 = this.WATERMARK_FONT_SIZES[i][4];
            int i6 = 0;
            for (char c : str.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    i6 += i2;
                } else if (c == ':') {
                    i6 += i5;
                } else if (c == '-') {
                    i6 += i3;
                } else if (c == ' ') {
                    i6 += i4;
                }
            }
            return i6;
        }

        private void print() {
            Log.v("SnapshotEffectProcessor", "WaterMark mPictureWidth=" + this.mPictureWidth + " mPictureHeight =" + this.mPictureHeight + " mWaterText=" + this.mWaterText + " mFontIndex=" + this.mFontIndex + " mCenterX=" + this.mCenterX + " mCenterY=" + this.mCenterY + " mWaterWidth=" + this.mWaterWidth + " mWaterHeight=" + this.mWaterHeight + " mPadding=" + this.mPadding);
        }

        public int getCenterX() {
            return this.mCenterX;
        }

        public int getCenterY() {
            return this.mCenterY;
        }

        public int getHeight() {
            return this.mWaterHeight;
        }

        public BasicTexture getTexture() {
            return this.mWaterTexture;
        }

        public int getWidth() {
            return this.mWaterWidth;
        }
    }

    public SnapshotEffectRender(ActivityBase activityBase, boolean z) {
        this.mActivity = activityBase;
        this.mIsImageCaptureIntent = z;
        this.mEglThread = new HandlerThread("SnapshotEffectProcessor");
        this.mEglThread.start();
        this.mEglHandler = new EGLHandler(this.mEglThread.getLooper());
        this.mEglHandler.sendMessageSync(0);
        this.mRelease = false;
        if (CameraSettings.isSupportedOpticalZoom()) {
            Options options = new Options();
            options.inScaled = false;
            options.inPurgeable = true;
            options.inPremultiplied = false;
            this.mDualCameraWaterMark = BitmapFactory.decodeResource(this.mActivity.getResources(), C0049R.raw.dualcamera_watermark, options);
        }
    }

    private static EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay) {
        int[] iArr = new int[1];
        if (egl10.eglChooseConfig(eGLDisplay, CONFIG_SPEC, null, 0, iArr)) {
            int i = iArr[0];
            if (i <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }
            EGLConfig[] eGLConfigArr = new EGLConfig[i];
            if (egl10.eglChooseConfig(eGLDisplay, CONFIG_SPEC, eGLConfigArr, i, iArr)) {
                return eGLConfigArr[0];
            }
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        throw new IllegalArgumentException("eglChooseConfig failed");
    }

    private ExifInterface getExif(byte[] bArr) {
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(bArr);
        } catch (IOException e) {
            Log.d("SnapshotEffectProcessor", e.getMessage());
        }
        return exifInterface;
    }

    private void processorThumAsync(DrawJPEGAttribute drawJPEGAttribute) {
        if (this.mExifNeeded) {
            this.mEglHandler.obtainMessage(3, drawJPEGAttribute).sendToTarget();
        } else {
            drawJPEGAttribute.mUri = Storage.newImage(this.mActivity, drawJPEGAttribute.mTitle, drawJPEGAttribute.mDate, drawJPEGAttribute.mJpegOrientation, drawJPEGAttribute.mPreviewWidth, drawJPEGAttribute.mPreviewHeight);
        }
    }

    private void processorThumSync(DrawJPEGAttribute drawJPEGAttribute) {
        if (this.mExifNeeded) {
            drawJPEGAttribute.mExif = getExif(drawJPEGAttribute.mData);
            if (drawJPEGAttribute.mExif.getThumbnailBytes() != null) {
                this.mEglThreadBlockVar.close();
                this.mEglHandler.obtainMessage(4, drawJPEGAttribute).sendToTarget();
                this.mEglThreadBlockVar.block();
                return;
            }
        }
        drawJPEGAttribute.mUri = Storage.newImage(this.mActivity, drawJPEGAttribute.mTitle, drawJPEGAttribute.mDate, drawJPEGAttribute.mJpegOrientation, drawJPEGAttribute.mPreviewWidth, drawJPEGAttribute.mPreviewHeight);
    }

    public void changeJpegTitle(String str, String str2) {
        if (str2 != null && str2.length() != 0) {
            synchronized (this) {
                this.mTitleMap.put(str2, str);
            }
        }
    }

    public void prepareEffectRender(int i) {
        this.mEglHandler.obtainMessage(6, i, 0).sendToTarget();
    }

    public boolean processorJpegAsync(DrawJPEGAttribute drawJPEGAttribute) {
        Log.d("SnapshotEffectProcessor", "queueSize=" + this.mJpegQueueSize);
        if (this.mJpegQueueSize >= 7) {
            Log.d("SnapshotEffectProcessor", "queueSize is full, drop it " + drawJPEGAttribute.mTitle);
            return false;
        }
        boolean z = this.mJpegQueueSize == 0;
        if (z) {
            processorThumSync(drawJPEGAttribute);
        } else {
            processorThumAsync(drawJPEGAttribute);
        }
        if (!this.mIsImageCaptureIntent && z && this.mExifNeeded) {
            Bitmap thumbnailBitmap = drawJPEGAttribute.mExif.getThumbnailBitmap();
            if (!(thumbnailBitmap == null || drawJPEGAttribute.mUri == null)) {
                drawJPEGAttribute.mFinalImage = false;
                this.mActivity.getThumbnailUpdater().setThumbnail(Thumbnail.createThumbnail(drawJPEGAttribute.mUri, thumbnailBitmap, drawJPEGAttribute.mJpegOrientation, false));
            }
        }
        synchronized (this.mLock) {
            this.mJpegQueueSize++;
        }
        this.mEglHandler.obtainMessage(1, drawJPEGAttribute).sendToTarget();
        return true;
    }

    public void processorJpegSync(DrawJPEGAttribute drawJPEGAttribute) {
        this.mEglThreadBlockVar.close();
        this.mEglHandler.obtainMessage(2, this.mExifNeeded ? 1 : 0, 0, drawJPEGAttribute).sendToTarget();
        this.mEglThreadBlockVar.block();
    }

    public void release() {
        if (this.mEglHandler.hasMessages(1)) {
            this.mReleasePending = true;
        } else {
            this.mEglHandler.sendEmptyMessage(5);
        }
    }

    public void setImageSaver(ImageSaver imageSaver) {
        this.mImageSaver = imageSaver;
    }

    public void setQuality(int i) {
        if (i > 0 && i < 100) {
            this.mQuality = i;
        }
    }
}
