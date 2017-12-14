package com.android.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Country;
import android.location.CountryDetector;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.aosp_porting.FeatureParser;
import com.android.camera.aosp_porting.ReflectUtil;
import com.android.camera.effect.EffectController;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.steganography.SteganographyUtils;
import com.android.camera.storage.Storage;
import com.android.camera.ui.V6ModulePicker;
import dalvik.system.VMRuntime;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Util {
    private static HashSet<String> ANTIBANDING_60_COUNTRY = new HashSet(Arrays.asList(new String[]{"TW", "KR", "SA", "US", "CA", "BR", "CO", "MX", "PH"}));
    private static final File INTERNAL_STORAGE_DIRECTORY = new File("/data/sdcard");
    private static String mCountryIso = null;
    private static int mLockedOrientation = -1;
    private static boolean sClearMemoryLimit;
    private static ImageFileNamer sImageFileNamer;
    public static boolean sIsDumpLog;
    private static float sPixelDensity = 1.0f;
    private static HashMap<String, Typeface> sTypefaces = new HashMap();
    public static int sWindowHeight = 1080;
    public static int sWindowWidth = 720;

    private static class ImageFileNamer {
        private SimpleDateFormat mFormat;
        private long mLastDate;
        private int mSameSecondCount;

        public ImageFileNamer(String str) {
            this.mFormat = new SimpleDateFormat(str);
        }

        public String generateName(long j) {
            String format = this.mFormat.format(new Date(j));
            if (j / 1000 == this.mLastDate / 1000) {
                this.mSameSecondCount++;
                return format + "_" + this.mSameSecondCount;
            }
            this.mLastDate = j;
            this.mSameSecondCount = 0;
            return format;
        }
    }

    private Util() {
    }

    public static void Assert(boolean z) {
        if (!z) {
            throw new AssertionError();
        }
    }

    private static String addProperties(String str) {
        String str2 = "";
        if (SystemProperties.get(str) == null) {
            return str2;
        }
        return ("\t " + SystemProperties.get(str)) + "\n";
    }

    public static <T> int binarySearchRightMost(List<? extends Comparable<? super T>> list, T t) {
        int i = 0;
        int size = list.size() - 1;
        while (i <= size) {
            int i2 = (i + size) / 2;
            if (((Comparable) list.get(i2)).compareTo(t) >= 0) {
                size = i2 - 1;
            } else {
                i = i2 + 1;
            }
        }
        return i;
    }

    public static void broadcastNewPicture(Context context, Uri uri) {
        if (VERSION.SDK_INT < 24) {
            context.sendBroadcast(new Intent("android.hardware.action.NEW_PICTURE", uri));
            context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
        }
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        return (KeyCharacterMap.deviceHasKey(82) || KeyCharacterMap.deviceHasKey(4)) ? false : true;
    }

    public static void checkLockedOrientation(Activity activity) {
        try {
            if (System.getInt(activity.getContentResolver(), "accelerometer_rotation") == 0) {
                mLockedOrientation = System.getInt(activity.getContentResolver(), "user_rotation");
            } else {
                mLockedOrientation = -1;
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static <T> T checkNotNull(T t) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException();
    }

    public static int clamp(int i, int i2, int i3) {
        return i > i3 ? i3 : i < i2 ? i2 : i;
    }

    public static void clearMemoryLimit() {
        if (!sClearMemoryLimit) {
            long currentTimeMillis = System.currentTimeMillis();
            VMRuntime.getRuntime().clearGrowthLimit();
            sClearMemoryLimit = true;
            Log.v("CameraUtil", "clearMemoryLimit() consume:" + (System.currentTimeMillis() - currentTimeMillis));
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    private static int computeInitialSampleSize(Options options, int i, int i2) {
        double d = (double) options.outWidth;
        double d2 = (double) options.outHeight;
        int ceil = i2 < 0 ? 1 : (int) Math.ceil(Math.sqrt((d * d2) / ((double) i2)));
        int min = i < 0 ? 128 : (int) Math.min(Math.floor(d / ((double) i)), Math.floor(d2 / ((double) i)));
        return min < ceil ? ceil : (i2 >= 0 || i >= 0) ? i < 0 ? ceil : min : 1;
    }

    public static int computeSampleSize(Options options, int i, int i2) {
        int computeInitialSampleSize = computeInitialSampleSize(options, i, i2);
        if (computeInitialSampleSize > 8) {
            return ((computeInitialSampleSize + 7) / 8) * 8;
        }
        int i3 = 1;
        while (i3 < computeInitialSampleSize) {
            i3 <<= 1;
        }
        return i3;
    }

    public static boolean createFile(File file) {
        if (file.exists()) {
            return false;
        }
        String parent = file.getParent();
        if (parent != null) {
            mkdirs(new File(parent), 511, -1, -1);
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        return true;
    }

    public static String createJpegName(long j) {
        String generateName;
        synchronized (sImageFileNamer) {
            generateName = sImageFileNamer.generateName(j);
        }
        return generateName;
    }

    public static int dpToPixel(float f) {
        return Math.round(sPixelDensity * f);
    }

    public static boolean equals(Object obj, Object obj2) {
        return obj != obj2 ? obj == null ? false : obj.equals(obj2) : true;
    }

    public static void expandViewTouchDelegate(View view) {
        if (view.isShown()) {
            Rect rect = new Rect();
            view.getHitRect(rect);
            int dpToPixel = dpToPixel(10.0f);
            rect.top -= dpToPixel;
            rect.bottom += dpToPixel;
            rect.left -= dpToPixel;
            rect.right += dpToPixel;
            TouchDelegate touchDelegate = new TouchDelegate(rect, view);
            if (View.class.isInstance(view.getParent())) {
                ((View) view.getParent()).setTouchDelegate(touchDelegate);
            }
        } else if (View.class.isInstance(view.getParent())) {
            ((View) view.getParent()).setTouchDelegate(null);
        }
    }

    public static void fadeIn(View view) {
        fadeIn(view, 400);
    }

    public static void fadeIn(View view, int i) {
        if (view != null && view.getVisibility() != 0) {
            view.setVisibility(0);
            Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration((long) i);
            view.clearAnimation();
            view.startAnimation(alphaAnimation);
        }
    }

    public static void fadeOut(View view) {
        fadeOut(view, 400);
    }

    public static void fadeOut(View view, int i) {
        if (view != null && view.getVisibility() == 0) {
            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration((long) i);
            view.clearAnimation();
            view.startAnimation(alphaAnimation);
            view.setVisibility(8);
        }
    }

    public static int getCameraFacingIntentExtras(Activity activity) {
        int intExtra = activity.getIntent().getIntExtra("android.intent.extras.CAMERA_FACING", -1);
        activity.getIntent().removeExtra("android.intent.extras.CAMERA_FACING");
        if (intExtra == -1 && ((activity.getIntent().getBooleanExtra("KEY_HANDOVER_THROUGH_VELVET", false) || TextUtils.equals(activity.getIntent().getStringExtra("android.intent.extra.REFERRER_NAME"), "android-app://com.google.android.googlequicksearchbox/https/www.google.com")) && activity.getIntent().hasExtra("android.intent.extra.USE_FRONT_CAMERA"))) {
            intExtra = activity.getIntent().getBooleanExtra("android.intent.extra.USE_FRONT_CAMERA", false) ? 1 : CameraHolder.instance().getBackCameraId();
        }
        if (isFrontCameraIntent(intExtra)) {
            int frontCameraId = CameraHolder.instance().getFrontCameraId();
            return frontCameraId != -1 ? frontCameraId : -1;
        } else if (isBackCameraIntent(intExtra)) {
            if (isViceBackIntent((ActivityBase) activity)) {
                int viceBackCameraId = CameraHolder.instance().getViceBackCameraId();
                return viceBackCameraId != -1 ? viceBackCameraId : -1;
            } else {
                int backCameraId = CameraHolder.instance().getBackCameraId();
                return backCameraId != -1 ? backCameraId : -1;
            }
        } else if (!isPortraitIntent((ActivityBase) activity)) {
            return -1;
        } else {
            int backCameraId2 = CameraHolder.instance().getBackCameraId();
            return backCameraId2 != -1 ? backCameraId2 : -1;
        }
    }

    public static int getCenterFocusDepthIndex(byte[] bArr, int i, int i2) {
        if (bArr == null || bArr.length < 25) {
            return 1;
        }
        int length = bArr.length - 25;
        int i3 = length + 1;
        if (bArr[length] != (byte) 0) {
            return 1;
        }
        length = i3 + 1;
        i3 = length + 1;
        length = i3 + 1;
        i3 = length + 1;
        int i4 = ((((bArr[i3] & 255) << 24) | ((bArr[length] & 255) << 16)) | ((bArr[i3] & 255) << 8)) | (bArr[length] & 255);
        length = i3 + 1;
        i3 = length + 1;
        length = i3 + 1;
        i3 = length + 1;
        int i5 = ((((bArr[i3] & 255) << 24) | ((bArr[length] & 255) << 16)) | ((bArr[i3] & 255) << 8)) | (bArr[length] & 255);
        int dimensionPixelSize = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.focus_area_width);
        int i6 = (i4 * dimensionPixelSize) / sWindowWidth;
        int dimensionPixelSize2 = (int) (((float) (i5 * CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.focus_area_height))) / ((((float) sWindowWidth) * ((float) i2)) / ((float) i)));
        int[] iArr = new int[5];
        int i7 = 0;
        int i8 = (i5 - dimensionPixelSize2) / 2;
        while (i7 < dimensionPixelSize2) {
            int i9 = i8 + 1;
            int i10 = 0;
            int i11 = (i8 * i4) + ((i4 - i6) / 2);
            while (i10 < i6) {
                int i12 = i11 + 1;
                byte b = bArr[i11];
                iArr[b] = iArr[b] + 1;
                i10++;
                i11 = i12;
            }
            i7++;
            i8 = i9;
        }
        int i13 = 0;
        for (i7 = 1; i7 < 5; i7++) {
            if (iArr[i13] < iArr[i7]) {
                i13 = i7;
            }
        }
        return i13;
    }

    public static String getDebugInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        if ("1".equals(SystemProperties.get("persist.camera.debug.show_af")) || "1".equals(SystemProperties.get("persist.camera.debug.enable"))) {
            stringBuilder.append(addProperties("persist.camera.debug.param0"));
            stringBuilder.append(addProperties("persist.camera.debug.param1"));
            stringBuilder.append(addProperties("persist.camera.debug.param2"));
            stringBuilder.append(addProperties("persist.camera.debug.param3"));
            stringBuilder.append(addProperties("persist.camera.debug.param4"));
            stringBuilder.append(addProperties("persist.camera.debug.param5"));
            stringBuilder.append(addProperties("persist.camera.debug.param6"));
            stringBuilder.append(addProperties("persist.camera.debug.param7"));
            stringBuilder.append(addProperties("persist.camera.debug.param8"));
            stringBuilder.append(addProperties("persist.camera.debug.param9"));
        }
        if ("1".equals(SystemProperties.get("persist.camera.debug.show_awb"))) {
            stringBuilder.append(addProperties("persist.camera.debug.param10"));
            stringBuilder.append(addProperties("persist.camera.debug.param11"));
            stringBuilder.append(addProperties("persist.camera.debug.param12"));
            stringBuilder.append(addProperties("persist.camera.debug.param13"));
            stringBuilder.append(addProperties("persist.camera.debug.param14"));
            stringBuilder.append(addProperties("persist.camera.debug.param15"));
            stringBuilder.append(addProperties("persist.camera.debug.param16"));
            stringBuilder.append(addProperties("persist.camera.debug.param17"));
            stringBuilder.append(addProperties("persist.camera.debug.param18"));
            stringBuilder.append(addProperties("persist.camera.debug.param19"));
        }
        if ("1".equals(SystemProperties.get("persist.camera.debug.show_aec"))) {
            stringBuilder.append(addProperties("persist.camera.debug.param20"));
            stringBuilder.append(addProperties("persist.camera.debug.param21"));
            stringBuilder.append(addProperties("persist.camera.debug.param22"));
            stringBuilder.append(addProperties("persist.camera.debug.param23"));
            stringBuilder.append(addProperties("persist.camera.debug.param24"));
            stringBuilder.append(addProperties("persist.camera.debug.param25"));
            stringBuilder.append(addProperties("persist.camera.debug.param26"));
            stringBuilder.append(addProperties("persist.camera.debug.param27"));
            stringBuilder.append(addProperties("persist.camera.debug.param28"));
            stringBuilder.append(addProperties("persist.camera.debug.param29"));
        }
        stringBuilder.append(addProperties("persist.camera.debug.checkerf"));
        stringBuilder.append(addProperties("persist.camera.debug.fc"));
        if ("1".equals(SystemProperties.get("persist.camera.debug.hht"))) {
            stringBuilder.append(addProperties("camera.debug.hht.luma"));
        }
        if ("1".equals(SystemProperties.get("persist.camera.debug.autoscene"))) {
            stringBuilder.append(addProperties("camera.debug.hht.iso"));
        }
        return stringBuilder.toString();
    }

    public static int getDisplayOrientation(int i, int i2) {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(i2, cameraInfo);
        return cameraInfo.facing == 1 ? (360 - ((cameraInfo.orientation + i) % 360)) % 360 : ((cameraInfo.orientation - i) + 360) % 360;
    }

    public static int getDisplayRotation(Activity activity) {
        int i = 0;
        if (activity.getRequestedOrientation() == 7) {
            i = activity.getWindowManager().getDefaultDisplay().getRotation();
        } else if (mLockedOrientation == 0 || mLockedOrientation == 2) {
            i = mLockedOrientation;
        }
        switch (i) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                return 0;
        }
    }

    public static int getIntField(String str, Object obj, String str2, String str3) {
        try {
            return ReflectUtil.getFieldInt(Class.forName(str), obj, str2, Integer.MIN_VALUE);
        } catch (ClassNotFoundException e) {
            Log.w("CameraUtil", "failed to get int field " + str + ", obj " + obj + "field " + str2);
            return Integer.MIN_VALUE;
        }
    }

    public static int getJpegRotation(int i, int i2) {
        CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[i];
        return i2 != -1 ? cameraInfo.facing == 1 ? ((cameraInfo.orientation - i2) + 360) % 360 : (cameraInfo.orientation + i2) % 360 : cameraInfo.orientation;
    }

    public static String getLocalizedNumberString(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            return String.format("%d", new Object[]{Integer.valueOf(str)});
        } catch (Exception e) {
            return str;
        }
    }

    public static Typeface getMiuiTimeTypeface(Context context) {
        return getTypeface(context, "fonts/MIUI_Time.ttf");
    }

    public static Typeface getMiuiTypeface(Context context) {
        return getTypeface(context, "fonts/MIUI_Normal.ttf");
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", "android"));
        Log.v("CameraUtil", "Navi height:" + dimensionPixelSize);
        return dimensionPixelSize;
    }

    public static boolean getNeedSealCameraUUIDIntentExtras(Activity activity) {
        return activity.getIntent().getBooleanExtra("android.intent.extras.EXTRAS_SEAL_CAMERAUUID_WATERMARK", false);
    }

    public static Size getOptimalJpegThumbnailSize(List<Size> list, double d) {
        if (list == null) {
            return null;
        }
        Size size = null;
        double d2 = 0.0d;
        for (Size size2 : list) {
            if (!(size2.width == 0 || size2.height == 0)) {
                double d3 = ((double) size2.width) / ((double) size2.height);
                double abs = Math.abs(d3 - d);
                if (abs <= Math.abs(d2 - d) || abs <= 0.001d) {
                    if (size != null && abs >= Math.abs(d2 - d)) {
                        if (size2.width > size.width) {
                        }
                    }
                    size = size2;
                    d2 = d3;
                }
            }
        }
        if (size == null) {
            Log.w("CameraUtil", "No thumbnail size match the aspect ratio");
            for (Size size22 : list) {
                if (size == null || size22.width > size.width) {
                    size = size22;
                }
            }
        }
        return size;
    }

    public static Size getOptimalPreviewSize(Activity activity, List<Size> list, double d) {
        if (list == null) {
            return null;
        }
        Size size = null;
        Size size2 = null;
        double d2 = Double.MAX_VALUE;
        double d3 = Double.MAX_VALUE;
        Object obj = null;
        int integer = FeatureParser.getInteger("camera_reduce_preview_flag", 0);
        if (integer != 0) {
            boolean isFrontCamera = CameraSettingPreferences.instance().isFrontCamera();
            if (sWindowWidth < 1080) {
                integer &= -15;
            }
            obj = (integer & (((isFrontCamera ? 2 : 1) << (!V6ModulePicker.isVideoModule() ? 0 : 2)) | 0)) != 0 ? 1 : null;
        }
        Point point = new Point(sWindowWidth, sWindowHeight);
        int i = (Device.isMDPRender() || !Device.isSurfaceSizeLimited()) ? 1080 : 720;
        if (point.x > i) {
            point.y = (point.y * i) / point.x;
            point.x = i;
        }
        for (Size size3 : list) {
            int abs;
            if (Math.abs((((double) size3.width) / ((double) size3.height)) - d) <= 0.02d && (r18 == null || (point.x > size3.height && point.y > size3.width))) {
                abs = Math.abs(point.x - size3.height) + Math.abs(point.y - size3.width);
                if (abs == 0) {
                    size = size3;
                    size2 = size3;
                    break;
                }
                if (size3.height <= point.x && size3.width <= point.y && ((double) abs) < r8) {
                    size2 = size3;
                    d3 = (double) abs;
                }
                if (((double) abs) < d2) {
                    size = size3;
                    d2 = (double) abs;
                }
            }
        }
        if (size2 != null) {
            size = size2;
        }
        if (size == null) {
            Log.w("CameraUtil", "No preview size match the aspect ratio");
            d2 = Double.MAX_VALUE;
            for (Size size32 : list) {
                abs = Math.abs(point.x - size32.height) + Math.abs(point.y - size32.width);
                if (((double) abs) < d2) {
                    size = size32;
                    d2 = (double) abs;
                }
            }
        }
        if (size != null) {
            Log.i("CameraUtil", "The best preview size is :(" + size.width + " , " + size.height + ")");
        }
        return size;
    }

    public static Size getOptimalVideoSnapshotPictureSize(List<Size> list, double d, int i, int i2) {
        if (list == null) {
            return null;
        }
        Size size = null;
        for (Size size2 : list) {
            if (Math.abs((((double) size2.width) / ((double) size2.height)) - d) <= 0.02d && ((size == null || size2.width > size.width) && size2.width <= i && size2.height <= i2)) {
                size = size2;
            }
        }
        if (size == null) {
            Log.w("CameraUtil", "No picture size match the aspect ratio");
            for (Size size22 : list) {
                if (size == null || size22.width > size.width) {
                    size = size22;
                }
            }
        }
        return size;
    }

    public static int[] getRelativeLocation(View view, View view2) {
        int[] iArr = new int[2];
        view.getLocationInWindow(iArr);
        int i = iArr[0];
        int i2 = iArr[1];
        view2.getLocationInWindow(iArr);
        iArr[0] = iArr[0] - i;
        iArr[1] = iArr[1] - i2;
        return iArr;
    }

    public static double getScreenInches(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        double sqrt = Math.sqrt(Math.pow((double) (((float) sWindowWidth) / displayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) sWindowHeight) / displayMetrics.ydpi), 2.0d));
        Log.d("CameraUtil", "getScreenInches = " + sqrt);
        return sqrt;
    }

    public static int getShootOrientation(Activity activity, int i) {
        return ((i - getDisplayRotation(activity)) + 360) % 360;
    }

    public static float getShootRotation(Activity activity, float f) {
        f -= (float) getDisplayRotation(activity);
        while (f < 0.0f) {
            f += 360.0f;
        }
        while (f > 360.0f) {
            f -= 360.0f;
        }
        return f;
    }

    public static int getStartCameraId(Activity activity) {
        int i = -1;
        if (activity.getIntent().getBooleanExtra("android.intent.extras.START_WITH_FRONT_CAMERA", false)) {
            i = CameraHolder.instance().getFrontCameraId();
        } else if (activity.getIntent().getBooleanExtra("android.intent.extras.START_WITH_BACK_CAMERA", false)) {
            i = CameraHolder.instance().getBackCameraId();
        }
        activity.getIntent().removeExtra("android.intent.extras.START_WITH_FRONT_CAMERA");
        activity.getIntent().removeExtra("android.intent.extras.START_WITH_BACK_CAMERA");
        return i;
    }

    public static int getStartModuleIndex(Activity activity) {
        return "android.media.action.STILL_IMAGE_CAMERA".equals(activity.getIntent().getAction()) ? 0 : "android.media.action.VIDEO_CAMERA".equals(activity.getIntent().getAction()) ? 1 : -1;
    }

    public static String getTimeWatermark() {
        return getTimeWatermark(Device.isSupportedNewStyleTimeWaterMark());
    }

    public static String getTimeWatermark(boolean z) {
        StringBuilder stringBuilder = new StringBuilder();
        if (z) {
            stringBuilder.append(new SimpleDateFormat("yyyy/M/d", Locale.ENGLISH).format(new Date()).toCharArray());
        } else {
            stringBuilder.append(new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH).format(new Date()).toCharArray());
        }
        stringBuilder.append(" ");
        new Time().set(System.currentTimeMillis());
        stringBuilder.append(String.format(Locale.ENGLISH, "%02d", new Object[]{Integer.valueOf(r2.hour)}));
        stringBuilder.append(":");
        stringBuilder.append(String.format(Locale.ENGLISH, "%02d", new Object[]{Integer.valueOf(r2.minute)}));
        return stringBuilder.toString();
    }

    private static synchronized Typeface getTypeface(Context context, String str) {
        Typeface typeface;
        synchronized (Util.class) {
            if (!sTypefaces.containsKey(str)) {
                sTypefaces.put(str, Typeface.createFromAsset(context.getAssets(), str));
            }
            typeface = (Typeface) sTypefaces.get(str);
        }
        return typeface;
    }

    public static void getWindowAttribute(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        sPixelDensity = displayMetrics.noncompatDensity;
        Display defaultDisplay = windowManager.getDefaultDisplay();
        Point point = new Point();
        if (Device.IS_A8 || Device.IS_D5) {
            ReflectUtil.callMethod(Display.class, defaultDisplay, "getRealSize", "(Landroid/graphics/Point;Z)V", point, Boolean.valueOf(true));
        } else {
            defaultDisplay.getRealSize(point);
        }
        if (point.x < point.y) {
            sWindowWidth = point.x;
            sWindowHeight = point.y;
        } else {
            sWindowWidth = point.y;
            sWindowHeight = point.x;
        }
        Log.d("CameraUtil", "Width = " + sWindowWidth + " Height = " + sWindowHeight + " sPixelDensity=" + sPixelDensity);
    }

    public static void initialize(Context context) {
        sImageFileNamer = new ImageFileNamer(context.getString(C0049R.string.image_file_name_format));
        getWindowAttribute(context);
    }

    public static boolean isActivityInvert(Activity activity) {
        return getDisplayRotation(activity) == 180;
    }

    public static boolean isAntibanding60() {
        return ANTIBANDING_60_COUNTRY.contains(mCountryIso);
    }

    public static final boolean isAppLocked(Context context, String str) {
        return false;
    }

    private static boolean isBackCameraIntent(int i) {
        return i == 0;
    }

    public static boolean isContaints(Rect rect, RectF rectF) {
        boolean z = false;
        if (rect == null || rectF == null) {
            return false;
        }
        if (rect.left < rect.right && rect.top < rect.bottom && ((float) rect.left) <= rectF.left && ((float) rect.top) <= rectF.top && ((float) rect.right) >= rectF.right && ((float) rect.bottom) >= rectF.bottom) {
            z = true;
        }
        return z;
    }

    public static boolean isFingerPrintKeyEvent(KeyEvent keyEvent) {
        return (keyEvent == null || 27 != keyEvent.getKeyCode() || keyEvent.getDevice() == null) ? false : Device.getFpNavEventNameList().contains(keyEvent.getDevice().getName());
    }

    public static boolean isForceCamera0() {
        return new File(Storage.generatePrimaryFilepath("force_camera_0")).exists();
    }

    private static boolean isFrontCameraIntent(int i) {
        return i == 1;
    }

    public static boolean isInVideoCall(Context context) {
        if (Device.isMTKPlatform() && 23 <= VERSION.SDK_INT) {
            try {
                return ((Boolean) Class.forName("android.telecom.TelecomManager").getMethod("isInVideoCall", new Class[0]).invoke(context.getSystemService("telecom"), new Object[0])).booleanValue();
            } catch (Throwable e) {
                Log.e("CameraUtil", "check isInVideoCall Exception", e);
            }
        }
        return false;
    }

    public static boolean isLayoutRTL(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (context.getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isMemoryRich(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem > 419430400;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isPortraitIntent(com.android.camera.ActivityBase r3) {
        /*
        r2 = 0;
        r0 = com.android.camera.CameraSettings.isSupportedPortrait();
        if (r0 != 0) goto L_0x0008;
    L_0x0007:
        return r2;
    L_0x0008:
        if (r3 == 0) goto L_0x0010;
    L_0x000a:
        r0 = r3.getIntent();
        if (r0 != 0) goto L_0x0011;
    L_0x0010:
        return r2;
    L_0x0011:
        r0 = r3.isImageCaptureIntent();
        if (r0 != 0) goto L_0x0018;
    L_0x0017:
        return r2;
    L_0x0018:
        r0 = r3.getIntent();
        r1 = "android.intent.extras.PORTRAIT";
        r0 = r0.getBooleanExtra(r1, r2);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.Util.isPortraitIntent(com.android.camera.ActivityBase):boolean");
    }

    public static boolean isProduceFocusInfoSuccess(byte[] bArr) {
        return bArr != null && 25 < bArr.length && bArr[bArr.length - 25] == (byte) 0;
    }

    public static boolean isShowDebugInfo() {
        return ("1".equals(SystemProperties.get("persist.camera.enable.log")) || "1".equals(SystemProperties.get("persist.camera.debug.show_af")) || "1".equals(SystemProperties.get("persist.camera.debug.show_awb")) || "1".equals(SystemProperties.get("persist.camera.debug.show_aec")) || "1".equals(SystemProperties.get("persist.camera.debug.autoscene"))) ? true : "1".equals(SystemProperties.get("persist.camera.debug.hht"));
    }

    public static boolean isSupported(String str, List<String> list) {
        return list != null && list.indexOf(str) >= 0;
    }

    public static boolean isTimeout(long j, long j2, long j3) {
        return j < j2 || j - j2 > j3;
    }

    public static boolean isUriValid(Uri uri, ContentResolver contentResolver) {
        if (uri == null) {
            return false;
        }
        try {
            ParcelFileDescriptor openFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if (openFileDescriptor == null) {
                Log.e("CameraUtil", "Fail to open URI. URI=" + uri);
                return false;
            }
            openFileDescriptor.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isViceBackIntent(com.android.camera.ActivityBase r3) {
        /*
        r2 = 0;
        if (r3 == 0) goto L_0x0009;
    L_0x0003:
        r0 = r3.getIntent();
        if (r0 != 0) goto L_0x000a;
    L_0x0009:
        return r2;
    L_0x000a:
        r0 = r3.isImageCaptureIntent();
        if (r0 != 0) goto L_0x0011;
    L_0x0010:
        return r2;
    L_0x0011:
        r0 = r3.getIntent();
        r1 = "android.intent.extras.EXTRAS_CAMERA_VICE_BACK";
        r0 = r0.getBooleanExtra(r1, r2);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.Util.isViceBackIntent(com.android.camera.ActivityBase):boolean");
    }

    public static Bitmap makeBitmap(byte[] bArr, int i) {
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
            if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
                return null;
            }
            options.inSampleSize = computeSampleSize(options, -1, i);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        } catch (Throwable e) {
            Log.e("CameraUtil", "Got oom exception ", e);
            return null;
        }
    }

    public static boolean mkdirs(File file, int i, int i2, int i3) {
        if (file.exists()) {
            return false;
        }
        String parent = file.getParent();
        if (parent != null) {
            mkdirs(new File(parent), i, i2, i3);
        }
        return file.mkdir();
    }

    public static CameraProxy openCamera(Activity activity, int i) throws CameraHardwareException, CameraDisabledException {
        if (((DevicePolicyManager) activity.getSystemService("device_policy")).getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }
        try {
            return CameraHolder.instance().open(i, ((ActivityBase) activity).getCurrentModule().isCaptureIntent() ? isPortraitIntent((ActivityBase) activity) : true);
        } catch (Throwable e) {
            if ("eng".equals(Build.TYPE)) {
                throw new RuntimeException("openCamera failed", e);
            }
            throw e;
        }
    }

    public static boolean pointInView(float f, float f2, View view) {
        boolean z = true;
        if (view == null) {
            return false;
        }
        int[] iArr = new int[2];
        view.getLocationInWindow(iArr);
        if (f < ((float) iArr[0]) || f >= ((float) (iArr[0] + view.getWidth())) || f2 < ((float) iArr[1])) {
            z = false;
        } else if (f2 >= ((float) (iArr[1] + view.getHeight()))) {
            z = false;
        }
        return z;
    }

    public static void prepareMatrix(Matrix matrix, boolean z, int i, int i2, int i3, int i4, int i5) {
        matrix.setScale((float) (z ? -1 : 1), 1.0f);
        matrix.postRotate((float) i);
        matrix.postScale(((float) i2) / 2000.0f, ((float) i3) / 2000.0f);
        matrix.postTranslate((float) i4, (float) i5);
    }

    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static int replaceStartEffectRender(Activity activity) {
        if (Device.isSupportedShaderEffect()) {
            String stringExtra = activity.getIntent().getStringExtra("android.intent.extras.START_WITH_EFFECT_RENDER");
            if (stringExtra != null) {
                int identifier = activity.getResources().getIdentifier(stringExtra, "string", activity.getPackageName());
                if (identifier != 0) {
                    int effectIndexByEntryName = EffectController.getInstance().getEffectIndexByEntryName(activity.getResources().getString(identifier));
                    CameraSettings.setShaderEffect(effectIndexByEntryName);
                    return effectIndexByEntryName;
                }
            }
        }
        return 0;
    }

    public static Bitmap rotate(Bitmap bitmap, int i) {
        return rotateAndMirror(bitmap, i, false);
    }

    public static Bitmap rotateAndMirror(Bitmap bitmap, int i, boolean z) {
        if ((i == 0 && !z) || bitmap == null) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        if (z) {
            matrix.postScale(-1.0f, 1.0f);
            i = (i + 360) % 360;
            if (i == 0 || i == 180) {
                matrix.postTranslate((float) bitmap.getWidth(), 0.0f);
            } else if (i == 90 || i == 270) {
                matrix.postTranslate((float) bitmap.getHeight(), 0.0f);
            } else {
                throw new IllegalArgumentException("Invalid degrees=" + i);
            }
        }
        if (i != 0) {
            matrix.postRotate((float) i, ((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        }
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap == createBitmap) {
                return bitmap;
            }
            bitmap.recycle();
            return createBitmap;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }

    public static int roundOrientation(int i, int i2) {
        Object obj;
        if (i2 == -1) {
            obj = 1;
        } else {
            int abs = Math.abs(i - i2);
            obj = Math.min(abs, 360 - abs) >= 50 ? 1 : null;
        }
        return obj != null ? (((i + 45) / 90) * 90) % 360 : i2;
    }

    public static int safeDelete(Uri uri, String str, String[] strArr) {
        int i = -1;
        try {
            i = CameraAppImpl.getAndroidContext().getContentResolver().delete(uri, str, strArr);
            Log.v("CameraUtil", "safeDelete url=" + uri + " where=" + str + " selectionArgs=" + strArr + " result=" + i);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    public static byte[] sealInvisibleWatermark(byte[] bArr, int i, String str) {
        Options options = new Options();
        options.inSampleSize = i;
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        if (decodeByteArray == null) {
            return null;
        }
        int orientation = Exif.getOrientation(bArr);
        if (orientation != 0) {
            decodeByteArray = rotate(decodeByteArray, orientation);
        }
        Bitmap encodeWatermark = SteganographyUtils.encodeWatermark(decodeByteArray, str);
        decodeByteArray.recycle();
        if (encodeWatermark == null) {
            return null;
        }
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        encodeWatermark.compress(CompressFormat.PNG, 100, byteArrayOutputStream);
        encodeWatermark.recycle();
        return byteArrayOutputStream.toByteArray();
    }

    public static void setGpsParameters(Parameters parameters, Location location) {
        Object obj = 1;
        parameters.removeGpsData();
        parameters.setGpsTimestamp(System.currentTimeMillis() / 1000);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (latitude == 0.0d && longitude == 0.0d) {
                obj = null;
            }
            if (obj != null) {
                Log.d("CameraUtil", "Set gps location");
                parameters.setGpsLatitude(latitude);
                parameters.setGpsLongitude(longitude);
                parameters.setGpsProcessingMethod(location.getProvider().toUpperCase());
                if (location.hasAltitude()) {
                    parameters.setGpsAltitude(location.getAltitude());
                } else {
                    parameters.setGpsAltitude(0.0d);
                }
                if (location.getTime() != 0) {
                    parameters.setGpsTimestamp(location.getTime() / 1000);
                }
            }
        }
    }

    public static void setNumberText(TextView textView, String str) {
        if (TextUtils.isDigitsOnly(str)) {
            textView.setText(getLocalizedNumberString(str));
        } else {
            textView.setText(str);
        }
    }

    public static void setRotationParameter(Parameters parameters, int i, int i2) {
        int i3 = 0;
        if (i2 != -1) {
            CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[i];
            i3 = cameraInfo.facing == 1 ? ((cameraInfo.orientation - i2) + 360) % 360 : (cameraInfo.orientation + i2) % 360;
        }
        parameters.setRotation(i3);
    }

    public static void showErrorAndFinish(final Activity activity, int i) {
        new Builder(activity).setCancelable(false).setIconAttribute(16843605).setTitle(C0049R.string.camera_error_title).setMessage(i).setNeutralButton(C0049R.string.dialog_ok, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.finish();
            }
        }).show();
    }

    public static void updateCountryIso(Context context) {
        Country detectCountry = ((CountryDetector) context.getSystemService("country_detector")).detectCountry();
        if (detectCountry != null) {
            mCountryIso = detectCountry.getCountryIso();
        }
        Log.v("CameraUtil", "antiBanding mCountryIso=" + mCountryIso);
        sIsDumpLog = SystemProperties.getBoolean("camera_dump_parameters", false);
    }
}
