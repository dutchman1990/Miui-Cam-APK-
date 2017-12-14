package com.android.camera;

import android.support.v7.recyclerview.C0049R;
import java.util.HashMap;

public class JpegEncodingQualityMappings {
    private static HashMap<String, Integer> mHashMap = new HashMap();

    static {
        mHashMap.put("low", Integer.valueOf(67));
        mHashMap.put("normal", Integer.valueOf(87));
        mHashMap.put("high", Integer.valueOf(CameraAppImpl.getAndroidContext().getResources().getInteger(C0049R.integer.high_jpeg_quality)));
    }

    public static int getQualityNumber(String str) {
        Integer num = (Integer) mHashMap.get(str);
        return num == null ? 87 : num.intValue();
    }
}
