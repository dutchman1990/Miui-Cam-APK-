package com.google.zxing.qrcode.decoder;

import com.google.zxing.qrcode.decoder.Version.ECB;
import com.google.zxing.qrcode.decoder.Version.ECBlocks;

final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int i, byte[] bArr) {
        this.numDataCodewords = i;
        this.codewords = bArr;
    }

    static DataBlock[] getDataBlocks(byte[] bArr, Version version, ErrorCorrectionLevel errorCorrectionLevel) {
        if (bArr.length == version.getTotalCodewords()) {
            ECB ecb;
            int i;
            int i2;
            int i3;
            ECBlocks eCBlocksForLevel = version.getECBlocksForLevel(errorCorrectionLevel);
            int i4 = 0;
            ECB[] eCBlocks = eCBlocksForLevel.getECBlocks();
            for (ECB ecb2 : eCBlocks) {
                i4 += ecb2.getCount();
            }
            DataBlock[] dataBlockArr = new DataBlock[i4];
            int i5 = 0;
            int length = eCBlocks.length;
            int i6 = 0;
            while (i6 < length) {
                ecb2 = eCBlocks[i6];
                i = 0;
                int i7 = i5;
                while (i < ecb2.getCount()) {
                    int dataCodewords = ecb2.getDataCodewords();
                    i5 = i7 + 1;
                    dataBlockArr[i7] = new DataBlock(dataCodewords, new byte[(eCBlocksForLevel.getECCodewordsPerBlock() + dataCodewords)]);
                    i++;
                    i7 = i5;
                }
                i6++;
                i5 = i7;
            }
            int length2 = dataBlockArr[0].codewords.length;
            int length3 = dataBlockArr.length - 1;
            while (length3 >= 0 && dataBlockArr[length3].codewords.length != length2) {
                length3--;
            }
            length3++;
            int eCCodewordsPerBlock = length2 - eCBlocksForLevel.getECCodewordsPerBlock();
            int i8 = 0;
            i = 0;
            while (i < eCCodewordsPerBlock) {
                i2 = 0;
                i3 = i8;
                while (i2 < i5) {
                    i8 = i3 + 1;
                    dataBlockArr[i2].codewords[i] = (byte) bArr[i3];
                    i2++;
                    i3 = i8;
                }
                i++;
                i8 = i3;
            }
            i2 = length3;
            i3 = i8;
            while (i2 < i5) {
                i8 = i3 + 1;
                dataBlockArr[i2].codewords[eCCodewordsPerBlock] = (byte) bArr[i3];
                i2++;
                i3 = i8;
            }
            int length4 = dataBlockArr[0].codewords.length;
            i = eCCodewordsPerBlock;
            i8 = i3;
            while (i < length4) {
                i2 = 0;
                i3 = i8;
                while (i2 < i5) {
                    i8 = i3 + 1;
                    dataBlockArr[i2].codewords[i2 >= length3 ? i + 1 : i] = (byte) bArr[i3];
                    i2++;
                    i3 = i8;
                }
                i++;
                i8 = i3;
            }
            return dataBlockArr;
        }
        throw new IllegalArgumentException();
    }

    byte[] getCodewords() {
        return this.codewords;
    }

    int getNumDataCodewords() {
        return this.numDataCodewords;
    }
}
