package com.android.camera.effect;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.CameraAppImpl;
import com.android.camera.Device;
import com.android.camera.aosp_porting.FeatureParser;
import com.android.camera.effect.renders.BigFaceEffectRender;
import com.android.camera.effect.renders.BlackWhiteEffectRender;
import com.android.camera.effect.renders.FishEyeEffectRender;
import com.android.camera.effect.renders.FocusPeakingRender;
import com.android.camera.effect.renders.Gaussian2DEffectRender;
import com.android.camera.effect.renders.GaussianMaskEffectRender;
import com.android.camera.effect.renders.GradienterEffectRender;
import com.android.camera.effect.renders.GradienterSnapshotEffectRender;
import com.android.camera.effect.renders.GrayEffectRender;
import com.android.camera.effect.renders.InstagramClarendonEffectRender;
import com.android.camera.effect.renders.InstagramCremaEffectRender;
import com.android.camera.effect.renders.InstagramHudsonEffectRender;
import com.android.camera.effect.renders.InstagramRiseEffectRender;
import com.android.camera.effect.renders.LightTunnelEffectRender;
import com.android.camera.effect.renders.LongFaceEffectRender;
import com.android.camera.effect.renders.MirrorEffectRender;
import com.android.camera.effect.renders.MosaicEffectRender;
import com.android.camera.effect.renders.PipeRenderPair;
import com.android.camera.effect.renders.RenderGroup;
import com.android.camera.effect.renders.SketchEffectRender;
import com.android.camera.effect.renders.SmallFaceEffectRender;
import com.android.camera.effect.renders.TiltShiftMaskEffectRender;
import com.android.camera.effect.renders.VividEffectRender;
import com.android.camera.effect.renders.VscoA4EffectRender;
import com.android.camera.effect.renders.VscoF2EffectRender;
import com.android.camera.effect.renders.XBlurEffectRender;
import com.android.camera.effect.renders.XGaussianEffectRender;
import com.android.camera.effect.renders.XTiltShiftEffectRender;
import com.android.camera.effect.renders.YBlurEffectRender;
import com.android.camera.effect.renders.YGaussianEffectRender;
import com.android.camera.effect.renders.YTiltShiftEffectRender;
import com.android.camera.ui.V6ModulePicker;
import com.android.gallery3d.ui.GLCanvas;
import java.util.ArrayList;

public class EffectController {
    public static final int COLUMN_COUNT;
    public static final int SHOW_COUNT = (Device.isPad() ? 7 : 12);
    public static int sBackgroundBlurIndex = 16;
    public static int sDividerIndex = 8;
    public static int sFishEyeIndex = 12;
    public static int sGaussianIndex = 19;
    public static int sGradienterIndex = 17;
    private static EffectController sInstance;
    public static int sPeakingMFIndex = 20;
    public static int sTiltShiftIndex = 18;
    private boolean mBlur = false;
    private int mBlurStep = -1;
    private float mDeviceRotation;
    public volatile int mDisplayEndIndex = SHOW_COUNT;
    public volatile boolean mDisplayShow = false;
    public volatile int mDisplayStartIndex = 0;
    private boolean mDrawPeaking;
    private int mEffectCount = 16;
    private ArrayList<String> mEffectEntries;
    private ArrayList<String> mEffectEntryValues;
    private int mEffectGroupSize = 21;
    private ArrayList<Integer> mEffectImageIds;
    private int mEffectIndex = 0;
    private ArrayList<String> mEffectKeys;
    private EffectRectAttribute mEffectRectAttribute = new EffectRectAttribute();
    public volatile boolean mFillAnimationCache = false;
    private boolean mIsDrawMainFrame = true;
    private ArrayList<Integer> mNeedRectSet;
    private ArrayList<Integer> mNeedScaleDownSet;
    private int mOrientation;
    private int mOverrideEffectIndex = -1;
    public SurfacePosition mSurfacePosition = new SurfacePosition();
    private float mTiltShiftMaskAlpha;

    public static class EffectRectAttribute {
        public int mInvertFlag;
        public PointF mPoint1;
        public PointF mPoint2;
        public float mRangeWidth;
        public RectF mRectF;

        private EffectRectAttribute() {
            this.mRectF = new RectF();
            this.mPoint1 = new PointF();
            this.mPoint2 = new PointF();
        }

