package com.android.camera.camera_adapter;

import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Exif;
import com.android.camera.JpegEncodingQualityMappings;
import com.android.camera.MtkFBParamsUtil;
import com.android.camera.PictureSize;
import com.android.camera.PictureSizeManager;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.CameraHardwareProxy.CameraHardwareFace;
import com.android.camera.hardware.CameraHardwareProxy.ContinuousShotCallback;
import com.android.camera.hardware.MTKCameraProxy;
import com.android.camera.hardware.MTKCameraProxy.StereoDataCallback;
import com.android.camera.module.BaseModule;
import com.android.camera.module.CameraModule;
import com.android.camera.stereo.StereoDataGroup;
import com.android.camera.stereo.WarningCallback;
import com.android.camera.ui.FaceView;
import java.util.List;

public class CameraMTK extends CameraModule {
    private static MTKCameraProxy sProxy = ((MTKCameraProxy) CameraHardwareProxy.getDeviceProxy());
    private byte[] mClearImage;
    private ContinuousShotCallback mContinuousShotCallback;
    private FBParams mCurrentFBParams;
    private int mCurrentNum;
    private byte[] mDepthMap;
    private FaceNo mFaceNo;
    private FBParams mInUseFBParams;
    private boolean mIsLongShotMode;
    private boolean mIsMTKFaceBeautyMode;
    private boolean mIsStereoCapture;
    private StereoPictureCallback mJpegPictureCB;
    private byte[] mJpsData;
    private byte[] mLdcData;
    private byte[] mMaskAndConfigData;
    private final Object mOperator;
    private byte[] mOriginalJpegData;
    private SaveHandler mSaveHandler;
    private final WarningCallback mStereoCameraWarningCallback;
    private final StereoPhotoDataCallback mStereoPhotoDataCallback;

    class C00931 implements ContinuousShotCallback {
        C00931() {
        }

        public void onContinuousShotDone(int i) {
            Log.d("Camera", "onContinuousShotDone: capNum=" + i);
            CameraMTK.this.mHandler.removeMessages(37);
            CameraMTK.this.handleMultiSnapDone();
        }
    }

    public enum FBLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    public class FBParams {
        public int enlargeEye;
        public int skinColor;
        public int slimFace;
        public int smoothLevel;

        public void copy(FBParams fBParams) {
            if (fBParams != null) {
                this.skinColor = fBParams.skinColor;
                this.smoothLevel = fBParams.smoothLevel;
                this.slimFace = fBParams.slimFace;
                this.enlargeEye = fBParams.enlargeEye;
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FBParams)) {
                return false;
            }
            FBParams fBParams = (FBParams) obj;
            return this.enlargeEye == fBParams.enlargeEye && this.skinColor == fBParams.skinColor && this.slimFace == fBParams.slimFace && this.smoothLevel == fBParams.smoothLevel;
        }

