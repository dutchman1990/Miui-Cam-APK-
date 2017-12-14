package com.android.camera.steganography;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BitmapEncoder {
    public static byte[] createHeader(long j) {
        int i;
        int i2 = 0;
        byte[] bArr = new byte[12];
        bArr[0] = (byte) 91;
        int i3 = 1 + 1;
        bArr[1] = (byte) 91;
        byte[] longToBytes = longToBytes(j);
        int length = longToBytes.length;
        while (i2 < length) {
            i = i3 + 1;
            bArr[i3] = longToBytes[i2];
            i2++;
            i3 = i;
        }
        i = i3 + 1;
        bArr[i3] = (byte) 93;
        i3 = i + 1;
        bArr[i] = (byte) 93;
        return bArr;
    }

    public static Bitmap encode(Bitmap bitmap, byte[] bArr) {
        byte[] createHeader = createHeader((long) bArr.length);
        if (bArr.length % 24 != 0) {
            bArr = Arrays.copyOf(bArr, bArr.length + (24 - (bArr.length % 24)));
        }
        return encodeByteArrayIntoBitmap(bitmap, createHeader, bArr);
    }

    private static Bitmap encodeByteArrayIntoBitmap(Bitmap bitmap, byte[] bArr, byte[] bArr2) {
        Bitmap copy = bitmap.copy(Config.ARGB_8888, true);
        int i = 0;
        int i2 = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int i3 = 0;
        int[] iArr = new int[]{0, 0, 0};
        for (int i4 = 0; i4 < bArr.length + bArr2.length; i4++) {
            for (int i5 = 0; i5 < 8; i5++) {
                if (i4 < bArr.length) {
                    iArr[i3] = (bArr[i4] >> i5) & 1;
                } else {
                    iArr[i3] = (bArr2[i4 - bArr.length] >> i5) & 1;
                }
                if (i3 == 2) {
                    int pixel = bitmap.getPixel(i, i2);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);
                    if (red % 2 == 1 - iArr[0]) {
                        red++;
                    }
                    if (green % 2 == 1 - iArr[1]) {
                        green++;
                    }
                    if (blue % 2 == 1 - iArr[2]) {
                        blue++;
                    }
                    if (red == 256) {
                        red = 254;
                    }
                    if (green == 256) {
                        green = 254;
                    }
                    if (blue == 256) {
                        blue = 254;
                    }
                    copy.setPixel(i, i2, Color.argb(255, red, green, blue));
                    i++;
                    if (i == width) {
                        i = 0;
                        i2++;
                    }
                    i3 = 0;
                } else {
                    i3++;
                }
            }
        }
        return copy;
    }

    private static byte[] longToBytes(long j) {
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.putLong(j);
        return allocate.array();
    }
}
