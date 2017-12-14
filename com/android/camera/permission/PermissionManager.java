package com.android.camera.permission;

import android.app.Activity;
import android.os.Build.VERSION;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.android.camera.CameraAppImpl;
import com.android.camera.Device;
import com.android.camera.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {
    private static List<String> mLauchPermissionList = new ArrayList();
    private static List<String> sLocationPermissionList = new ArrayList();
    private static List<String> sRuntimePermissions = new ArrayList();

    static {
        sLocationPermissionList.add("android.permission.ACCESS_FINE_LOCATION");
        sLocationPermissionList.add("android.permission.ACCESS_COARSE_LOCATION");
        mLauchPermissionList.add("android.permission.CAMERA");
        mLauchPermissionList.add("android.permission.RECORD_AUDIO");
        mLauchPermissionList.add("android.permission.WRITE_EXTERNAL_STORAGE");
        sRuntimePermissions.addAll(mLauchPermissionList);
        sRuntimePermissions.addAll(sLocationPermissionList);
        if (Device.isMTKPlatform()) {
            sRuntimePermissions.add("android.permission.READ_PHONE_STATE");
        }
    }

    public static boolean checkCameraLaunchPermissions() {
        if (VERSION.SDK_INT < 23 || !Device.isPermissionFixe()) {
            return true;
        }
        if (getNeedCheckPermissionList(mLauchPermissionList).size() > 0) {
            return false;
        }
        Log.m4i("PermissionManager", "CheckCameraPermissions(), all on");
        return true;
    }

    public static boolean checkCameraLocationPermissions() {
        if (VERSION.SDK_INT < 23 || !Device.isPermissionFixe()) {
            return true;
        }
        if (getNeedCheckPermissionList(sLocationPermissionList).size() > 0) {
            return false;
        }
        Log.m4i("PermissionManager", "checkCameraLocationPermissions(), all on");
        return true;
    }

    public static int getCameraRuntimePermissionRequestCode() {
        return 100;
    }

    private static List<String> getNeedCheckPermissionList(List<String> list) {
        if (list.size() <= 0) {
            return list;
        }
        List<String> arrayList = new ArrayList();
        for (String str : list) {
            if (ContextCompat.checkSelfPermission(CameraAppImpl.getAndroidContext(), str) != 0) {
                Log.m4i("PermissionManager", "getNeedCheckPermissionList() permission =" + str);
                arrayList.add(str);
            }
        }
        Log.m4i("PermissionManager", "getNeedCheckPermissionList() listSize =" + arrayList.size());
        return arrayList;
    }

    public static boolean isCameraLaunchPermissionsResultReady(String[] strArr, int[] iArr) {
        Map hashMap = new HashMap();
        hashMap.put("android.permission.CAMERA", Integer.valueOf(0));
        hashMap.put("android.permission.RECORD_AUDIO", Integer.valueOf(0));
        hashMap.put("android.permission.WRITE_EXTERNAL_STORAGE", Integer.valueOf(0));
        for (int i = 0; i < strArr.length; i++) {
            hashMap.put(strArr[i], Integer.valueOf(iArr[i]));
        }
        return ((Integer) hashMap.get("android.permission.CAMERA")).intValue() == 0 && ((Integer) hashMap.get("android.permission.RECORD_AUDIO")).intValue() == 0 && ((Integer) hashMap.get("android.permission.WRITE_EXTERNAL_STORAGE")).intValue() == 0;
    }

    public static boolean isCameraLocationPermissionsResultReady(String[] strArr, int[] iArr) {
        Map hashMap = new HashMap();
        hashMap.put("android.permission.ACCESS_COARSE_LOCATION", Integer.valueOf(0));
        hashMap.put("android.permission.ACCESS_FINE_LOCATION", Integer.valueOf(0));
        for (int i = 0; i < strArr.length; i++) {
            hashMap.put(strArr[i], Integer.valueOf(iArr[i]));
        }
        return ((Integer) hashMap.get("android.permission.ACCESS_COARSE_LOCATION")).intValue() == 0 && ((Integer) hashMap.get("android.permission.ACCESS_FINE_LOCATION")).intValue() == 0;
    }

    public static boolean requestCameraRuntimePermissions(Activity activity) {
        if (VERSION.SDK_INT < 23 || !Device.isPermissionFixe()) {
            return true;
        }
        List needCheckPermissionList = getNeedCheckPermissionList(sRuntimePermissions);
        if (needCheckPermissionList.size() > 0) {
            Log.m4i("PermissionManager", "requestCameraRuntimePermissions(), user check");
            ActivityCompat.requestPermissions(activity, (String[]) needCheckPermissionList.toArray(new String[needCheckPermissionList.size()]), 100);
            return false;
        }
        Log.m4i("PermissionManager", "requestCameraRuntimePermissions(), all on");
        return true;
    }
}
