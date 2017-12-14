package com.adobe.xmp.impl;

import java.io.UnsupportedEncodingException;

public class Latin1Converter {
    private Latin1Converter() {
    }

    public static ByteBuffer convert(ByteBuffer byteBuffer) {
        if (!"UTF-8".equals(byteBuffer.getEncoding())) {
            return byteBuffer;
        }
        byte[] bArr = new byte[8];
        int i = 0;
        int i2 = 0;
        ByteBuffer byteBuffer2 = new ByteBuffer((byteBuffer.length() * 4) / 3);
        int i3 = 0;
        int i4 = 0;
        while (i4 < byteBuffer.length()) {
            int charAt = byteBuffer.charAt(i4);
            int i5;
            switch (i3) {
                case 11:
                    if (i2 > 0 && (charAt & 192) == 128) {
                        i5 = i + 1;
                        bArr[i] = (byte) charAt;
                        i2--;
                        if (i2 != 0) {
                            i = i5;
                            break;
                        }
                        byteBuffer2.append(bArr, 0, i5);
                        i = 0;
                        i3 = 0;
                        break;
                    }
                    byteBuffer2.append(convertToUTF8(bArr[0]));
                    i4 -= i;
                    i = 0;
                    i3 = 0;
                    break;
                    break;
                default:
                    if (charAt >= 127) {
                        if (charAt < 192) {
                            byteBuffer2.append(convertToUTF8((byte) charAt));
                            break;
                        }
                        i2 = -1;
                        int i6 = charAt;
                        while (i2 < 8 && (i6 & 128) == 128) {
                            i2++;
                            i6 <<= 1;
                        }
                        i5 = i + 1;
                        bArr[i] = (byte) charAt;
                        i3 = 11;
                        i = i5;
                        break;
                    }
                    byteBuffer2.append((byte) charAt);
                    break;
                    break;
            }
            i4++;
        }
        if (i3 == 11) {
            for (int i7 = 0; i7 < i; i7++) {
                byteBuffer2.append(convertToUTF8(bArr[i7]));
            }
        }
        return byteBuffer2;
    }

    private static byte[] convertToUTF8(byte b) {
        int i = b & 255;
        if (i >= 128) {
            if (i == 129 || i == 141 || i == 143 || i == 144 || i == 157) {
                try {
                    return new byte[]{(byte) 32};
                } catch (UnsupportedEncodingException e) {
                }
            } else {
                return new String(new byte[]{b}, "cp1252").getBytes("UTF-8");
            }
        }
        return new byte[]{b};
    }
}
