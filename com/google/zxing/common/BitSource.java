package com.google.zxing.common;

public final class BitSource {
    private int bitOffset;
    private int byteOffset;
    private final byte[] bytes;

    public BitSource(byte[] bArr) {
        this.bytes = bArr;
    }

    public int available() {
        return ((this.bytes.length - this.byteOffset) * 8) - this.bitOffset;
    }

    public int getBitOffset() {
        return this.bitOffset;
    }

    public int getByteOffset() {
        return this.byteOffset;
    }

    public int readBits(int i) {
        if (i >= 1 && i <= 32 && i <= available()) {
            int i2;
            int i3 = 0;
            if (this.bitOffset > 0) {
                int i4 = 8 - this.bitOffset;
                int i5 = i >= i4 ? i4 : i;
                i2 = i4 - i5;
                i3 = (this.bytes[this.byteOffset] & ((255 >> (8 - i5)) << i2)) >> i2;
                i -= i5;
                this.bitOffset += i5;
                if (this.bitOffset == 8) {
                    this.bitOffset = 0;
                    this.byteOffset++;
                }
            }
            if (i <= 0) {
                return i3;
            }
            while (i >= 8) {
                i3 = (i3 << 8) | (this.bytes[this.byteOffset] & 255);
                this.byteOffset++;
                i -= 8;
            }
            if (i <= 0) {
                return i3;
            }
            i2 = 8 - i;
            i3 = (i3 << i) | ((this.bytes[this.byteOffset] & ((255 >> i2) << i2)) >> i2);
            this.bitOffset += i;
            return i3;
        }
        throw new IllegalArgumentException(String.valueOf(i));
    }
}
