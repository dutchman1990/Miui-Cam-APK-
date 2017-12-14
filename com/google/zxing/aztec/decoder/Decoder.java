package com.google.zxing.aztec.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.aztec.AztecDetectorResult;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import java.util.Arrays;

public final class Decoder {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$google$zxing$aztec$decoder$Decoder$Table;
    private static final String[] DIGIT_TABLE = new String[]{"CTRL_PS", " ", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ",", ".", "CTRL_UL", "CTRL_US"};
    private static final String[] LOWER_TABLE = new String[]{"CTRL_PS", " ", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "CTRL_US", "CTRL_ML", "CTRL_DL", "CTRL_BS"};
    private static final String[] MIXED_TABLE = new String[]{"CTRL_PS", " ", "\u0001", "\u0002", "\u0003", "\u0004", "\u0005", "\u0006", "\u0007", "\b", "\t", "\n", "\u000b", "\f", "\r", "\u001b", "\u001c", "\u001d", "\u001e", "\u001f", "@", "\\", "^", "_", "`", "|", "~", "", "CTRL_LL", "CTRL_UL", "CTRL_PL", "CTRL_BS"};
    private static final String[] PUNCT_TABLE = new String[]{"", "\r", "\r\n", ". ", ", ", ": ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", ":", ";", "<", "=", ">", "?", "[", "]", "{", "}", "CTRL_UL"};
    private static final String[] UPPER_TABLE = new String[]{"CTRL_PS", " ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CTRL_LL", "CTRL_ML", "CTRL_DL", "CTRL_BS"};
    private AztecDetectorResult ddata;

