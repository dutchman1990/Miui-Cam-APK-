package com.android.camera.snap;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.recyclerview.C0049R;
import android.view.OrientationEventListener;
import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Exif;
import com.android.camera.LocationManager;
import com.android.camera.Log;
import com.android.camera.PictureSize;
import com.android.camera.PictureSizeManager;
import com.android.camera.Util;
import com.android.camera.module.VideoModule;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.Storage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;

public class SnapCamera implements OnErrorListener, OnInfoListener {
    private static final String TAG = SnapCamera.class.getSimpleName();
    private ContentValues contentValues = null;
    private CameraProxy mCamera;
    private int mCameraId;
    private Context mContext;
    private int mHeight;
    private boolean mIsCamcorder = false;
    private int mLastAngle = 0;
    private MediaRecorder mMediaRecorder;
    private OrientationEventListener mOrientationListener;
    private PictureCallback mPicture = new C01221();
    private CamcorderProfile mProfile;
    private boolean mRecording = false;
    private SnapStatusListener mStatusListener;
    private SurfaceTexture mSurface;
    private int mWidth;

    class C01221 implements PictureCallback {
        C01221() {
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            try {
                Location currentLocation = LocationManager.instance().getCurrentLocation();
                String str = Util.createJpegName(System.currentTimeMillis()) + "_SNAP";
                int orientation = Exif.getOrientation(bArr);
                Uri addImage = Storage.addImage(SnapCamera.this.mContext, str, System.currentTimeMillis(), currentLocation, orientation, bArr, SnapCamera.this.mWidth, SnapCamera.this.mHeight, false, false, false);
                if (addImage != null) {
                }
                if (SnapCamera.this.mStatusListener != null) {
                    SnapCamera.this.mStatusListener.onDone(addImage);
                }
                if (SnapCamera.this.mCamera != null) {
                    SnapCamera.this.mCamera.startPreview();
                }
            } catch (Exception e) {
                Log.m2e(SnapCamera.TAG, "save picture failed " + e.getMessage());
            }
        }
    }

    public interface SnapStatusListener {
        void onDone(Uri uri);
    }

    public SnapCamera(Context context, SnapStatusListener snapStatusListener) {
        try {
            LocationManager.instance().recordLocation(CameraSettings.isRecordLocation(CameraSettingPreferences.instance()));
            this.mStatusListener = snapStatusListener;
            this.mContext = context;
            initSnapType();
            initOrientationListener();
            initCamera();
        } catch (Exception e) {
            Log.m2e(TAG, "init failed" + e.getMessage());
        }
    }

    private void initCamera() {
        try {
            Constructor declaredConstructor = Class.forName("android.graphics.SurfaceTexture").getDeclaredConstructor(new Class[]{Boolean.TYPE});
            declaredConstructor.setAccessible(true);
            this.mSurface = (SurfaceTexture) declaredConstructor.newInstance(new Object[]{Boolean.valueOf(false)});
            this.mCameraId = 0;
            if (System.getInt(this.mContext.getContentResolver(), "persist.camera.snap.auto_switch", 0) == 1) {
                this.mCameraId = CameraSettings.readPreferredCameraId(CameraSettingPreferences.instance());
            }
            this.mCamera = CameraHolder.instance().open(this.mCameraId, false);
            this.mCamera.setPreviewTexture(this.mSurface);
            Parameters parameters = this.mCamera.getParameters();
            if (isCamcorder()) {
                this.mProfile = CamcorderProfile.get(this.mCameraId, CameraSettings.getPreferVideoQuality());
                parameters.set("video-size", this.mProfile.videoFrameWidth + "x" + this.mProfile.videoFrameHeight);
                parameters.set("camera-service-mute", "true");
                parameters.setFocusMode("continuous-video");
                parameters.setRecordingHint(true);
            } else {
                PictureSizeManager.initialize(null, parameters.getSupportedPictureSizes(), 0);
                PictureSize bestPictureSize = PictureSizeManager.getBestPictureSize();
                this.mWidth = bestPictureSize.width;
                this.mHeight = bestPictureSize.height;
                parameters.setPictureSize(this.mWidth, this.mHeight);
                parameters.setRotation(this.mLastAngle);
                parameters.set("zsl", "on");
                parameters.setFocusMode("continuous-picture");
                parameters.set("street-snap-mode", "on");
                parameters.set("no-display-mode", 1);
            }
            this.mCamera.setParameters(parameters);
            if (!isCamcorder()) {
                this.mCamera.startPreview();
            }
        } catch (CameraHardwareException e) {
            Log.m2e(TAG, "camera init failed " + e.getMessage());
        } catch (ClassNotFoundException e2) {
            Log.m2e(TAG, "reflecting constructor of SurfaceTexture failed. " + e2.getMessage());
        } catch (InvocationTargetException e3) {
            Log.m2e(TAG, "reflecting constructor of SurfaceTexture failed. " + e3.getMessage());
        } catch (NoSuchMethodException e4) {
            Log.m2e(TAG, "reflecting constructor of SurfaceTexture failed. " + e4.getMessage());
        } catch (InstantiationException e5) {
            Log.m2e(TAG, "reflecting constructor of SurfaceTexture failed. " + e5.getMessage());
        } catch (IllegalAccessException e6) {
            Log.m2e(TAG, "reflecting constructor of SurfaceTexture failed. " + e6.getMessage());
        }
    }

