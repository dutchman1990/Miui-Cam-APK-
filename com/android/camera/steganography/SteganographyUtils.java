package com.android.camera.steganography;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

public class SteganographyUtils {
    private static String TAG = "Whet_SteganographyUtils";

    public static Bitmap encodeWatermark(Bitmap bitmap, String str) {
        Bitmap bitmap2 = null;
        if (bitmap == null || TextUtils.isEmpty(str)) {
            return bitmap2;
        }
        try {
            bitmap2 = Steg.withInput(bitmap).encode(str).intoBitmap();
        } catch (Exception e) {
            Log.w(TAG, "encodeWatermark Exception e:" + e.getMessage());
        }
        return bitmap2;
    }
}
