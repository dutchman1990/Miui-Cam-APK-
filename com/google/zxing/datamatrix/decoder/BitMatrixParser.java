package com.google.zxing.datamatrix.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;

final class BitMatrixParser {
    private final BitMatrix mappingBitMatrix;
    private final BitMatrix readMappingMatrix;
    private final Version version;

    BitMatrixParser(BitMatrix bitMatrix) throws FormatException {
        int height = bitMatrix.getHeight();
        if (height >= 8 && height <= 144 && (height & 1) == 0) {
            this.version = readVersion(bitMatrix);
            this.mappingBitMatrix = extractDataRegion(bitMatrix);
            this.readMappingMatrix = new BitMatrix(this.mappingBitMatrix.getWidth(), this.mappingBitMatrix.getHeight());
            return;
        }
        throw FormatException.getFormatInstance();
    }

    private static Version readVersion(BitMatrix bitMatrix) throws FormatException {
        return Version.getVersionForDimensions(bitMatrix.getHeight(), bitMatrix.getWidth());
    }

    BitMatrix extractDataRegion(BitMatrix bitMatrix) {
        int symbolSizeRows = this.version.getSymbolSizeRows();
        int symbolSizeColumns = this.version.getSymbolSizeColumns();
        if (bitMatrix.getHeight() == symbolSizeRows) {
            int dataRegionSizeRows = this.version.getDataRegionSizeRows();
            int dataRegionSizeColumns = this.version.getDataRegionSizeColumns();
            int i = symbolSizeRows / dataRegionSizeRows;
            int i2 = symbolSizeColumns / dataRegionSizeColumns;
            BitMatrix bitMatrix2 = new BitMatrix(i2 * dataRegionSizeColumns, i * dataRegionSizeRows);
            for (int i3 = 0; i3 < i; i3++) {
                int i4 = i3 * dataRegionSizeRows;
                for (int i5 = 0; i5 < i2; i5++) {
                    int i6 = i5 * dataRegionSizeColumns;
                    for (int i7 = 0; i7 < dataRegionSizeRows; i7++) {
                        int i8 = (((dataRegionSizeRows + 2) * i3) + 1) + i7;
                        int i9 = i4 + i7;
                        for (int i10 = 0; i10 < dataRegionSizeColumns; i10++) {
                            if (bitMatrix.get((((dataRegionSizeColumns + 2) * i5) + 1) + i10, i8)) {
                                bitMatrix2.set(i6 + i10, i9);
                            }
                        }
                    }
                }
            }
            return bitMatrix2;
        }
        throw new IllegalArgumentException("Dimension of bitMarix must match the version size");
    }

    Version getVersion() {
        return this.version;
    }

    byte[] readCodewords() throws FormatException {
        byte[] bArr = new byte[this.version.getTotalCodewords()];
        int i = 4;
        int i2 = 0;
        int height = this.mappingBitMatrix.getHeight();
        int width = this.mappingBitMatrix.getWidth();
        Object obj = null;
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        int i3 = 0;
        while (true) {
            int i4;
            if (i == height && i2 == 0 && r1 == null) {
                i4 = i3 + 1;
                bArr[i3] = (byte) ((byte) readCorner1(height, width));
                i -= 2;
                i2 += 2;
                obj = 1;
            } else if (i == height - 2 && i2 == 0 && (width & 3) != 0 && r2 == null) {
                i4 = i3 + 1;
                bArr[i3] = (byte) ((byte) readCorner2(height, width));
                i -= 2;
                i2 += 2;
                obj2 = 1;
            } else if (i == height + 4 && i2 == 2 && (width & 7) == 0 && r3 == null) {
                i4 = i3 + 1;
                bArr[i3] = (byte) ((byte) readCorner3(height, width));
                i -= 2;
                i2 += 2;
                obj3 = 1;
            } else if (i == height - 2 && i2 == 0 && (width & 7) == 4 && r4 == null) {
                i4 = i3 + 1;
                bArr[i3] = (byte) ((byte) readCorner4(height, width));
                i -= 2;
                i2 += 2;
                obj4 = 1;
            } else {
                while (true) {
                    if (i < height && i2 >= 0 && !this.readMappingMatrix.get(i2, i)) {
                        i4 = i3 + 1;
                        bArr[i3] = (byte) ((byte) readUtah(i, i2, height, width));
                    } else {
                        i4 = i3;
                    }
                    i -= 2;
                    i2 += 2;
                    if (i >= 0 && i2 < width) {
                        i3 = i4;
                    }
                }
                i++;
                i2 += 3;
                i3 = i4;
                while (true) {
                    if (i >= 0 && i2 < width && !this.readMappingMatrix.get(i2, i)) {
                        i4 = i3 + 1;
                        bArr[i3] = (byte) ((byte) readUtah(i, i2, height, width));
                    } else {
                        i4 = i3;
                    }
                    i += 2;
                    i2 -= 2;
                    if (i < height && i2 >= 0) {
                        i3 = i4;
                    }
                }
                i += 3;
                i2++;
            }
            if (i >= height) {
                if (i2 >= width) {
                    break;
                }
                i3 = i4;
            } else {
                i3 = i4;
            }
        }
        if (i4 == this.version.getTotalCodewords()) {
            return bArr;
        }
        throw FormatException.getFormatInstance();
    }

    int readCorner1(int i, int i2) {
        int i3 = 0;
        if (readModule(i - 1, 0, i, i2)) {
            i3 = 1;
        }
        i3 <<= 1;
        if (readModule(i - 1, 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(i - 1, 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(2, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        return !readModule(3, i2 + -1, i, i2) ? i3 : i3 | 1;
    }

    int readCorner2(int i, int i2) {
        int i3 = 0;
        if (readModule(i - 3, 0, i, i2)) {
            i3 = 1;
        }
        i3 <<= 1;
        if (readModule(i - 2, 0, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(i - 1, 0, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 4, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 3, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        return !readModule(1, i2 + -1, i, i2) ? i3 : i3 | 1;
    }

    int readCorner3(int i, int i2) {
        int i3 = 0;
        if (readModule(i - 1, 0, i, i2)) {
            i3 = 1;
        }
        i3 <<= 1;
        if (readModule(i - 1, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 3, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, i2 - 3, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, i2 - 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        return !readModule(1, i2 + -1, i, i2) ? i3 : i3 | 1;
    }

    int readCorner4(int i, int i2) {
        int i3 = 0;
        if (readModule(i - 3, 0, i, i2)) {
            i3 = 1;
        }
        i3 <<= 1;
        if (readModule(i - 2, 0, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(i - 1, 0, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(2, i2 - 1, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        return !readModule(3, i2 + -1, i, i2) ? i3 : i3 | 1;
    }

    boolean readModule(int i, int i2, int i3, int i4) {
        if (i < 0) {
            i += i3;
            i2 += 4 - ((i3 + 4) & 7);
        }
        if (i2 < 0) {
            i2 += i4;
            i += 4 - ((i4 + 4) & 7);
        }
        this.readMappingMatrix.set(i2, i);
        return this.mappingBitMatrix.get(i2, i);
    }

    int readUtah(int i, int i2, int i3, int i4) {
        int i5 = 0;
        if (readModule(i - 2, i2 - 2, i3, i4)) {
            i5 = 1;
        }
        i5 <<= 1;
        if (readModule(i - 2, i2 - 1, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i - 1, i2 - 2, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i - 1, i2 - 1, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i - 1, i2, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i, i2 - 2, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i, i2 - 1, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        return !readModule(i, i2, i3, i4) ? i5 : i5 | 1;
    }
}
