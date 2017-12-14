package com.google.zxing.datamatrix.decoder;

final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int i, byte[] bArr) {
        this.numDataCodewords = i;
        this.codewords = bArr;
    }

    static DataBlock[] getDataBlocks(byte[] bArr, Version version) {
        int i;
        int i2;
        int i3;
        ECBlocks eCBlocks = version.getECBlocks();
        int i4 = 0;
        ECB[] eCBlocks2 = eCBlocks.getECBlocks();
        for (ECB count : eCBlocks2) {
            ECB count2;
            i4 += count2.getCount();
        }
        DataBlock[] dataBlockArr = new DataBlock[i4];
        int i5 = 0;
        int length = eCBlocks2.length;
        int i6 = 0;
        while (i6 < length) {
            count2 = eCBlocks2[i6];
            i = 0;
            int i7 = i5;
            while (i < count2.getCount()) {
                int dataCodewords = count2.getDataCodewords();
                i5 = i7 + 1;
                dataBlockArr[i7] = new DataBlock(dataCodewords, new byte[(eCBlocks.getECCodewords() + dataCodewords)]);
                i++;
                i7 = i5;
            }
            i6++;
            i5 = i7;
        }
        int length2 = dataBlockArr[0].codewords.length - eCBlocks.getECCodewords();
        int i8 = length2 - 1;
        int i9 = 0;
        i = 0;
        while (i < i8) {
            i2 = 0;
            i3 = i9;
            while (i2 < i5) {
                i9 = i3 + 1;
                dataBlockArr[i2].codewords[i] = (byte) bArr[i3];
                i2++;
                i3 = i9;
            }
            i++;
            i9 = i3;
        }
        Object obj = version.getVersionNumber() != 24 ? null : 1;
        int i10 = obj == null ? i5 : 8;
        i2 = 0;
        i3 = i9;
        while (i2 < i10) {
            i9 = i3 + 1;
            dataBlockArr[i2].codewords[length2 - 1] = (byte) bArr[i3];
            i2++;
            i3 = i9;
        }
        int length3 = dataBlockArr[0].codewords.length;
        i = length2;
        i9 = i3;
        while (i < length3) {
            i2 = 0;
            i3 = i9;
            while (i2 < i5) {
                int i11 = (obj != null && i2 > 7) ? i - 1 : i;
                i9 = i3 + 1;
                dataBlockArr[i2].codewords[i11] = (byte) bArr[i3];
                i2++;
                i3 = i9;
            }
            i++;
            i9 = i3;
        }
        if (i9 == bArr.length) {
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
