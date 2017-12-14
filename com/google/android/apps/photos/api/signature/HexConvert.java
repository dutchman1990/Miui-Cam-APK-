package com.google.android.apps.photos.api.signature;

final class HexConvert {
    private static final char[] HEX_DIGITS_ARRAY = "0123456789ABCDEF".toCharArray();

    private HexConvert() {
    }

    static String bytesToHex(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = HEX_DIGITS_ARRAY[i2 >>> 4];
            cArr[(i * 2) + 1] = HEX_DIGITS_ARRAY[i2 & 15];
        }
        return new String(cArr);
    }
}
