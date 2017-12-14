package com.google.zxing.qrcode.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;

final class BitMatrixParser {
    private final BitMatrix bitMatrix;
    private boolean mirror;
    private FormatInformation parsedFormatInfo;
    private Version parsedVersion;

    BitMatrixParser(BitMatrix bitMatrix) throws FormatException {
        int height = bitMatrix.getHeight();
        if (height >= 21 && (height & 3) == 1) {
            this.bitMatrix = bitMatrix;
            return;
        }
        throw FormatException.getFormatInstance();
    }

    private int copyBit(int i, int i2, int i3) {
        return !(!this.mirror ? this.bitMatrix.get(i, i2) : this.bitMatrix.get(i2, i)) ? i3 << 1 : (i3 << 1) | 1;
    }

    void mirror() {
        for (int i = 0; i < this.bitMatrix.getWidth(); i++) {
            for (int i2 = i + 1; i2 < this.bitMatrix.getHeight(); i2++) {
                if (this.bitMatrix.get(i, i2) != this.bitMatrix.get(i2, i)) {
                    this.bitMatrix.flip(i2, i);
                    this.bitMatrix.flip(i, i2);
                }
            }
        }
    }

    byte[] readCodewords() throws FormatException {
        FormatInformation readFormatInformation = readFormatInformation();
        Version readVersion = readVersion();
        DataMask forReference = DataMask.forReference(readFormatInformation.getDataMask());
        int height = this.bitMatrix.getHeight();
        forReference.unmaskBitMatrix(this.bitMatrix, height);
        BitMatrix buildFunctionPattern = readVersion.buildFunctionPattern();
        int i = 1;
        byte[] bArr = new byte[readVersion.getTotalCodewords()];
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = height - 1;
        while (i5 > 0) {
            if (i5 == 6) {
                i5--;
            }
            int i6 = 0;
            while (i6 < height) {
                int i7 = i == 0 ? i6 : (height - 1) - i6;
                int i8 = 0;
                int i9 = i2;
                while (i8 < 2) {
                    if (buildFunctionPattern.get(i5 - i8, i7)) {
                        i2 = i9;
                    } else {
                        i4++;
                        i3 <<= 1;
                        if (this.bitMatrix.get(i5 - i8, i7)) {
                            i3 |= 1;
                        }
                        if (i4 != 8) {
                            i2 = i9;
                        } else {
                            i2 = i9 + 1;
                            bArr[i9] = (byte) ((byte) i3);
                            i4 = 0;
                            i3 = 0;
                        }
                    }
                    i8++;
                    i9 = i2;
                }
                i6++;
                i2 = i9;
            }
            i ^= 1;
            i5 -= 2;
        }
        if (i2 == readVersion.getTotalCodewords()) {
            return bArr;
        }
        throw FormatException.getFormatInstance();
    }

    FormatInformation readFormatInformation() throws FormatException {
        if (this.parsedFormatInfo != null) {
            return this.parsedFormatInfo;
        }
        int i;
        int i2;
        int i3 = 0;
        for (i = 0; i < 6; i++) {
            i3 = copyBit(i, 8, i3);
        }
        i3 = copyBit(8, 7, copyBit(8, 8, copyBit(7, 8, i3)));
        for (i2 = 5; i2 >= 0; i2--) {
            i3 = copyBit(8, i2, i3);
        }
        int height = this.bitMatrix.getHeight();
        int i4 = 0;
        int i5 = height - 7;
        for (i2 = height - 1; i2 >= i5; i2--) {
            i4 = copyBit(8, i2, i4);
        }
        for (i = height - 8; i < height; i++) {
            i4 = copyBit(i, 8, i4);
        }
        this.parsedFormatInfo = FormatInformation.decodeFormatInformation(i3, i4);
        if (this.parsedFormatInfo != null) {
            return this.parsedFormatInfo;
        }
        throw FormatException.getFormatInstance();
    }

    Version readVersion() throws FormatException {
        if (this.parsedVersion != null) {
            return this.parsedVersion;
        }
        int height = this.bitMatrix.getHeight();
        int i = (height - 17) / 4;
        if (i <= 6) {
            return Version.getVersionForNumber(i);
        }
        int i2;
        int i3 = 0;
        int i4 = height - 11;
        for (i2 = 5; i2 >= 0; i2--) {
            int i5;
            for (i5 = height - 9; i5 >= i4; i5--) {
                i3 = copyBit(i5, i2, i3);
            }
        }
        Version decodeVersionInformation = Version.decodeVersionInformation(i3);
        if (decodeVersionInformation != null && decodeVersionInformation.getDimensionForVersion() == height) {
            this.parsedVersion = decodeVersionInformation;
            return decodeVersionInformation;
        }
        i3 = 0;
        for (i5 = 5; i5 >= 0; i5--) {
            for (i2 = height - 9; i2 >= i4; i2--) {
                i3 = copyBit(i5, i2, i3);
            }
        }
        decodeVersionInformation = Version.decodeVersionInformation(i3);
        if (decodeVersionInformation != null && decodeVersionInformation.getDimensionForVersion() == height) {
            this.parsedVersion = decodeVersionInformation;
            return decodeVersionInformation;
        }
        throw FormatException.getFormatInstance();
    }

    void remask() {
        if (this.parsedFormatInfo != null) {
            DataMask.forReference(this.parsedFormatInfo.getDataMask()).unmaskBitMatrix(this.bitMatrix, this.bitMatrix.getHeight());
        }
    }

    void setMirror(boolean z) {
        this.parsedVersion = null;
        this.parsedFormatInfo = null;
        this.mirror = z;
    }
}
