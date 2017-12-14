package com.android.camera;

import android.hardware.Camera.Size;
import android.support.v7.recyclerview.C0049R;
import com.android.camera.preferences.CameraSettingPreferences;
import java.util.ArrayList;
import java.util.List;

public class PictureSizeManager {
    private static String sDefaultValue = "4x3";
    private static final ArrayList<String> sEntryValues = new ArrayList();
    private static final ArrayList<PictureSize> sPictureList = new ArrayList();

    static {
        sEntryValues.add("4x3");
        sEntryValues.add("16x9");
    }

    private static PictureSize _findMaxRatio16_9(List<PictureSize> list) {
        int i = 0;
        int i2 = 0;
        for (PictureSize pictureSize : list) {
            if (CameraSettings.isAspectRatio16_9(pictureSize.width, pictureSize.height) && pictureSize.width * pictureSize.height > i * i2) {
                i = pictureSize.width;
                i2 = pictureSize.height;
            }
        }
        return i != 0 ? new PictureSize(i, i2) : new PictureSize();
    }

    public static PictureSize _findMaxRatio1_1(List<PictureSize> list) {
        int i = 0;
        int i2 = 0;
        for (PictureSize pictureSize : list) {
            if (CameraSettings.isAspectRatio1_1(pictureSize.width, pictureSize.height) && pictureSize.width * pictureSize.height > i * i2) {
                i = pictureSize.width;
                i2 = pictureSize.height;
            }
        }
        return i != 0 ? new PictureSize(i, i2) : new PictureSize();
    }

    public static PictureSize _findMaxRatio4_3(List<PictureSize> list) {
        int i = 0;
        int i2 = 0;
        for (PictureSize pictureSize : list) {
            if (CameraSettings.isAspectRatio4_3(pictureSize.width, pictureSize.height) && pictureSize.width * pictureSize.height > i * i2) {
                i = pictureSize.width;
                i2 = pictureSize.height;
            }
        }
        return i != 0 ? new PictureSize(i, i2) : new PictureSize();
    }

    public static PictureSize findMaxRatio16_9(List<Size> list) {
        int i = 0;
        int i2 = 0;
        for (Size size : list) {
            if (CameraSettings.isAspectRatio16_9(size.width, size.height) && size.width * size.height > i * i2) {
                i = size.width;
                i2 = size.height;
            }
        }
        return i != 0 ? new PictureSize(i, i2) : new PictureSize();
    }

    private static PictureSize findMaxRatio1_1(List<Size> list) {
        int i = 0;
        int i2 = 0;
        for (Size size : list) {
            if (CameraSettings.isAspectRatio1_1(size.width, size.height) && size.width * size.height > i * i2) {
                i = size.width;
                i2 = size.height;
            }
        }
        return i != 0 ? new PictureSize(i, i2) : new PictureSize();
    }

    private static PictureSize findMaxRatio4_3(List<Size> list) {
        int i = 0;
        int i2 = 0;
        for (Size size : list) {
            if (CameraSettings.isAspectRatio4_3(size.width, size.height) && size.width * size.height > i * i2) {
                i = size.width;
                i2 = size.height;
            }
        }
        return i != 0 ? new PictureSize(i, i2) : new PictureSize();
    }

    public static PictureSize getBestPanoPictureSize() {
        PictureSize _findMaxRatio4_3 = CameraSettings.isAspectRatio4_3(Util.sWindowWidth, Util.sWindowHeight) ? _findMaxRatio4_3(sPictureList) : _findMaxRatio16_9(sPictureList);
        return (_findMaxRatio4_3 == null || _findMaxRatio4_3.isEmpty()) ? new PictureSize(((PictureSize) sPictureList.get(0)).width, ((PictureSize) sPictureList.get(0)).height) : _findMaxRatio4_3;
    }

    public static PictureSize getBestPictureSize() {
        PictureSize pictureSize = getPictureSize(false);
        PictureSize pictureSize2 = null;
        if (pictureSize.isAspectRatio16_9()) {
            pictureSize2 = _findMaxRatio16_9(sPictureList);
        } else if (pictureSize.isAspectRatio4_3()) {
            pictureSize2 = _findMaxRatio4_3(sPictureList);
        } else if (pictureSize.isAspectRatio1_1()) {
            pictureSize2 = _findMaxRatio1_1(sPictureList);
        }
        return (pictureSize2 == null || pictureSize2.isEmpty()) ? new PictureSize(((PictureSize) sPictureList.get(0)).width, ((PictureSize) sPictureList.get(0)).height) : pictureSize2;
    }

    public static String getDefaultValue() {
        return sDefaultValue;
    }

    public static String[] getEntries() {
        return new String[]{CameraSettings.getString(C0049R.string.pref_camera_picturesize_entry_standard), CameraSettings.getString(C0049R.string.pref_camera_picturesize_entry_fullscreen)};
    }

    public static String[] getEntryValues() {
        String[] strArr = new String[sEntryValues.size()];
        sEntryValues.toArray(strArr);
        return strArr;
    }

    public static PictureSize getPictureSize(boolean z) {
        return (z || !CameraSettings.isSwitchOn("pref_camera_square_mode_key")) ? new PictureSize(CameraSettingPreferences.instance().getString("pref_camera_picturesize_key", sDefaultValue)) : new PictureSize(1, 1);
    }

    private static void initSensorRatio(List<Size> list) {
        if (Device.IS_X9 || Device.IS_A8) {
            sDefaultValue = "16x9";
            return;
        }
        int i = -1;
        int i2 = 0;
        PictureSize pictureSize = new PictureSize();
        for (int i3 = 0; i3 < list.size(); i3++) {
            pictureSize.setPictureSize((Size) list.get(i3));
            if (i2 < pictureSize.area()) {
                i = i3;
                i2 = pictureSize.area();
            }
        }
        pictureSize.setPictureSize((Size) list.get(i));
        if (pictureSize.isAspectRatio4_3()) {
            sDefaultValue = "4x3";
        } else {
            sDefaultValue = "16x9";
        }
    }

    public static void initialize(ActivityBase activityBase, List<Size> list, int i) {
        sPictureList.clear();
        if (list == null || list.size() == 0) {
            throw new IllegalArgumentException("The supported picture size list return from hal is null!");
        }
        List list2;
        initSensorRatio(list);
        if (i != 0) {
            ArrayList arrayList = new ArrayList();
            for (Size size : list) {
                if (size.width * size.height <= i) {
                    arrayList.add(size);
                }
            }
            list2 = arrayList;
        }
        PictureSize findMaxRatio4_3 = findMaxRatio4_3(list2);
        if (findMaxRatio4_3 != null) {
            sPictureList.add(findMaxRatio4_3);
        }
        PictureSize findMaxRatio1_1 = findMaxRatio1_1(list2);
        if (findMaxRatio1_1 != null) {
            sPictureList.add(findMaxRatio1_1);
        }
        PictureSize findMaxRatio16_9 = findMaxRatio16_9(list2);
        if (findMaxRatio16_9 != null) {
            sPictureList.add(findMaxRatio16_9);
        }
        if (sPictureList.size() == 0) {
            throw new IllegalArgumentException("Not find the desire picture sizes!");
        }
    }
}
