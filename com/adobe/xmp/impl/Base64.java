package com.adobe.xmp.impl;

public class Base64 {
    private static byte[] ascii = new byte[255];
    private static byte[] base64 = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 43, (byte) 47};

    static {
        int i;
        for (i = 0; i < 255; i++) {
            ascii[i] = (byte) -1;
        }
        for (i = 0; i < base64.length; i++) {
            ascii[base64[i]] = (byte) i;
        }
        ascii[9] = (byte) -2;
        ascii[10] = (byte) -2;
        ascii[13] = (byte) -2;
        ascii[32] = (byte) -2;
        ascii[61] = (byte) -3;
    }

    public static final byte[] encode(byte[] bArr) {
        return encode(bArr, 0);
    }

    public static final byte[] encode(byte[] bArr, int i) {
        i = (i / 4) * 4;
        if (i < 0) {
            i = 0;
        }
        int length = ((bArr.length + 2) / 3) * 4;
        if (i > 0) {
            length += (length - 1) / i;
        }
        byte[] bArr2 = new byte[length];
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i3 + 3 <= bArr.length) {
            int i5 = i3 + 1;
            i3 = i5 + 1;
            i5 = i3 + 1;
            int i6 = (((bArr[i3] & 255) << 16) | ((bArr[i5] & 255) << 8)) | ((bArr[i3] & 255) << 0);
            int i7 = i2 + 1;
            bArr2[i2] = base64[(i6 & 16515072) >> 18];
            i2 = i7 + 1;
            bArr2[i7] = base64[(i6 & 258048) >> 12];
            i7 = i2 + 1;
            bArr2[i2] = base64[(i6 & 4032) >> 6];
            i2 = i7 + 1;
            bArr2[i7] = base64[i6 & 63];
            i4 += 4;
            if (i2 < length && i > 0 && i4 % i == 0) {
                i7 = i2 + 1;
                bArr2[i2] = (byte) 10;
                i2 = i7;
            }
            i3 = i5;
        }
        if (bArr.length - i3 == 2) {
            i6 = ((bArr[i3] & 255) << 16) | ((bArr[i3 + 1] & 255) << 8);
            i7 = i2 + 1;
            bArr2[i2] = base64[(i6 & 16515072) >> 18];
            i2 = i7 + 1;
            bArr2[i7] = base64[(i6 & 258048) >> 12];
            i7 = i2 + 1;
            bArr2[i2] = base64[(i6 & 4032) >> 6];
            i2 = i7 + 1;
            bArr2[i7] = (byte) 61;
        } else if (bArr.length - i3 == 1) {
            i6 = (bArr[i3] & 255) << 16;
            i7 = i2 + 1;
            bArr2[i2] = base64[(i6 & 16515072) >> 18];
            i2 = i7 + 1;
            bArr2[i7] = base64[(i6 & 258048) >> 12];
            i7 = i2 + 1;
            bArr2[i2] = (byte) 61;
            i2 = i7 + 1;
            bArr2[i7] = (byte) 61;
        }
        return bArr2;
    }
}