        public int hashCode() {
            return (((((this.skinColor * 31) + this.smoothLevel) * 31) + this.slimFace) * 31) + this.enlargeEye;
        }
    }

    public enum FaceNo {
        NONE,
        SINGLE,
        MULTIPLE
    }

    class MtkCategory extends CameraCategory {
        public MtkCategory() {
            super();
        }

        public void takePicture(Location location) {
            if (Device.isSupportedStereo() && CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                Log.d("Camera", "takePicture " + CameraMTK.this.mStereoCameraWarningCallback.isDualCameraReady());
                CameraMTK.this.mIsStereoCapture = CameraMTK.this.mStereoCameraWarningCallback.isDualCameraReady();
                CameraMTK.this.mJpegPictureCB.setLocation(location);
                CameraMTK.this.mCameraDevice.setStereoDataCallback(CameraMTK.this.mStereoPhotoDataCallback);
                CameraMTK.this.mCameraDevice.takePicture(CameraMTK.this.mShutterCallback, null, null, CameraMTK.this.mJpegPictureCB);
                return;
            }
            super.takePicture(location);
        }
    }

    private class SaveHandler extends Handler {
        private byte[] mXmpJpegData;

        SaveHandler(Looper looper) {
            super(looper);
        }

        private void saveFile(byte[] bArr, String str) {
            int i;
            int i2;
            Size pictureSize = CameraMTK.this.mParameters.getPictureSize();
            int orientation = Exif.getOrientation(bArr);
            if ((CameraMTK.this.mJpegRotation + orientation) % 180 == 0) {
                i = pictureSize.width;
                i2 = pictureSize.height;
            } else {
                i = pictureSize.height;
                i2 = pictureSize.width;
            }
            CameraMTK.this.mActivity.getImageSaver().addImage(bArr, str, System.currentTimeMillis(), null, CameraMTK.this.mJpegPictureCB.getLocation(), i, i2, null, orientation, false, false, true);
        }

        public void handleMessage(Message message) {
            Log.i("Camera", "Save handleMessage msg.what = " + message.what + ", msg.obj = " + message.obj);
            switch (message.what) {
                case 10004:
                    StereoDataGroup stereoDataGroup = (StereoDataGroup) message.obj;
                    this.mXmpJpegData = CameraMTK.this.writeStereoCaptureInfoToJpg(stereoDataGroup.getPictureName(), stereoDataGroup.getOriginalJpegData(), stereoDataGroup.getJpsData(), stereoDataGroup.getMaskAndConfigData(), stereoDataGroup.getClearImage(), stereoDataGroup.getDepthMap(), stereoDataGroup.getLdcData());
                    Log.i("Camera", "notifyMergeData mXmpJpegData: " + this.mXmpJpegData);
                    if (this.mXmpJpegData != null) {
                        saveFile(this.mXmpJpegData, stereoDataGroup.getPictureName());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class StereoPhotoDataCallback implements StereoDataCallback {
        private StereoPhotoDataCallback() {
        }

        public void onClearImageCapture(byte[] bArr) {
            Log.i("Camera", "onClearImageCapture clearImageData");
            if (bArr == null) {
                Log.i("Camera", " clearImage data is null");
                return;
            }
            Log.i("Camera", "onClearImageCapture clearImageData:" + bArr.length);
            CameraMTK.this.mClearImage = bArr;
            CameraMTK.this.notifyMergeData();
        }

        public void onDepthMapCapture(byte[] bArr) {
            Log.i("Camera", "onDepthMapCapture depthData");
            if (bArr == null) {
                Log.i("Camera", "depth data is null");
                return;
            }
            Log.i("Camera", "onDepthMapCapture depthData:" + bArr.length);
            CameraMTK.this.mDepthMap = bArr;
            CameraMTK.this.notifyMergeData();
        }

        public void onJpsCapture(byte[] bArr) {
            if (bArr == null) {
                Log.i("Camera", "JPS data is null");
                return;
            }
            Log.i("Camera", "onJpsCapture jpsData:" + bArr.length);
            CameraMTK.this.mJpsData = bArr;
            CameraMTK.this.notifyMergeData();
        }

        public void onLdcCapture(byte[] bArr) {
            Log.i("Camera", "onLdcCapture ldcData");
            if (bArr == null) {
                Log.i("Camera", " ldc data is null");
                return;
            }
            Log.i("Camera", "onLdcCapture ldcData:" + bArr.length);
            CameraMTK.this.mLdcData = bArr;
            CameraMTK.this.notifyMergeData();
        }

        public void onMaskCapture(byte[] bArr) {
            if (bArr == null) {
                Log.i("Camera", "Mask data is null");
                return;
            }
            Log.i("Camera", "onMaskCapture maskData:" + bArr.length);
            CameraMTK.this.mMaskAndConfigData = bArr;
            CameraMTK.this.setJsonBuffer(CameraMTK.this.mMaskAndConfigData);
            CameraMTK.this.notifyMergeData();
        }
    }

    class StereoPictureCallback extends JpegPictureCallback {
        public StereoPictureCallback(Location location) {
            super(location);
        }

        public Location getLocation() {
            return this.mLocation;
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            Log.d("Camera", "[mJpegPictureCallback] " + CameraMTK.this.mIsStereoCapture);
            if (!CameraMTK.this.mPaused) {
                if (bArr == null || !CameraMTK.this.mIsStereoCapture) {
                    super.onPictureTaken(bArr, camera);
                    if (bArr == null) {
                        return;
                    }
                }
                CameraMTK.this.mFocusManager.onShutter();
                CameraMTK.this.mOriginalJpegData = bArr;
                CameraMTK.this.notifyMergeData();
                Log.d("Camera", "[mJpegPictureCallback] end");
            }
        }
    }

    public CameraMTK() {
        this.mIsLongShotMode = false;
        this.mIsMTKFaceBeautyMode = false;
        this.mInUseFBParams = new FBParams();
        this.mCurrentFBParams = new FBParams();
        this.mFaceNo = FaceNo.NONE;
        this.mContinuousShotCallback = new C00931();
        this.mStereoPhotoDataCallback = new StereoPhotoDataCallback();
        this.mStereoCameraWarningCallback = new WarningCallback();
        this.mJpegPictureCB = new StereoPictureCallback(null);
        this.mIsStereoCapture = false;
        this.mCurrentNum = 0;
        this.mCameraCategory = new MtkCategory();
        this.mOperator = constructObject();
    }

    private void applyFBParams(Parameters parameters, FBParams fBParams) {
        if (parameters == null || fBParams == null) {
            Log.w("Camera", "applyFBParams: unexpected null " + (parameters == null ? "cameraParam" : "fbParam"));
            return;
        }
        sProxy.setSmoothLevel(parameters, "" + fBParams.smoothLevel);
        sProxy.setEnlargeEye(parameters, "" + fBParams.enlargeEye);
        sProxy.setSlimFace(parameters, "" + fBParams.slimFace);
        sProxy.setSkinColor(parameters, "" + fBParams.skinColor);
    }

    private Object constructObject() {
        return !Device.isSupportedStereo() ? null : null;
    }

    private boolean enableZSL() {
        return (Device.IS_HM3Y || Device.IS_HM3Z || Device.IS_H3C) ? true : Device.IS_B6;
    }

    private String flattenFaces(CameraHardwareFace[] cameraHardwareFaceArr) {
        int i = 0;
        if (cameraHardwareFaceArr != null) {
            i = cameraHardwareFaceArr.length;
        }
        if (i == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = cameraHardwareFaceArr[i2].rect.top + ((cameraHardwareFaceArr[i2].rect.bottom - cameraHardwareFaceArr[i2].rect.top) / 2);
            stringBuilder.append(cameraHardwareFaceArr[i2].rect.left + ((cameraHardwareFaceArr[i2].rect.right - cameraHardwareFaceArr[i2].rect.left) / 2)).append(":").append(i3);
            if (i2 != i - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private FaceNo getFaceNo(CameraHardwareFace[] cameraHardwareFaceArr) {
        switch (cameraHardwareFaceArr == null ? 0 : cameraHardwareFaceArr.length) {
            case 0:
                return FaceNo.NONE;
            case 1:
                return FaceNo.SINGLE;
            default:
                return FaceNo.MULTIPLE;
        }
    }

    private boolean isFlashWillOn(String str) {
        String flashMode = this.mParameters.getFlashMode();
        return ("auto".equals(flashMode) && "1".equals(str)) ? true : "on".equals(flashMode);
    }

    private static boolean isSupportContinuousShut(Parameters parameters) {
        List supportedCaptureMode = sProxy.getSupportedCaptureMode(parameters);
        return supportedCaptureMode != null && supportedCaptureMode.indexOf("continuousshot") >= 0;
    }

    private boolean isUseMediaTekFaceBeautify() {
        return !Device.IS_HM3Y ? Device.IS_HM3Z : true;
    }

    private void mtkUpdateCameraParametersPreference() {
        sProxy.setCameraMode(this.mParameters, 1);
        String string = getString(C0049R.string.pref_camera_jpegquality_value_low);
        Log.v("Camera", "thumbnailQuality = " + string);
        this.mParameters.setJpegThumbnailQuality(JpegEncodingQualityMappings.getQualityNumber(string));
        if ((EffectController.getInstance().hasEffect() ? Device.isEffectWatermarkFilted() : false) || this.mMutexModePicker.isUbiFocus() || !CameraSettings.isTimeWaterMarkOpen(this.mPreferences) || CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            sProxy.setTimeWatermark(this.mParameters, "off");
        } else {
            sProxy.setTimeWatermark(this.mParameters, "on");
        }
        String manualValue = getManualValue("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
        if (BaseModule.isSupported(manualValue, sProxy.getSupportedIsoValues(this.mParameters))) {
            Log.v("Camera", "ISO value = " + manualValue);
            sProxy.setISOValue(this.mParameters, manualValue);
        }
        String string2 = this.mPreferences.getString("pref_qc_camera_saturation_key", getString(C0049R.string.pref_camera_saturation_default));
        Log.v("Camera", "Saturation value = " + string2);
        sProxy.setSaturation(this.mParameters, string2);
        String string3 = this.mPreferences.getString("pref_qc_camera_contrast_key", getString(C0049R.string.pref_camera_contrast_default));
        Log.v("Camera", "Contrast value = " + string3);
        sProxy.setContrast(this.mParameters, string3);
        String string4 = this.mPreferences.getString("pref_qc_camera_sharpness_key", getString(C0049R.string.pref_camera_sharpness_default));
        Log.v("Camera", "Sharpness value = " + string4);
        sProxy.setSharpness(this.mParameters, string4);
        setPictureFlipIfNeed(this.mParameters);
        boolean enableZSL = enableZSL();
        Log.v("Camera", "ZSL value = " + (enableZSL ? "on" : "off"));
        if (enableZSL) {
            this.mIsZSLMode = true;
            sProxy.setZSLMode(this.mParameters, "on");
        } else {
            this.mIsZSLMode = false;
            sProxy.setZSLMode(this.mParameters, "off");
        }
        if (Device.isSupportedStereo()) {
            if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                sProxy.setVsDofMode(this.mParameters, true);
                this.mParameters.setPreviewFpsRange(5000, 24000);
                String string5 = this.mPreferences.getString("pref_camera_stereo_key", getString(C0049R.string.pref_camera_stereo_default));
                Log.v("Camera", "vfLevel value = " + string5);
                sProxy.setVsDofLevel(this.mParameters, string5);
            } else {
                this.mParameters.setPreviewFpsRange(5000, 30000);
                sProxy.setVsDofMode(this.mParameters, false);
            }
        }
        if (this.mMultiSnapStatus && !this.mIsLongShotMode && isSupportContinuousShut(this.mParameters)) {
            this.mIsLongShotMode = true;
            setTimeWatermarkIfNeed();
        } else if (this.mIsLongShotMode) {
            this.mIsLongShotMode = false;
            applyMultiShutParameters(false);
        }
        Log.v("Camera", "Long Shot mode value = " + isLongShotMode());
        if (Device.isSupportedSkinBeautify()) {
            String faceBeautifyValue = CameraSettings.getFaceBeautifyValue();
            sProxy.setStillBeautify(this.mParameters, faceBeautifyValue);
            Log.v("Camera", "FB value =" + sProxy.getStillBeautify(this.mParameters));
            if (isUseMediaTekFaceBeautify()) {
                setMediatekBeautify(faceBeautifyValue);
            } else {
                setBeautyParams();
            }
            sProxy.setFaceBeauty(this.mParameters, "false");
            sProxy.set3dnrMode(this.mParameters, "on");
        }
        if (Device.isSupportedIntelligentBeautify()) {
            String string6 = this.mPreferences.getString("pref_camera_show_gender_age_key", getString(C0049R.string.pref_camera_show_gender_age_default));
            getUIController().getFaceView().setShowGenderAndAge(string6);
            Log.v("Camera", "SetShowGenderAndAge =" + string6);
        }
        sProxy.setHDR(this.mParameters, "false");
        sProxy.setNightShot(this.mParameters, "false");
        sProxy.setNightAntiMotion(this.mParameters, "false");
        if (!this.mMutexModePicker.isNormal()) {
            if (this.mMutexModePicker.isHandNight()) {
                if (isSceneMotion()) {
                    sProxy.setNightAntiMotion(this.mParameters, "true");
                    Log.v("Camera", "AntiMotion = true");
                } else {
                    sProxy.setNightShot(this.mParameters, "true");
                    Log.v("Camera", "Hand Nigh = true");
                }
            } else if (this.mMutexModePicker.isMorphoHdr()) {
                sProxy.setHDR(this.mParameters, "true");
                Log.v("Camera", "Morpho HDR = true");
            }
        }
        if (isBackCamera() && Device.isSupportedASD()) {
            boolean z = (getUIController().getSettingPage().isItemSelected() || this.mIsLongShotMode) ? false : !CameraSettings.isSwitchOn("pref_camera_stereo_mode_key");
            Log.v("Camera", "ASD Enable = " + z);
            setMetaCallback(z);
        }
    }

    private void notifyMergeData() {
        Log.i("Camera", "notifyMergeData mCurrentNum = " + this.mCurrentNum);
        this.mCurrentNum++;
        if (this.mCurrentNum == 6) {
            Log.i("Camera", "notifyMergeData Vs Dof " + this.mIsStereoCapture);
            setupPreview();
            if (this.mIsStereoCapture) {
                this.mSaveHandler.obtainMessage(10004, new StereoDataGroup(Util.createJpegName(System.currentTimeMillis()) + "_STEREO", this.mOriginalJpegData, this.mJpsData, this.mMaskAndConfigData, this.mDepthMap, this.mClearImage, this.mLdcData)).sendToTarget();
            }
            this.mCurrentNum = 0;
        }
    }

    private void setFacePoints(Parameters parameters) {
        String flattenFaces = flattenFaces(getUIController().getFaceView().getFaces());
        if (flattenFaces != null) {
            sProxy.setFacePosition(parameters, flattenFaces);
        }
    }

    private void setJsonBuffer(byte[] bArr) {
    }

    private void setMediatekBeautify(String str) {
        if (this.mIsMTKFaceBeautyMode && getString(C0049R.string.pref_face_beauty_close).equals(str)) {
            this.mIsMTKFaceBeautyMode = false;
            sProxy.setCaptureMode(this.mParameters, "normal");
            sProxy.setFaceBeauty(this.mParameters, "false");
            sProxy.set3dnrMode(this.mParameters, "on");
            this.mHandler.obtainMessage(34, 0, 0, this).sendToTarget();
        } else if (!this.mIsMTKFaceBeautyMode && !getString(C0049R.string.pref_face_beauty_close).equals(str)) {
            this.mIsMTKFaceBeautyMode = true;
            stopFaceDetection(true);
            sProxy.setCaptureMode(this.mParameters, "face_beauty");
            sProxy.setFaceBeauty(this.mParameters, "true");
            sProxy.set3dnrMode(this.mParameters, "off");
            CameraHardwareFace cameraHardwareFace = null;
            if (this.mFaceNo == FaceNo.SINGLE) {
                sProxy.setExtremeBeauty(this.mParameters, "true");
                CameraHardwareFace[] faces = getUIController().getFaceView().getFaces();
                if (faces != null && faces.length >= 1) {
                    cameraHardwareFace = faces[0];
                }
            } else {
                sProxy.setExtremeBeauty(this.mParameters, "false");
            }
            updateFBParams(this.mInUseFBParams, str, cameraHardwareFace);
            applyFBParams(this.mParameters, this.mInUseFBParams);
            this.mHandler.obtainMessage(34, 1, 0, this).sendToTarget();
        }
    }

    private void setPictureFlipIfNeed(Parameters parameters) {
        if (isFrontMirror()) {
            sProxy.setPictureFlip(parameters, "1");
        } else {
            sProxy.setPictureFlip(parameters, "0");
        }
        Log.d("Camera", "Picture flip value = " + sProxy.getPictureFlip(parameters));
    }

    private void updateFBParams(FBParams fBParams, String str, CameraHardwareFace cameraHardwareFace) {
        if (getString(C0049R.string.pref_face_beauty_advanced).equals(str)) {
            MtkFBParamsUtil.getAdvancedValue(fBParams);
        } else {
            FBLevel fBLevel;
            if (getString(C0049R.string.pref_face_beauty_low).equals(str)) {
                fBLevel = FBLevel.LOW;
            } else if (getString(C0049R.string.pref_face_beauty_medium).equals(str)) {
                fBLevel = FBLevel.MEDIUM;
            } else if (getString(C0049R.string.pref_face_beauty_high).equals(str)) {
                fBLevel = FBLevel.HIGH;
            } else {
                Log.w("Camera", "updateFBParams: unexpected fbMode " + str);
                return;
            }
            MtkFBParamsUtil.getIntelligentValue(fBParams, fBLevel, cameraHardwareFace);
        }
    }

    private byte[] writeStereoCaptureInfoToJpg(String str, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6) {
        return null;
    }

    protected void applyMultiShutParameters(boolean z) {
        sProxy.setBurstShotNum(this.mParameters, z ? BURST_SHOOTING_COUNT : 1);
        sProxy.setCaptureMode(this.mParameters, z ? "continuousshot" : "normal");
    }

    protected void cancelContinuousShot() {
        this.mCameraDevice.cancelPicture();
    }

    protected void closeCamera() {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setContinuousShotCallback(null);
            this.mCameraDevice.setStereoWarningCallback(null);
        }
        super.closeCamera();
    }

    protected PictureSize getBestPictureSize() {
        List supportedStereoPictureSizes = (Device.isSupportedStereo() && CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) ? sProxy.getSupportedStereoPictureSizes(this.mCameraDevice, this.mParameters) : this.mParameters.getSupportedPictureSizes();
        PictureSizeManager.initialize(getActivity(), supportedStereoPictureSizes, getMaxPictureSize());
        return PictureSizeManager.getBestPictureSize();
    }

    protected void handleMultiSnapDone() {
        if (!this.mPaused) {
            if (this.mCameraDevice != null) {
                this.mCameraDevice.setContinuousShotCallback(null);
            }
            super.handleMultiSnapDone();
        }
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            if (this.mSaveHandler == null) {
                HandlerThread handlerThread = new HandlerThread("Stereo Save Handler Thread");
                handlerThread.start();
                this.mSaveHandler = new SaveHandler(handlerThread.getLooper());
            }
            this.mIsStereoCapture = true;
            return;
        }
        this.mIsStereoCapture = false;
    }

    protected boolean isFaceBeautyMode() {
        return this.mIsMTKFaceBeautyMode;
    }

    protected boolean isLongShotMode() {
        return this.mIsLongShotMode;
    }

    protected boolean isSupportSceneMode() {
        return true;
    }

    protected boolean isZeroShotMode() {
        return this.mIsZSLMode;
    }

    protected boolean needAutoFocusBeforeCapture() {
        return isFlashWillOn(this.mCameraDevice.getParameters().get("flash-on"));
    }

    protected boolean needSetupPreview(boolean z) {
        String str = this.mCameraDevice.getParameters().get("preview-stopped");
        return str != null ? "1".equals(str) : true;
    }

    public void onCameraStartPreview() {
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            this.mStereoCameraWarningCallback.setActivity(this.mActivity);
            this.mCameraDevice.setStereoWarningCallback(this.mStereoCameraWarningCallback);
            if (CameraSettings.isDualCameraHintShown(this.mPreferences)) {
                this.mHandler.sendEmptyMessage(40);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mSaveHandler != null) {
            this.mSaveHandler.getLooper().quit();
            this.mSaveHandler = null;
        }
    }

    public void onFaceDetection(Face[] faceArr, Camera camera) {
        CameraHardwareFace cameraHardwareFace = null;
        super.onFaceDetection(faceArr, camera);
        if (Device.isSupportedSkinBeautify() && isUseMediaTekFaceBeautify()) {
            FaceView faceView = getUIController().getFaceView();
            if (faceView != null && faceView.faceExists() && faceView.isFaceStable()) {
                CameraHardwareFace[] convertCameraHardwareFace = CameraHardwareFace.convertCameraHardwareFace(faceArr);
                FaceNo faceNo = getFaceNo(convertCameraHardwareFace);
                if (faceNo == FaceNo.SINGLE || faceNo != this.mFaceNo) {
                    if (faceNo == FaceNo.SINGLE) {
                        cameraHardwareFace = convertCameraHardwareFace[0];
                    }
                    if (cameraHardwareFace == null || (((double) cameraHardwareFace.gender) >= 0.001d && (cameraHardwareFace.gender <= 0.4f || cameraHardwareFace.gender >= 0.6f))) {
                        String faceBeautifyValue = CameraSettings.getFaceBeautifyValue();
                        if (!getString(C0049R.string.pref_face_beauty_close).equals(faceBeautifyValue)) {
                            updateFBParams(this.mCurrentFBParams, faceBeautifyValue, cameraHardwareFace);
                        }
                        if (!(faceNo == this.mFaceNo && this.mCurrentFBParams.equals(this.mInUseFBParams))) {
                            if (faceNo == FaceNo.SINGLE) {
                                sProxy.setExtremeBeauty(this.mParameters, "true");
                            } else {
                                sProxy.setExtremeBeauty(this.mParameters, "false");
                            }
                            applyFBParams(this.mParameters, this.mCurrentFBParams);
                            this.mCameraDevice.setParameters(this.mParameters);
                            this.mFaceNo = faceNo;
                            this.mInUseFBParams.copy(this.mCurrentFBParams);
                        }
                    }
                }
            }
        }
    }

    public void onSettingValueChanged(String str) {
        if (this.mCameraDevice != null) {
            if ("pref_qc_camera_iso_key".equals(str)) {
                String string = this.mPreferences.getString("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
                if (BaseModule.isSupported(string, sProxy.getSupportedIsoValues(this.mParameters))) {
                    Log.v("Camera", "ISO value = " + string);
                    sProxy.setISOValue(this.mParameters, string);
                }
                this.mCameraDevice.setParametersAsync(this.mParameters);
            } else if ("pref_camera_stereo_key".equals(str)) {
                String string2 = this.mPreferences.getString("pref_camera_stereo_key", getString(C0049R.string.pref_camera_stereo_default));
                Log.v("Camera", "Setting changed, vfLevel value = " + string2);
                sProxy.setVsDofLevel(this.mParameters, string2);
                this.mCameraDevice.setParametersAsync(this.mParameters);
                updateStatusBar("pref_camera_stereo_key");
            } else {
                super.onSettingValueChanged(str);
            }
        }
    }

    protected void prepareCapture() {
        setPictureFlipIfNeed(this.mParameters);
        setTimeWatermarkIfNeed();
        if (isFaceBeautyMode()) {
            setFacePoints(this.mParameters);
        }
    }

    protected void prepareMultiCapture() {
        applyMultiShutParameters(true);
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setContinuousShotCallback(this.mContinuousShotCallback);
        }
    }

    protected void prepareOpenCamera() {
        closeCamera();
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") && !this.mIsImageCaptureIntent) {
            sProxy.enableStereoMode();
        }
    }

    protected void resetFaceBeautyMode() {
        this.mIsMTKFaceBeautyMode = false;
    }

    protected void setAutoExposure(Parameters parameters, String str) {
        List supportedAutoexposure = sProxy.getSupportedAutoexposure(parameters);
        if (supportedAutoexposure != null && supportedAutoexposure.contains(str)) {
            sProxy.setAutoExposure(parameters, str);
        }
    }

    protected void updateCameraParametersPreference() {
        super.updateCameraParametersPreference();
        mtkUpdateCameraParametersPreference();
    }
}