    private void initOrientationListener() {
        Context context = this.mContext;
        int i = (Device.IS_D4 || Device.IS_C1 || Device.IS_D5) ? 2 : 3;
        this.mOrientationListener = new OrientationEventListener(context, i) {
            public void onOrientationChanged(int i) {
                int i2 = (45 > i || i >= 135) ? (135 > i || i >= 225) ? (225 > i || i >= 315) ? 90 : 0 : 270 : 180;
                CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[SnapCamera.this.mCameraId];
                int i3 = i2 != -1 ? cameraInfo.facing == 1 ? (360 - i2) % 360 : i2 % 360 : cameraInfo.orientation;
                if (SnapCamera.this.mLastAngle != i3) {
                    SnapCamera.this.updateCameraOrientation(i3);
                    SnapCamera.this.mLastAngle = i3;
                }
            }
        };
        if (this.mOrientationListener.canDetectOrientation()) {
            Log.m0d(TAG, "Can detect orientation");
            this.mOrientationListener.enable();
            return;
        }
        Log.m0d(TAG, "Cannot detect orientation");
        this.mOrientationListener.disable();
    }

    private void initSnapType() {
        String string = Secure.getString(this.mContext.getContentResolver(), "key_long_press_volume_down");
        if (string.equals("Street-snap-picture")) {
            this.mIsCamcorder = false;
        } else if (string.equals("Street-snap-movie")) {
            this.mIsCamcorder = true;
        } else {
            this.mIsCamcorder = false;
        }
    }

    public static boolean isSnapEnabled(Context context) {
        String string = CameraSettingPreferences.instance().getString("pref_camera_snap_key", null);
        if (string != null) {
            Secure.putString(context.getContentResolver(), "key_long_press_volume_down", CameraSettings.getMiuiSettingsKeyForStreetSnap(string));
            CameraSettingPreferences.instance().edit().remove("pref_camera_snap_key").apply();
        }
        String string2 = Secure.getString(context.getContentResolver(), "key_long_press_volume_down");
        return ("public_transportation_shortcuts".equals(string2) || "none".equals(string2)) ? false : true;
    }

    private void stopCamcorder() {
        if (this.mMediaRecorder != null) {
            if (this.mRecording) {
                try {
                    this.mMediaRecorder.stop();
                } catch (IllegalStateException e) {
                    this.mRecording = false;
                    e.printStackTrace();
                }
            }
            this.mMediaRecorder.reset();
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
        if (this.mRecording) {
            Uri uri = null;
            try {
                uri = this.mContext.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, this.contentValues);
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.m2e(TAG, "Failed to write MediaStore" + e2);
            }
            if (this.mStatusListener != null) {
                this.mStatusListener.onDone(uri);
            }
        }
        this.mRecording = false;
    }

    public boolean isCamcorder() {
        return this.mIsCamcorder;
    }

    public void onError(MediaRecorder mediaRecorder, int i, int i2) {
        stopCamcorder();
    }

    public void onInfo(MediaRecorder mediaRecorder, int i, int i2) {
        if (i == 800 || i == 801) {
            Log.m0d(TAG, "duration or file size reach MAX");
            stopCamcorder();
        }
    }

