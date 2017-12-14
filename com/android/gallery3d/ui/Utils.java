package com.android.gallery3d.ui;

import android.os.Build;

public class Utils {
    private static final boolean IS_DEBUG_BUILD = (!Build.TYPE.equals("eng") ? Build.TYPE.equals("userdebug") : true);
    private static long[] sCrcTable = new long[256];

    static {
        for (int i = 0; i < 256; i++) {
            long j = (long) i;
            for (int i2 = 0; i2 < 8; i2++) {
                j = (j >> 1) ^ ((((int) j) & 1) != 0 ? -7661587058870466123L : 0);
            }
            sCrcTable[i] = j;
        }
    }

    public static void assertTrue(boolean z) {
        if (!z) {
            throw new AssertionError();
        }
    }

    public static int nextPowerOf2(int i) {
        if (i <= 0 || i > 1073741824) {
            throw new IllegalArgumentException();
        }
        i--;
        i |= i >> 16;
        i |= i >> 8;
        i |= i >> 4;
        i |= i >> 2;
        return (i | (i >> 1)) + 1;
    }
}