        private EffectRectAttribute(EffectRectAttribute effectRectAttribute) {
            this.mRectF = new RectF();
            this.mPoint1 = new PointF();
            this.mPoint2 = new PointF();
            this.mRectF.set(effectRectAttribute.mRectF);
            this.mPoint1.set(effectRectAttribute.mPoint1);
            this.mPoint2.set(effectRectAttribute.mPoint2);
            this.mInvertFlag = effectRectAttribute.mInvertFlag;
            this.mRangeWidth = effectRectAttribute.mRangeWidth;
        }

        public String toString() {
            return "mRectF=" + this.mRectF + " mPoint1=" + this.mPoint1 + " mPoint2=" + this.mPoint2 + " mInvertFlag=" + this.mInvertFlag + " mRangeWidth=" + this.mRangeWidth;
        }
    }

    public static class SurfacePosition {
        public int mHonSpace;
        public boolean mIsRtl;
        public int mStartX;
        public int mStartY;
        public int mVerSpace;
        public int mWidth;
    }

    static {
        int i = 7;
        if (!Device.isPad()) {
            i = 3;
        }
        COLUMN_COUNT = i;
    }

    private EffectController() {
        initialize();
    }

    private void addEntryItem(int i, int i2) {
        this.mEffectEntries.add(getString(i));
        this.mEffectEntryValues.add(String.valueOf(i2));
    }

    public static synchronized EffectController getInstance() {
        EffectController effectController;
        synchronized (EffectController.class) {
            if (sInstance == null) {
                sInstance = new EffectController();
            }
            effectController = sInstance;
        }
        return effectController;
    }

    private String getString(int i) {
        return CameraAppImpl.getAndroidContext().getString(i);
    }

    private void initEffectWeight() {
    }

    public static synchronized void releaseInstance() {
        synchronized (EffectController.class) {
            sInstance = null;
        }
    }

    public void clearEffectAttribute() {
        this.mEffectRectAttribute.mRectF.set(0.0f, 0.0f, 0.0f, 0.0f);
        this.mEffectRectAttribute.mPoint1.set(0.0f, 0.0f);
        this.mEffectRectAttribute.mPoint2.set(0.0f, 0.0f);
        this.mEffectRectAttribute.mRangeWidth = 0.0f;
    }

    public EffectRectAttribute copyEffectRectAttribute() {
        return new EffectRectAttribute(this.mEffectRectAttribute);
    }

    public String getAnalyticsKey() {
        String str;
        synchronized (this) {
            str = (this.mEffectKeys == null || this.mEffectIndex >= this.mEffectKeys.size()) ? "" : (String) this.mEffectKeys.get(this.mEffectIndex);
        }
        return str;
    }

    public int getBlurAnimationValue() {
        if (this.mBlurStep >= 0 && this.mBlurStep <= 8) {
            this.mBlurStep = (this.mBlur ? 1 : -1) + this.mBlurStep;
            if (8 <= this.mBlurStep && this.mBlur) {
                this.mOverrideEffectIndex = sBackgroundBlurIndex;
            }
            if (this.mBlurStep >= 0 && this.mBlurStep <= 8) {
                return (this.mBlurStep * 212) / 8;
            }
        }
        return -1;
    }

    public float getDeviceRotation() {
        return this.mDeviceRotation;
    }

    public int getDisplayEndIndex() {
        return this.mDisplayEndIndex;
    }

    public int getDisplayStartIndex() {
        return this.mDisplayStartIndex;
    }

    public int getEffect(boolean z) {
        synchronized (this) {
            int i;
            if (z) {
                if (this.mOverrideEffectIndex != -1) {
                    i = this.mOverrideEffectIndex;
                    return i;
                }
            }
            i = this.mEffectIndex;
            return i;
        }
    }

    public EffectRectAttribute getEffectAttribute() {
        return this.mEffectRectAttribute;
    }

    public int getEffectCount() {
        return this.mEffectCount;
    }