    public void release() {
        try {
            this.mLastAngle = 0;
            LocationManager.instance().recordLocation(false);
            if (this.mOrientationListener != null) {
                this.mOrientationListener.disable();
                this.mOrientationListener = null;
            }
        } catch (Exception e) {
        }
        try {
            stopCamcorder();
        } catch (Exception e2) {
        }
        try {
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
        } catch (Exception e3) {
        }
        try {
            if (this.mCamera != null) {
                this.mCamera.setZoomChangeListener(null);
                this.mCamera.setFaceDetectionListener(null);
                this.mCamera.setErrorCallback(null);
                this.mCamera.setOneShotPreviewCallback(null);
                this.mCamera.setAutoFocusMoveCallback(null);
                this.mCamera.addRawImageCallbackBuffer(null);
                this.mCamera.removeAllAsyncMessage();
                CameraHolder.instance().release();
                this.mCamera = null;
            }
        } catch (Exception e4) {
        }
    }

    public void startCamcorder() {
        try {
            this.mMediaRecorder = new MediaRecorder();
            this.mCamera.unlock();
            this.mMediaRecorder.setCamera(this.mCamera.getCamera());
            this.mMediaRecorder.setAudioSource(5);
            this.mMediaRecorder.setVideoSource(1);
            this.mProfile.duration = 300000;
            this.mMediaRecorder.setProfile(this.mProfile);
            this.mMediaRecorder.setMaxDuration(this.mProfile.duration);
            Location currentLocation = LocationManager.instance().getCurrentLocation();
            if (currentLocation != null) {
                this.mMediaRecorder.setLocation((float) currentLocation.getLatitude(), (float) currentLocation.getLongitude());
            }
            long currentTimeMillis = System.currentTimeMillis();
            String format = new SimpleDateFormat(this.mContext.getString(C0049R.string.video_file_name_format)).format(Long.valueOf(currentTimeMillis));
            String str = format + "_SNAP" + VideoModule.convertOutputFormatToFileExt(this.mProfile.fileFormat);
            String convertOutputFormatToMimeType = VideoModule.convertOutputFormatToMimeType(this.mProfile.fileFormat);
            String str2 = Storage.DIRECTORY + '/' + str;
            this.contentValues = new ContentValues(7);
            this.contentValues.put("title", format);
            this.contentValues.put("_display_name", str);
            this.contentValues.put("datetaken", Long.valueOf(currentTimeMillis));
            this.contentValues.put("mime_type", convertOutputFormatToMimeType);
            this.contentValues.put("_data", str2);
            this.contentValues.put("resolution", Integer.toString(this.mProfile.videoFrameWidth) + "x" + Integer.toString(this.mProfile.videoFrameHeight));
            if (currentLocation != null) {
                this.contentValues.put("latitude", Double.valueOf(currentLocation.getLatitude()));
                this.contentValues.put("longitude", Double.valueOf(currentLocation.getLongitude()));
            }
            Log.m0d(TAG, "save to " + str2);
            this.mMediaRecorder.setOutputFile(str2);
            long availableSpace = Storage.getAvailableSpace() - 52428800;
            if (3670016000L < availableSpace) {
                Log.m0d(TAG, "need reduce , now maxFileSize = " + availableSpace);
                availableSpace = 3670016000L;
            }
            if (availableSpace < VideoModule.VIDEO_MIN_SINGLE_FILE_SIZE) {
                availableSpace = VideoModule.VIDEO_MIN_SINGLE_FILE_SIZE;
            }
            try {
                this.mMediaRecorder.setMaxFileSize(availableSpace);
            } catch (RuntimeException e) {
            }
            Log.m0d(TAG, "set orientation to " + this.mLastAngle);
            this.mMediaRecorder.setOrientationHint(this.mLastAngle);
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.setOnErrorListener(this);
            this.mMediaRecorder.setOnInfoListener(this);
            this.mMediaRecorder.start();
            this.mRecording = true;
        } catch (Exception e2) {
            Log.m2e(TAG, "prepare or start failed " + e2.getMessage());
            stopCamcorder();
            this.mCamera.lock();
        }
    }

    public void takeSnap() {
        try {
            this.mCamera.takePicture(null, null, null, this.mPicture);
        } catch (Exception e) {
            Log.m2e(TAG, "take picture failed" + e.getMessage());
        }
    }

    public void updateCameraOrientation(int i) {
        if (!isCamcorder() && this.mCamera != null) {
            Parameters parameters = this.mCamera.getParameters();
            parameters.setRotation(i);
            this.mCamera.setParameters(parameters);
        }
    }
}
