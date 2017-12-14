package com.android.camera.storage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.android.camera.CameraAppImpl;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.ExifHelper;
import com.android.camera.Util;
import com.android.camera.XmpUtil;
import com.android.camera.aosp_porting.ReflectUtil;
import com.android.camera.effect.EffectController;
import com.android.gallery3d.exif.ExifInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Storage {
    public static int BUCKET_ID = DIRECTORY.toLowerCase().hashCode();
    public static String DIRECTORY = (FIRST_CONSIDER_STORAGE_PATH + "/DCIM/Camera");
    public static String FIRST_CONSIDER_STORAGE_PATH = (Device.IS_HM ? SECONDARY_STORAGE_PATH : PRIMARY_STORAGE_PATH);
    public static String HIDEDIRECTORY = (FIRST_CONSIDER_STORAGE_PATH + "/DCIM/Camera/.ubifocus");
    private static final AtomicLong LEFT_SPACE = new AtomicLong(0);
    public static int PRIMARY_BUCKET_ID = (PRIMARY_STORAGE_PATH + "/DCIM/Camera").toLowerCase().hashCode();
    private static final String PRIMARY_STORAGE_PATH = Environment.getExternalStorageDirectory().toString();
    public static int SECONDARY_BUCKET_ID = (SECONDARY_STORAGE_PATH + "/DCIM/Camera").toLowerCase().hashCode();
    private static String SECONDARY_STORAGE_PATH = System.getenv("SECONDARY_STORAGE");
    private static String sCurrentStoragePath = FIRST_CONSIDER_STORAGE_PATH;
    private static WeakReference<StorageListener> sStorageListener;

    public interface StorageListener {
        void onStoragePathChanged();
    }

    static {
        File file = new File(DIRECTORY + File.separator + ".nomedia");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void addDNGToDataBase(Activity activity, String str) {
        String generateFilepath = generateFilepath(str, ".dng");
        ContentValues contentValues = new ContentValues(4);
        contentValues.put("title", str);
        contentValues.put("_display_name", str + ".dng");
        contentValues.put("media_type", Integer.valueOf(1));
        contentValues.put("_data", generateFilepath);
        try {
            activity.getContentResolver().insert(Files.getContentUri("external"), contentValues);
        } catch (Exception e) {
            Log.e("CameraStorage", "Failed to write MediaStore" + e);
        }
    }

    public static Uri addImage(Context context, String str, int i, long j, Location location, int i2, int i3) {
        if (context == null || str == null) {
            return null;
        }
        File file = null;
        try {
            file = new File(str);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CameraStorage", "Failed to open panorama file." + e);
        }
        if (file == null || !file.exists()) {
            return null;
        }
        String name = file.getName();
        ContentValues contentValues = new ContentValues(11);
        contentValues.put("title", name);
        contentValues.put("_display_name", name);
        contentValues.put("datetaken", Long.valueOf(j));
        contentValues.put("mime_type", "image/jpeg");
        contentValues.put("orientation", Integer.valueOf(i));
        contentValues.put("_data", str);
        contentValues.put("_size", Long.valueOf(file.length()));
        contentValues.put("width", Integer.valueOf(i2));
        contentValues.put("height", Integer.valueOf(i3));
        if (location != null) {
            contentValues.put("latitude", Double.valueOf(location.getLatitude()));
            contentValues.put("longitude", Double.valueOf(location.getLongitude()));
        }
        Uri uri = null;
        try {
            uri = context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.e("CameraStorage", "Failed to write MediaStore" + e2);
        }
        saveToCloudAlbum(context, str);
        return uri;
    }

    public static Uri addImage(Context context, String str, long j, Location location, int i, byte[] bArr, int i2, int i3, boolean z, boolean z2, boolean z3) {
        return addImage(context, str, j, location, i, bArr, i2, i3, z, z2, z3, false);
    }

    public static Uri addImage(Context context, String str, long j, Location location, int i, byte[] bArr, int i2, int i3, boolean z, boolean z2, boolean z3, boolean z4) {
        return addImage(context, str, j, location, i, bArr, i2, i3, z, z2, z3, z4, false);
    }

    public static Uri addImage(Context context, String str, long j, Location location, int i, byte[] bArr, int i2, int i3, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return addImage(context, str, j, location, i, bArr, i2, i3, z, z2, z3, z4, z5, false);
    }

    public static Uri addImage(Context context, String str, long j, Location location, int i, byte[] bArr, int i2, int i3, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6) {
        Throwable e;
        boolean isProduceFocusInfoSuccess;
        int centerFocusDepthIndex;
        ContentValues contentValues;
        Uri uri;
        Throwable th;
        String generateFilepath = generateFilepath(str, z2, z3);
        if (z5) {
            generateFilepath = generateFilepath + ".thumb";
        }
        FileOutputStream fileOutputStream = null;
        Object obj = null;
        try {
            OutputStream fileOutputStream2 = new FileOutputStream(generateFilepath);
            if (z) {
                try {
                    Bitmap flipJpeg = flipJpeg(bArr);
                    if (flipJpeg != null) {
                        flipJpeg.compress(CompressFormat.JPEG, 100, fileOutputStream2);
                        flipJpeg.recycle();
                        z4 = true;
                    } else {
                        fileOutputStream2.write(bArr);
                    }
                } catch (Exception e2) {
                    e = e2;
                    fileOutputStream = fileOutputStream2;
                    try {
                        Log.e("CameraStorage", "Failed to write image", e);
                        obj = 1;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            } catch (Throwable e3) {
                                Log.e("CameraStorage", "Failed to flush/close stream", e3);
                                obj = 1;
                            }
                        }
                        if (obj == null) {
                            return null;
                        }
                        if (z3) {
                            isProduceFocusInfoSuccess = Util.isProduceFocusInfoSuccess(bArr);
                            centerFocusDepthIndex = Util.getCenterFocusDepthIndex(bArr, i2, i3);
                            str = str.substring(0, isProduceFocusInfoSuccess ? str.lastIndexOf("_") : str.lastIndexOf("_UBIFOCUS_"));
                            generateFilepath = generateFilepath(str, false, false);
                            new File(generateFilepath(str + (isProduceFocusInfoSuccess ? "_" : "_UBIFOCUS_") + centerFocusDepthIndex, z2, false)).renameTo(new File(generateFilepath));
                            if (!isProduceFocusInfoSuccess) {
                                deleteImage(str);
                            }
                        }
                        if (!z2) {
                        }
                        XmpUtil.addSpecialTypeMeta(generateFilepath);
                        contentValues = new ContentValues(12);
                        contentValues.put("title", str);
                        contentValues.put("_display_name", str + ".jpg");
                        contentValues.put("datetaken", Long.valueOf(j));
                        if (z5) {
                            contentValues.putNull("mime_type");
                            contentValues.put("media_type", Integer.valueOf(0));
                        } else {
                            contentValues.put("mime_type", "image/jpeg");
                        }
                        contentValues.put("orientation", Integer.valueOf(i));
                        contentValues.put("_data", generateFilepath);
                        contentValues.put("_size", Long.valueOf(new File(generateFilepath).length()));
                        contentValues.put("width", Integer.valueOf(i2));
                        contentValues.put("height", Integer.valueOf(i3));
                        if (location != null) {
                            contentValues.put("latitude", Double.valueOf(location.getLatitude()));
                            contentValues.put("longitude", Double.valueOf(location.getLongitude()));
                        }
                        uri = null;
                        if (z5) {
                            try {
                            } catch (Exception e4) {
                                e4.printStackTrace();
                                Log.e("CameraStorage", "Failed to write MediaStore" + e4);
                            }
                        }
                        if (!EffectController.getInstance().hasEffect()) {
                            saveToCloudAlbum(context, generateFilepath);
                        }
                        return uri;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            } catch (Throwable e32) {
                                Log.e("CameraStorage", "Failed to flush/close stream", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fileOutputStream2;
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    throw th;
                }
            }
            fileOutputStream2.write(bArr);
            if (z4) {
                fileOutputStream2.flush();
                ExifHelper.writeExif(generateFilepath, i, location, System.currentTimeMillis());
            }
            if (fileOutputStream2 != null) {
                try {
                    fileOutputStream2.flush();
                    fileOutputStream2.close();
                } catch (Throwable e322) {
                    Log.e("CameraStorage", "Failed to flush/close stream", e322);
                    obj = 1;
                }
            }
            OutputStream outputStream = fileOutputStream2;
        } catch (Exception e5) {
            e322 = e5;
            Log.e("CameraStorage", "Failed to write image", e322);
            obj = 1;
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            if (obj == null) {
                return null;
            }
            if (z3) {
                isProduceFocusInfoSuccess = Util.isProduceFocusInfoSuccess(bArr);
                centerFocusDepthIndex = Util.getCenterFocusDepthIndex(bArr, i2, i3);
                if (isProduceFocusInfoSuccess) {
                }
                str = str.substring(0, isProduceFocusInfoSuccess ? str.lastIndexOf("_") : str.lastIndexOf("_UBIFOCUS_"));
                generateFilepath = generateFilepath(str, false, false);
                if (isProduceFocusInfoSuccess) {
                }
                new File(generateFilepath(str + (isProduceFocusInfoSuccess ? "_" : "_UBIFOCUS_") + centerFocusDepthIndex, z2, false)).renameTo(new File(generateFilepath));
                if (isProduceFocusInfoSuccess) {
                    deleteImage(str);
                }
            }
            if (!z2) {
            }
            XmpUtil.addSpecialTypeMeta(generateFilepath);
            contentValues = new ContentValues(12);
            contentValues.put("title", str);
            contentValues.put("_display_name", str + ".jpg");
            contentValues.put("datetaken", Long.valueOf(j));
            if (z5) {
                contentValues.put("mime_type", "image/jpeg");
            } else {
                contentValues.putNull("mime_type");
                contentValues.put("media_type", Integer.valueOf(0));
            }
            contentValues.put("orientation", Integer.valueOf(i));
            contentValues.put("_data", generateFilepath);
            contentValues.put("_size", Long.valueOf(new File(generateFilepath).length()));
            contentValues.put("width", Integer.valueOf(i2));
            contentValues.put("height", Integer.valueOf(i3));
            if (location != null) {
                contentValues.put("latitude", Double.valueOf(location.getLatitude()));
                contentValues.put("longitude", Double.valueOf(location.getLongitude()));
            }
            uri = null;
            uri = z5 ? context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues) : context.getContentResolver().insert(Files.getContentUri("external"), contentValues);
            if (EffectController.getInstance().hasEffect()) {
                saveToCloudAlbum(context, generateFilepath);
            }
            return uri;
        }
        if (obj == null) {
            return null;
        }
        if (z3) {
            isProduceFocusInfoSuccess = Util.isProduceFocusInfoSuccess(bArr);
            centerFocusDepthIndex = Util.getCenterFocusDepthIndex(bArr, i2, i3);
            if (isProduceFocusInfoSuccess) {
            }
            str = str.substring(0, isProduceFocusInfoSuccess ? str.lastIndexOf("_") : str.lastIndexOf("_UBIFOCUS_"));
            generateFilepath = generateFilepath(str, false, false);
            if (isProduceFocusInfoSuccess) {
            }
            new File(generateFilepath(str + (isProduceFocusInfoSuccess ? "_" : "_UBIFOCUS_") + centerFocusDepthIndex, z2, false)).renameTo(new File(generateFilepath));
            if (isProduceFocusInfoSuccess) {
                deleteImage(str);
            }
        }
        if (!z2 && !z3) {
            return null;
        }
        if (z6 && !z5) {
            XmpUtil.addSpecialTypeMeta(generateFilepath);
        }
        contentValues = new ContentValues(12);
        contentValues.put("title", str);
        contentValues.put("_display_name", str + ".jpg");
        contentValues.put("datetaken", Long.valueOf(j));
        if (z5) {
            contentValues.putNull("mime_type");
            contentValues.put("media_type", Integer.valueOf(0));
        } else {
            contentValues.put("mime_type", "image/jpeg");
        }
        contentValues.put("orientation", Integer.valueOf(i));
        contentValues.put("_data", generateFilepath);
        contentValues.put("_size", Long.valueOf(new File(generateFilepath).length()));
        contentValues.put("width", Integer.valueOf(i2));
        contentValues.put("height", Integer.valueOf(i3));
        if (location != null) {
            contentValues.put("latitude", Double.valueOf(location.getLatitude()));
            contentValues.put("longitude", Double.valueOf(location.getLongitude()));
        }
        uri = null;
        if (z5) {
        }
        if (EffectController.getInstance().hasEffect()) {
            saveToCloudAlbum(context, generateFilepath);
        }
        return uri;
    }

    public static void deleteFromCloudAlbum(Context context, String str) {
        context.sendBroadcast(getDeleteFromCloudIntent(context, str));
    }

    public static void deleteImage(String str) {
        File file = new File(HIDEDIRECTORY);
        if (file.exists() && file.isDirectory()) {
            for (File file2 : file.listFiles()) {
                if (file2.getName().indexOf(str) != -1) {
                    file2.delete();
                }
            }
        }
    }

    public static Bitmap flipJpeg(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        Options options = new Options();
        options.inPurgeable = true;
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        Matrix matrix = new Matrix();
        matrix.setScale(-1.0f, 1.0f, ((float) decodeByteArray.getWidth()) * 0.5f, ((float) decodeByteArray.getHeight()) * 0.5f);
        try {
            Bitmap createBitmap = Bitmap.createBitmap(decodeByteArray, 0, 0, decodeByteArray.getWidth(), decodeByteArray.getHeight(), matrix, true);
            if (createBitmap != decodeByteArray) {
                decodeByteArray.recycle();
            }
            return (createBitmap.getWidth() == -1 || createBitmap.getHeight() == -1) ? null : createBitmap;
        } catch (Throwable e) {
            Log.w("CameraStorage", "Failed to rotate thumbnail", e);
            return null;
        }
    }

    public static String generateFilepath(String str) {
        return generateFilepath(str, ".jpg");
    }

    public static String generateFilepath(String str, String str2) {
        return DIRECTORY + '/' + str + str2;
    }

    public static String generateFilepath(String str, boolean z, boolean z2) {
        if (z && isLowStorageSpace(HIDEDIRECTORY)) {
            return null;
        }
        return (z ? HIDEDIRECTORY : DIRECTORY) + '/' + str + (z2 ? ".y" : ".jpg");
    }

    public static String generatePrimaryFilepath(String str) {
        return PRIMARY_STORAGE_PATH + "/DCIM/Camera" + '/' + str;
    }

    public static long getAvailableSpace() {
        return getAvailableSpace(DIRECTORY);
    }

    public static long getAvailableSpace(String str) {
        if (str == null) {
            return -1;
        }
        File file = new File(str);
        boolean mkdirs = Util.mkdirs(file, 511, -1, -1);
        if (!file.exists() || !file.isDirectory() || !file.canWrite()) {
            return -1;
        }
        if (mkdirs && str.endsWith("/DCIM/Camera")) {
            Intent intent = new Intent("miui.intent.action.MEDIA_SCANNER_SCAN_FOLDER");
            intent.setData(Uri.fromFile(file.getParentFile()));
            CameraAppImpl.getAndroidContext().sendBroadcast(intent);
        }
        try {
            if (HIDEDIRECTORY.equals(str)) {
                Util.createFile(new File(HIDEDIRECTORY + File.separator + ".nomedia"));
            }
            StatFs statFs = new StatFs(str);
            long availableBlocks = ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
            setLeftSpace(availableBlocks);
            return availableBlocks;
        } catch (Throwable e) {
            Log.i("CameraStorage", "Fail to access external storage", e);
            return -3;
        }
    }

    private static Intent getDeleteFromCloudIntent(Context context, String str) {
        Intent intent = new Intent("com.miui.gallery.DELETE_FROM_CLOUD");
        intent.setPackage("com.miui.gallery");
        List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(intent, 0);
        if (queryBroadcastReceivers != null && queryBroadcastReceivers.size() > 0) {
            intent.setComponent(new ComponentName("com.miui.gallery", ((ResolveInfo) queryBroadcastReceivers.get(0)).activityInfo.name));
        }
        intent.putExtra("extra_file_path", str);
        return intent;
    }

    public static long getLeftSpace() {
        long j = LEFT_SPACE.get();
        Log.i("CameraStorage", "getLeftSpace() return " + j);
        return j;
    }

    private static Intent getSaveToCloudIntent(Context context, String str) {
        Intent intent = new Intent("com.miui.gallery.SAVE_TO_CLOUD");
        intent.setPackage("com.miui.gallery");
        List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(intent, 0);
        if (queryBroadcastReceivers != null && queryBroadcastReceivers.size() > 0) {
            intent.setComponent(new ComponentName("com.miui.gallery", ((ResolveInfo) queryBroadcastReceivers.get(0)).activityInfo.name));
        }
        intent.putExtra("extra_file_path", str);
        return intent;
    }

    public static boolean hasSecondaryStorage() {
        return Device.isSupportedSecondaryStorage() && SECONDARY_STORAGE_PATH != null;
    }

    public static void initStorage(Context context) {
        if (Device.isSupportedSecondaryStorage()) {
            if (VERSION.SDK_INT >= 23) {
                StorageManager storageManager = (StorageManager) context.getSystemService("storage");
                try {
                    Object callMethod = ReflectUtil.callMethod(storageManager.getClass(), storageManager, "getVolumes", "()Ljava/util/List;", new Object[0]);
                    Class cls = Class.forName("android.os.storage.VolumeInfo");
                    Object obj = null;
                    if (callMethod != null && (callMethod instanceof List)) {
                        int fieldInt = ReflectUtil.getFieldInt(cls, null, "TYPE_PUBLIC", -1);
                        Class cls2 = Class.forName("android.os.storage.DiskInfo");
                        for (Object next : (List) callMethod) {
                            Object callMethod2 = ReflectUtil.callMethod(cls, next, "getType", "()I", new Object[0]);
                            if (callMethod2 != null && ((Integer) callMethod2).intValue() == fieldInt) {
                                Object callMethod3 = ReflectUtil.callMethod(cls, next, "isMountedWritable", "()Z", new Object[0]);
                                if (callMethod3 != null && ((Boolean) callMethod3).booleanValue()) {
                                    Object callMethod4 = ReflectUtil.callMethod(cls, next, "getDisk", "()Landroid/os/storage/DiskInfo;", new Object[0]);
                                    if (callMethod4 != null) {
                                        Object callMethod5 = ReflectUtil.callMethod(cls2, callMethod4, "isSd", "()Z", new Object[0]);
                                        if (callMethod5 != null && ((Boolean) callMethod5).booleanValue()) {
                                            obj = next;
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                        }
                    }
                    if (obj != null) {
                        Object callMethod6 = ReflectUtil.callMethod(cls, obj, "getPath", "()Ljava/io/File;", new Object[0]);
                        String path = callMethod6 == null ? null : ((File) callMethod6).getPath();
                        if (path != null) {
                            Log.v("CameraStorage", "initStorage sd=" + path);
                            SECONDARY_STORAGE_PATH = path;
                            SECONDARY_BUCKET_ID = (SECONDARY_STORAGE_PATH + "/DCIM/Camera").toLowerCase().hashCode();
                        }
                    }
                } catch (Throwable e) {
                    Log.e("CameraStorage", "initStorage Exception ", e);
                    e.printStackTrace();
                }
            }
            readSystemPriorityStorage();
        }
    }

    public static boolean isCurrentStorageIsSecondary() {
        return SECONDARY_STORAGE_PATH != null ? SECONDARY_STORAGE_PATH.equals(sCurrentStoragePath) : false;
    }

    public static boolean isLowStorageAtLastPoint() {
        return getLeftSpace() < 52428800;
    }

    public static boolean isLowStorageSpace(String str) {
        return getAvailableSpace(str) < 52428800;
    }

    public static boolean isPhoneStoragePriority() {
        return PRIMARY_STORAGE_PATH.equals(FIRST_CONSIDER_STORAGE_PATH);
    }

    public static boolean isRelatedStorage(Uri uri) {
        boolean z = false;
        if (uri == null) {
            return false;
        }
        String path = uri.getPath();
        if (path != null) {
            z = !path.equals(PRIMARY_STORAGE_PATH) ? path.equals(SECONDARY_STORAGE_PATH) : true;
        }
        return z;
    }

    public static boolean isUsePhoneStorage() {
        return PRIMARY_STORAGE_PATH.equals(sCurrentStoragePath);
    }

    public static Uri newImage(Context context, String str, long j, int i, int i2, int i3) {
        String generateFilepath = generateFilepath(str);
        ContentValues contentValues = new ContentValues(6);
        contentValues.put("datetaken", Long.valueOf(j));
        contentValues.put("orientation", Integer.valueOf(i));
        contentValues.put("_data", generateFilepath);
        contentValues.put("width", Integer.valueOf(i2));
        contentValues.put("height", Integer.valueOf(i3));
        contentValues.put("mime_type", "image/jpeg");
        Uri uri = null;
        try {
            uri = context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
        } catch (Exception e) {
            Log.e("CameraStorage", "Failed to new image" + e);
        }
        return uri;
    }

    public static void readSystemPriorityStorage() {
        boolean z = false;
        if (hasSecondaryStorage()) {
            z = PriorityStorageBroadcastReceiver.isPriorityStorage();
            CameraSettings.setPriorityStoragePreference(z);
        }
        FIRST_CONSIDER_STORAGE_PATH = z ? SECONDARY_STORAGE_PATH : PRIMARY_STORAGE_PATH;
        sCurrentStoragePath = FIRST_CONSIDER_STORAGE_PATH;
        updateDirectory();
    }

    public static void saveToCloudAlbum(Context context, String str) {
        context.sendBroadcast(getSaveToCloudIntent(context, str));
    }

    public static boolean secondaryStorageMounted() {
        return hasSecondaryStorage() && getAvailableSpace(SECONDARY_STORAGE_PATH) > 0;
    }

    private static void setLeftSpace(long j) {
        LEFT_SPACE.set(j);
        Log.i("CameraStorage", "setLeftSpace(" + j + ")");
    }

    public static void setStorageListener(StorageListener storageListener) {
        if (storageListener != null) {
            sStorageListener = new WeakReference(storageListener);
        }
    }

    public static void switchStoragePathIfNeeded() {
        if (hasSecondaryStorage()) {
            String str = FIRST_CONSIDER_STORAGE_PATH;
            String str2 = SECONDARY_STORAGE_PATH;
            if (FIRST_CONSIDER_STORAGE_PATH.equals(SECONDARY_STORAGE_PATH)) {
                str2 = PRIMARY_STORAGE_PATH;
            }
            String str3 = sCurrentStoragePath;
            if (!isLowStorageSpace(str)) {
                sCurrentStoragePath = str;
            } else if (!isLowStorageSpace(str2)) {
                sCurrentStoragePath = str2;
            } else {
                return;
            }
            if (!sCurrentStoragePath.equals(str3)) {
                updateDirectory();
                if (!(sStorageListener == null || sStorageListener.get() == null)) {
                    ((StorageListener) sStorageListener.get()).onStoragePathChanged();
                }
            }
            Log.i("CameraStorage", "Storage path is switched path = " + DIRECTORY);
        }
    }

    public static void switchToPhoneStorage() {
        FIRST_CONSIDER_STORAGE_PATH = PRIMARY_STORAGE_PATH;
        if (!PRIMARY_STORAGE_PATH.equals(sCurrentStoragePath)) {
            Log.v("CameraStorage", "switchToPhoneStorage");
            sCurrentStoragePath = PRIMARY_STORAGE_PATH;
            updateDirectory();
            if (sStorageListener != null && sStorageListener.get() != null) {
                ((StorageListener) sStorageListener.get()).onStoragePathChanged();
            }
        }
    }

    private static void updateDirectory() {
        DIRECTORY = sCurrentStoragePath + "/DCIM/Camera";
        HIDEDIRECTORY = sCurrentStoragePath + "/DCIM/Camera/.ubifocus";
        BUCKET_ID = DIRECTORY.toLowerCase().hashCode();
    }

    public static boolean updateImage(Context context, byte[] bArr, ExifInterface exifInterface, Uri uri, String str, Location location, int i, int i2, int i3, String str2, boolean z) {
        OutputStream outputStream;
        Throwable e;
        File file;
        long length;
        Throwable th;
        String generateFilepath = generateFilepath(str);
        String str3 = (str2 != null ? generateFilepath(str2) : generateFilepath) + ".tmp";
        FileOutputStream fileOutputStream = null;
        if (bArr != null) {
            try {
                OutputStream fileOutputStream2 = new FileOutputStream(str3);
                if (exifInterface != null) {
                    try {
                        exifInterface.writeExif(bArr, fileOutputStream2);
                        fileOutputStream = fileOutputStream2;
                    } catch (IOException e2) {
                        try {
                            Log.e("CameraStorage", "Failed to rewrite Exif");
                            fileOutputStream2.write(bArr);
                            outputStream = fileOutputStream2;
                        } catch (Exception e3) {
                            e = e3;
                            outputStream = fileOutputStream2;
                            try {
                                Log.e("CameraStorage", "Failed to write image", e);
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                    } catch (Throwable e4) {
                                        Log.e("CameraStorage", "Failed to flush/close stream", e4);
                                    }
                                }
                                file = new File(str3);
                                length = file.length();
                                file.renameTo(new File(generateFilepath));
                                try {
                                    new File(generateFilepath(str2)).delete();
                                } catch (Throwable e42) {
                                    Log.e("CameraStorage", "Exception when delete oldfile " + str2, e42);
                                }
                                return false;
                            } catch (Throwable th2) {
                                th = th2;
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                    } catch (Throwable e422) {
                                        Log.e("CameraStorage", "Failed to flush/close stream", e422);
                                    }
                                }
                                file = new File(str3);
                                length = file.length();
                                file.renameTo(new File(generateFilepath));
                                if (!(exifInterface == null || str2 == null)) {
                                    try {
                                        new File(generateFilepath(str2)).delete();
                                    } catch (Throwable e4222) {
                                        Log.e("CameraStorage", "Exception when delete oldfile " + str2, e4222);
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            outputStream = fileOutputStream2;
                            if (fileOutputStream != null) {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            }
                            file = new File(str3);
                            length = file.length();
                            file.renameTo(new File(generateFilepath));
                            new File(generateFilepath(str2)).delete();
                            throw th;
                        }
                    }
                }
                fileOutputStream2.write(bArr);
                outputStream = fileOutputStream2;
            } catch (Exception e5) {
                e4222 = e5;
                Log.e("CameraStorage", "Failed to write image", e4222);
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                file = new File(str3);
                length = file.length();
                file.renameTo(new File(generateFilepath));
                if (!(exifInterface == null || str2 == null)) {
                    new File(generateFilepath(str2)).delete();
                }
                return false;
            }
        } else if (str2 != null) {
            str3 = generateFilepath(str2);
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Throwable e42222) {
                Log.e("CameraStorage", "Failed to flush/close stream", e42222);
            }
        }
        file = new File(str3);
        length = file.length();
        file.renameTo(new File(generateFilepath));
        if (!(exifInterface == null || str2 == null)) {
            try {
                new File(generateFilepath(str2)).delete();
            } catch (Throwable e422222) {
                Log.e("CameraStorage", "Exception when delete oldfile " + str2, e422222);
            }
        }
        if (z) {
            XmpUtil.addSpecialTypeMeta(generateFilepath);
        }
        ContentValues contentValues = new ContentValues(11);
        contentValues.put("title", str);
        contentValues.put("_display_name", str + ".jpg");
        if (bArr != null) {
            contentValues.put("mime_type", "image/jpeg");
            contentValues.put("media_type", Integer.valueOf(1));
            contentValues.put("orientation", Integer.valueOf(i));
            contentValues.put("_size", Long.valueOf(length));
            contentValues.put("width", Integer.valueOf(i2));
            contentValues.put("height", Integer.valueOf(i3));
            if (location != null) {
                contentValues.put("latitude", Double.valueOf(location.getLatitude()));
                contentValues.put("longitude", Double.valueOf(location.getLongitude()));
            }
            contentValues.put("_data", generateFilepath);
        } else if (str2 != null) {
            contentValues.put("_data", generateFilepath);
        }
        try {
            context.getContentResolver().update(uri, contentValues, null, null);
            if (str2 != null) {
                deleteFromCloudAlbum(context, generateFilepath(str2));
            }
            saveToCloudAlbum(context, generateFilepath);
            return true;
        } catch (Exception e6) {
            Log.e("CameraStorage", "Failed to update image" + e6);
            return false;
        }
    }
}