    public RenderGroup getEffectGroup(GLCanvas gLCanvas, RenderGroup renderGroup, boolean z, boolean z2, int i) {
        if (!Device.isSupportedShaderEffect()) {
            return null;
        }
        Object obj = gLCanvas == null ? 1 : null;
        Object obj2 = null;
        if (gLCanvas == null) {
            this.mEffectEntries = new ArrayList();
            this.mEffectEntryValues = new ArrayList();
            this.mEffectImageIds = new ArrayList();
            this.mEffectKeys = new ArrayList();
            this.mNeedRectSet = new ArrayList();
            this.mNeedScaleDownSet = new ArrayList();
            obj = 1;
        } else if (renderGroup == null) {
            renderGroup = new RenderGroup(gLCanvas, this.mEffectGroupSize);
            if (!z && i < 0) {
                return renderGroup;
            }
        } else if (!renderGroup.isNeedInit(i)) {
            return renderGroup;
        }
        int i2 = 0;
        if (obj != null) {
            try {
                addEntryItem(C0049R.string.pref_camera_coloreffect_entry_none, 0);
                this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_none));
                this.mEffectKeys.add("");
            } catch (Throwable e) {
                if (i < 0) {
                    Log.e("EffectController", "IllegalArgumentException when create render.", e);
                } else {
                    throw e;
                }
            }
        } else if (renderGroup.getRender(0) == null) {
            if (!(z || i == 0)) {
                if (i < 0) {
                    if (null != null) {
                    }
                }
            }
            renderGroup.setRender(null, 0);
        }
        i2 = 0 + 1;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_instagram_rise, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_instagram_rise));
            this.mEffectKeys.add("effect_instagram_rise_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (null != null) {
                    }
                }
            }
            renderGroup.setRender(InstagramRiseEffectRender.create(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_instagram_clarendon, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_instagram_clarendon));
            this.mEffectKeys.add("effect_instagram_clarendon_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(InstagramClarendonEffectRender.create(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_instagram_crema, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_instagram_crema));
            this.mEffectKeys.add("effect_instagram_crema_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(InstagramCremaEffectRender.create(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_instagram_hudson, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_instagram_hudson));
            this.mEffectKeys.add("effect_instagram_hudson_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(InstagramHudsonEffectRender.create(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_vivid, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_vivid));
            this.mEffectKeys.add("effect_vivid_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new VividEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_vsco_a4, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_vsco_a4));
            this.mEffectKeys.add("effect_vsco_a4_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(VscoA4EffectRender.create(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_vsco_f2, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_vsco_f2));
            this.mEffectKeys.add("effect_vsco_f2_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new VscoF2EffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            this.mNeedScaleDownSet.add(Integer.valueOf(i2));
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_mono, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_mono));
            this.mEffectKeys.add("effect_gray_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new GrayEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_blackwhite, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_blackwhite));
            this.mEffectKeys.add("effect_blackwhite_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new BlackWhiteEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_sketch, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_sketch));
            this.mEffectKeys.add("effect_sketch_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            if (z || i == i2 || renderGroup.isPartComplete(2)) {
                renderGroup.setRender(new PipeRenderPair(gLCanvas, renderGroup.getPartRender(0) != null ? renderGroup.getPartRender(0) : new Gaussian2DEffectRender(gLCanvas, i2), renderGroup.getPartRender(1) != null ? renderGroup.getPartRender(1) : new SketchEffectRender(gLCanvas, i2), false), i2);
                renderGroup.clearPartRenders();
            } else if (renderGroup.getPartRender(0) == null) {
                renderGroup.addPartRender(new Gaussian2DEffectRender(gLCanvas, i2));
            } else if (renderGroup.getPartRender(1) == null) {
                renderGroup.addPartRender(new SketchEffectRender(gLCanvas, i2));
            }
            obj2 = 1;
        }
        if (obj != null) {
            sDividerIndex = i2;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_big_face, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_big_face));
            this.mEffectKeys.add("effect_big_face_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new BigFaceEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_small_face, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_small_face));
            this.mEffectKeys.add("effect_small_face_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new SmallFaceEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_long_face, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_long_face));
            this.mEffectKeys.add("effect_long_face_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new LongFaceEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            sFishEyeIndex = i2;
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_fisheye, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_fisheye));
            this.mEffectKeys.add("effect_fisheye_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new FishEyeEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            this.mNeedRectSet.add(Integer.valueOf(i2));
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_mosaic, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_mosaic));
            this.mEffectKeys.add("effect_mosaic_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new MosaicEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_mirror, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_mirror));
            this.mEffectKeys.add("effect_mirror_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new MirrorEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            addEntryItem(C0049R.string.pref_camera_coloreffect_entry_light_tunnel, i2);
            this.mEffectImageIds.add(Integer.valueOf(C0049R.drawable.camera_effect_image_light_tunnel));
            this.mEffectKeys.add("effect_light_tunnel_picture_taken_key");
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new LightTunnelEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        if (obj != null) {
            this.mEffectCount = 18;
        }
        i2++;
        if (obj != null) {
            sBackgroundBlurIndex = i2;
        } else if (renderGroup.getRender(i2) == null) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            if (z || i == i2 || renderGroup.isPartComplete(2)) {
                Object obj3 = renderGroup.getPartRender(0) != null ? renderGroup.getPartRender(0).getId() == i2 ? 1 : null : null;
                Object obj4 = renderGroup.getPartRender(1) != null ? renderGroup.getPartRender(1).getId() == i2 ? 1 : null : null;
                renderGroup.setRender(new PipeRenderPair(gLCanvas, obj3 != null ? renderGroup.getPartRender(0) : new XBlurEffectRender(gLCanvas, i2), obj4 != null ? renderGroup.getPartRender(1) : new YBlurEffectRender(gLCanvas, i2), false), i2);
                if (!(obj3 == null && obj4 == null)) {
                    renderGroup.clearPartRenders();
                }
            } else if (renderGroup.getPartRender(0) == null) {
                renderGroup.addPartRender(new XBlurEffectRender(gLCanvas, i2));
            } else if (renderGroup.getPartRender(1) == null) {
                renderGroup.addPartRender(new YBlurEffectRender(gLCanvas, i2));
            }
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            sGradienterIndex = i2;
        } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(z2 ? new GradienterSnapshotEffectRender(gLCanvas, i2) : new GradienterEffectRender(gLCanvas, i2), i2);
            obj2 = 1;
        }
        i2++;
        if (obj != null) {
            sTiltShiftIndex = i2;
            this.mNeedRectSet.add(Integer.valueOf(i2));
        } else if (renderGroup.getRender(i2) == null && Device.isSupportedTiltShift()) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            if (z || i == i2 || renderGroup.isPartComplete(3)) {
                renderGroup.setRender(new PipeRenderPair(gLCanvas, new PipeRenderPair(gLCanvas, renderGroup.getPartRender(0) != null ? renderGroup.getPartRender(0) : new XTiltShiftEffectRender(gLCanvas, i2), renderGroup.getPartRender(1) != null ? renderGroup.getPartRender(1) : new YTiltShiftEffectRender(gLCanvas, i2), false), renderGroup.getPartRender(2) != null ? renderGroup.getPartRender(2) : new TiltShiftMaskEffectRender(gLCanvas, i2), false), i2);
                renderGroup.clearPartRenders();
            } else if (renderGroup.getPartRender(0) == null) {
                renderGroup.addPartRender(new XTiltShiftEffectRender(gLCanvas, i2));
            } else if (renderGroup.getPartRender(1) == null) {
                renderGroup.addPartRender(new YTiltShiftEffectRender(gLCanvas, i2));
            } else if (renderGroup.getPartRender(2) == null) {
                renderGroup.addPartRender(new TiltShiftMaskEffectRender(gLCanvas, i2));
            }
            obj2 = 1;
        }
        if (!FeatureParser.getBoolean("is_camera_replace_higher_cost_effect", false)) {
            i2++;
            if (obj != null) {
                sGaussianIndex = i2;
                this.mNeedRectSet.add(Integer.valueOf(i2));
            } else if (renderGroup.getRender(i2) == null && (V6ModulePicker.isCameraModule() || z2)) {
                if (!(z || i == i2)) {
                    if (i < 0) {
                        if (obj2 != null) {
                        }
                    }
                }
                if (z || i == i2 || renderGroup.isPartComplete(3)) {
                    renderGroup.setRender(new PipeRenderPair(gLCanvas, new PipeRenderPair(gLCanvas, renderGroup.getPartRender(0) != null ? renderGroup.getPartRender(0) : new XGaussianEffectRender(gLCanvas, i2), renderGroup.getPartRender(1) != null ? renderGroup.getPartRender(1) : new YGaussianEffectRender(gLCanvas, i2), false), renderGroup.getPartRender(2) != null ? renderGroup.getPartRender(2) : new GaussianMaskEffectRender(gLCanvas, i2), false), i2);
                    renderGroup.clearPartRenders();
                } else if (renderGroup.getPartRender(0) == null) {
                    renderGroup.addPartRender(new XGaussianEffectRender(gLCanvas, i2));
                } else if (renderGroup.getPartRender(1) == null) {
                    renderGroup.addPartRender(new YGaussianEffectRender(gLCanvas, i2));
                } else if (renderGroup.getPartRender(2) == null) {
                    renderGroup.addPartRender(new GaussianMaskEffectRender(gLCanvas, i2));
                }
                obj2 = 1;
            }
        }
        i2++;
        if (obj != null) {
            sPeakingMFIndex = i2;
        } else if (renderGroup.getRender(i2) == null && ((V6ModulePicker.isCameraModule() || z2) && Device.isSupportedPeakingMF() && !z2)) {
            if (!(z || i == i2)) {
                if (i < 0) {
                    if (obj2 != null) {
                    }
                }
            }
            renderGroup.setRender(new FocusPeakingRender(gLCanvas, i2), i2);
        }
        if (obj != null) {
            this.mEffectGroupSize = i2 + 1;
        }
        return renderGroup;
    }

    public int getEffectIndexByEntryName(String str) {
        for (int i = 0; i < this.mEffectEntries.size(); i++) {
            if (((String) this.mEffectEntries.get(i)).equals(str)) {
                return i;
            }
        }
        return 0;
    }

    public RectF getEffectRectF() {
        return new RectF(this.mEffectRectAttribute.mRectF);
    }

    public String[] getEntries() {
        String[] strArr = new String[this.mEffectEntries.size()];
        this.mEffectEntries.toArray(strArr);
        return strArr;
    }

    public String[] getEntryValues() {
        String[] strArr = new String[this.mEffectEntryValues.size()];
        this.mEffectEntryValues.toArray(strArr);
        return strArr;
    }

    public int[] getImageIds() {
        int[] iArr = new int[this.mEffectImageIds.size()];
        for (int i = 0; i < this.mEffectImageIds.size(); i++) {
            iArr[i] = ((Integer) this.mEffectImageIds.get(i)).intValue();
        }
        return iArr;
    }

    public int getInvertFlag() {
        return this.mEffectRectAttribute.mInvertFlag;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public SurfacePosition getSurfacePosition() {
        return this.mSurfacePosition;
    }

    public float getTiltShiftMaskAlpha() {
        return this.mTiltShiftMaskAlpha;
    }

    public boolean hasEffect() {
        boolean z = false;
        synchronized (this) {
            if (Device.isSupportedShaderEffect() && this.mEffectIndex != 0) {
                z = true;
            }
        }
        return z;
    }

    public void initialize() {
        initEffectWeight();
        getEffectGroup(null, null, false, false, -1);
    }

    public boolean isBackGroundBlur() {
        return getEffect(true) == sBackgroundBlurIndex;
    }

    public boolean isDisplayShow() {
        return this.mDisplayShow;
    }

    public boolean isEffectPageSelected() {
        boolean z = false;
        synchronized (this) {
            if (this.mEffectIndex != 0 && this.mEffectIndex < this.mEffectCount) {
                z = true;
            }
        }
        return z;
    }

    public boolean isFishEye() {
        boolean z;
        synchronized (this) {
            z = this.mEffectIndex == sFishEyeIndex;
        }
        return z;
    }

    public boolean isMainFrameDisplay() {
        return this.mIsDrawMainFrame;
    }

    public boolean isNeedDrawPeaking() {
        return this.mDrawPeaking;
    }

    public boolean isNeedRect(int i) {
        if (Device.isSupportedShaderEffect()) {
            for (Integer intValue : this.mNeedRectSet) {
                if (intValue.intValue() == i) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean needDownScale(int i) {
        if (Device.isSupportedShaderEffect()) {
            for (Integer intValue : this.mNeedScaleDownSet) {
                if (intValue.intValue() == i) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setBlurEffect(boolean z) {
        int i = 0;
        if (z != this.mBlur) {
            if (!z) {
                this.mOverrideEffectIndex = -1;
            }
            if (this.mBlurStep < 0 || 8 < this.mBlurStep) {
                if (!z) {
                    i = 8;
                }
                this.mBlurStep = i;
            }
            this.mIsDrawMainFrame = true;
        }
        this.mBlur = z;
    }

    public void setDeviceRotation(boolean z, float f) {
        if (z) {
            f = -1.0f;
        }
        this.mDeviceRotation = f;
    }

    public void setDrawPeaking(boolean z) {
        this.mDrawPeaking = z;
    }

    public void setEffect(int i) {
        synchronized (this) {
            this.mEffectIndex = i;
        }
    }

    public void setEffectAttribute(RectF rectF, PointF pointF, PointF pointF2, float f) {
        this.mEffectRectAttribute.mRectF.set(rectF);
        this.mEffectRectAttribute.mPoint1.set(pointF);
        this.mEffectRectAttribute.mPoint2.set(pointF2);
        this.mEffectRectAttribute.mRangeWidth = f;
    }

    public void setInvertFlag(int i) {
        this.mEffectRectAttribute.mInvertFlag = i;
    }

    public void setOrientation(int i) {
        this.mOrientation = i;
    }

    public void setTiltShiftMaskAlpha(float f) {
        this.mTiltShiftMaskAlpha = f;
    }
}