    private enum Table {
        UPPER,
        LOWER,
        MIXED,
        DIGIT,
        PUNCT,
        BINARY
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$google$zxing$aztec$decoder$Decoder$Table() {
        int[] iArr = $SWITCH_TABLE$com$google$zxing$aztec$decoder$Decoder$Table;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[Table.values().length];
        try {
            iArr[Table.BINARY.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Table.DIGIT.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Table.LOWER.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Table.MIXED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Table.PUNCT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Table.UPPER.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        $SWITCH_TABLE$com$google$zxing$aztec$decoder$Decoder$Table = iArr;
        return iArr;
    }

    private boolean[] correctBits(boolean[] zArr) throws FormatException {
        int i;
        GenericGF genericGF;
        if (this.ddata.getNbLayers() <= 2) {
            i = 6;
            genericGF = GenericGF.AZTEC_DATA_6;
        } else if (this.ddata.getNbLayers() <= 8) {
            i = 8;
            genericGF = GenericGF.AZTEC_DATA_8;
        } else if (this.ddata.getNbLayers() > 22) {
            i = 12;
            genericGF = GenericGF.AZTEC_DATA_12;
        } else {
            i = 10;
            genericGF = GenericGF.AZTEC_DATA_10;
        }
        int nbDatablocks = this.ddata.getNbDatablocks();
        int length = zArr.length / i;
        if (length >= nbDatablocks) {
            int length2 = zArr.length % i;
            int i2 = length - nbDatablocks;
            int[] iArr = new int[length];
            int i3 = 0;
            while (i3 < length) {
                iArr[i3] = readCode(zArr, length2, i);
                i3++;
                length2 += i;
            }
            try {
                int i4;
                new ReedSolomonDecoder(genericGF).decode(iArr, i2);
                int i5 = (1 << i) - 1;
                int i6 = 0;
                for (i3 = 0; i3 < nbDatablocks; i3++) {
                    i4 = iArr[i3];
                    if (i4 == 0 || i4 == i5) {
                        throw FormatException.getFormatInstance();
                    }
                    if (i4 == 1 || i4 == i5 - 1) {
                        i6++;
                    }
                }
                boolean[] zArr2 = new boolean[((nbDatablocks * i) - i6)];
                int i7 = 0;
                for (i3 = 0; i3 < nbDatablocks; i3++) {
                    i4 = iArr[i3];
                    if (i4 == 1 || i4 == i5 - 1) {
                        Arrays.fill(zArr2, i7, (i7 + i) - 1, i4 > 1);
                        i7 += i - 1;
                    } else {
                        int i8 = i - 1;
                        int i9 = i7;
                        while (i8 >= 0) {
                            i7 = i9 + 1;
                            zArr2[i9] = ((1 << i8) & i4) != 0;
                            i8--;
                            i9 = i7;
                        }
                        i7 = i9;
                    }
                }
                return zArr2;
            } catch (ReedSolomonException e) {
                throw FormatException.getFormatInstance();
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static String getCharacter(Table table, int i) {
        switch ($SWITCH_TABLE$com$google$zxing$aztec$decoder$Decoder$Table()[table.ordinal()]) {
            case 1:
                return UPPER_TABLE[i];
            case 2:
                return LOWER_TABLE[i];
            case 3:
                return MIXED_TABLE[i];
            case 4:
                return DIGIT_TABLE[i];
            case 5:
                return PUNCT_TABLE[i];
            default:
                throw new IllegalStateException("Bad table");
        }
    }

    private static String getEncodedData(boolean[] zArr) {
        int length = zArr.length;
        Table table = Table.UPPER;
        Table table2 = Table.UPPER;
        StringBuilder stringBuilder = new StringBuilder(20);
        int i = 0;
        while (i < length) {
            if (table2 == Table.BINARY) {
                if (length - i < 5) {
                    break;
                }
                int readCode = readCode(zArr, i, 5);
                i += 5;
                if (readCode == 0) {
                    if (length - i < 11) {
                        break;
                    }
                    readCode = readCode(zArr, i, 11) + 31;
                    i += 11;
                }
                for (int i2 = 0; i2 < readCode; i2++) {
                    if (length - i < 8) {
                        i = length;
                        break;
                    }
                    stringBuilder.append((char) readCode(zArr, i, 8));
                    i += 8;
                }
                table2 = table;
            } else {
                int i3 = table2 != Table.DIGIT ? 5 : 4;
                if (length - i < i3) {
                    break;
                }
                int readCode2 = readCode(zArr, i, i3);
                i += i3;
                String character = getCharacter(table2, readCode2);
                if (character.startsWith("CTRL_")) {
                    table2 = getTable(character.charAt(5));
                    if (character.charAt(6) == 'L') {
                        table = table2;
                    }
                } else {
                    stringBuilder.append(character);
                    table2 = table;
                }
            }
        }
        return stringBuilder.toString();
    }

    private static Table getTable(char c) {
        switch (c) {
            case 'B':
                return Table.BINARY;
            case 'D':
                return Table.DIGIT;
            case 'L':
                return Table.LOWER;
            case 'M':
                return Table.MIXED;
            case 'P':
                return Table.PUNCT;
            default:
                return Table.UPPER;
        }
    }

    private static int readCode(boolean[] zArr, int i, int i2) {
        int i3 = 0;
        for (int i4 = i; i4 < i + i2; i4++) {
            i3 <<= 1;
            if (zArr[i4]) {
                i3 |= 1;
            }
        }
        return i3;
    }

    private static int totalBitsInLayer(int i, boolean z) {
        return ((!z ? 112 : 88) + (i * 16)) * i;
    }

    public DecoderResult decode(AztecDetectorResult aztecDetectorResult) throws FormatException {
        this.ddata = aztecDetectorResult;
        return new DecoderResult(null, getEncodedData(correctBits(extractBits(aztecDetectorResult.getBits()))), null, null);
    }

    boolean[] extractBits(BitMatrix bitMatrix) {
        int i;
        boolean isCompact = this.ddata.isCompact();
        int nbLayers = this.ddata.getNbLayers();
        int i2 = !isCompact ? (nbLayers * 4) + 14 : (nbLayers * 4) + 11;
        int[] iArr = new int[i2];
        boolean[] zArr = new boolean[totalBitsInLayer(nbLayers, isCompact)];
        if (isCompact) {
            for (i = 0; i < iArr.length; i++) {
                iArr[i] = i;
            }
        } else {
            int i3 = i2 / 2;
            int i4 = ((i2 + 1) + ((((i2 / 2) - 1) / 15) * 2)) / 2;
            for (i = 0; i < i3; i++) {
                int i5 = i + (i / 15);
                iArr[(i3 - i) - 1] = (i4 - i5) - 1;
                iArr[i3 + i] = (i4 + i5) + 1;
            }
        }
        int i6 = 0;
        for (i = 0; i < nbLayers; i++) {
            int i7 = !isCompact ? ((nbLayers - i) * 4) + 12 : ((nbLayers - i) * 4) + 9;
            int i8 = i * 2;
            int i9 = (i2 - 1) - i8;
            for (int i10 = 0; i10 < i7; i10++) {
                int i11 = i10 * 2;
                for (int i12 = 0; i12 < 2; i12++) {
                    zArr[(i6 + i11) + i12] = bitMatrix.get(iArr[i8 + i12], iArr[i8 + i10]);
                    zArr[(((i7 * 2) + i6) + i11) + i12] = bitMatrix.get(iArr[i8 + i10], iArr[i9 - i12]);
                    zArr[(((i7 * 4) + i6) + i11) + i12] = bitMatrix.get(iArr[i9 - i12], iArr[i9 - i10]);
                    zArr[(((i7 * 6) + i6) + i11) + i12] = bitMatrix.get(iArr[i9 - i10], iArr[i8 + i12]);
                }
            }
            i6 += i7 * 8;
        }
        return zArr;
    }
}
