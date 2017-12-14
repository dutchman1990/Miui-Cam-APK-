package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Code128Reader extends OneDReader {
    static final int[][] CODE_PATTERNS;

    static {
        r0 = new int[107][];
        r0[0] = new int[]{2, 1, 2, 2, 2, 2};
        r0[1] = new int[]{2, 2, 2, 1, 2, 2};
        r0[2] = new int[]{2, 2, 2, 2, 2, 1};
        r0[3] = new int[]{1, 2, 1, 2, 2, 3};
        r0[4] = new int[]{1, 2, 1, 3, 2, 2};
        r0[5] = new int[]{1, 3, 1, 2, 2, 2};
        r0[6] = new int[]{1, 2, 2, 2, 1, 3};
        r0[7] = new int[]{1, 2, 2, 3, 1, 2};
        r0[8] = new int[]{1, 3, 2, 2, 1, 2};
        r0[9] = new int[]{2, 2, 1, 2, 1, 3};
        r0[10] = new int[]{2, 2, 1, 3, 1, 2};
        r0[11] = new int[]{2, 3, 1, 2, 1, 2};
        r0[12] = new int[]{1, 1, 2, 2, 3, 2};
        r0[13] = new int[]{1, 2, 2, 1, 3, 2};
        r0[14] = new int[]{1, 2, 2, 2, 3, 1};
        r0[15] = new int[]{1, 1, 3, 2, 2, 2};
        r0[16] = new int[]{1, 2, 3, 1, 2, 2};
        r0[17] = new int[]{1, 2, 3, 2, 2, 1};
        r0[18] = new int[]{2, 2, 3, 2, 1, 1};
        r0[19] = new int[]{2, 2, 1, 1, 3, 2};
        r0[20] = new int[]{2, 2, 1, 2, 3, 1};
        r0[21] = new int[]{2, 1, 3, 2, 1, 2};
        r0[22] = new int[]{2, 2, 3, 1, 1, 2};
        r0[23] = new int[]{3, 1, 2, 1, 3, 1};
        r0[24] = new int[]{3, 1, 1, 2, 2, 2};
        r0[25] = new int[]{3, 2, 1, 1, 2, 2};
        r0[26] = new int[]{3, 2, 1, 2, 2, 1};
        r0[27] = new int[]{3, 1, 2, 2, 1, 2};
        r0[28] = new int[]{3, 2, 2, 1, 1, 2};
        r0[29] = new int[]{3, 2, 2, 2, 1, 1};
        r0[30] = new int[]{2, 1, 2, 1, 2, 3};
        r0[31] = new int[]{2, 1, 2, 3, 2, 1};
        r0[32] = new int[]{2, 3, 2, 1, 2, 1};
        r0[33] = new int[]{1, 1, 1, 3, 2, 3};
        r0[34] = new int[]{1, 3, 1, 1, 2, 3};
        r0[35] = new int[]{1, 3, 1, 3, 2, 1};
        r0[36] = new int[]{1, 1, 2, 3, 1, 3};
        r0[37] = new int[]{1, 3, 2, 1, 1, 3};
        r0[38] = new int[]{1, 3, 2, 3, 1, 1};
        r0[39] = new int[]{2, 1, 1, 3, 1, 3};
        r0[40] = new int[]{2, 3, 1, 1, 1, 3};
        r0[41] = new int[]{2, 3, 1, 3, 1, 1};
        r0[42] = new int[]{1, 1, 2, 1, 3, 3};
        r0[43] = new int[]{1, 1, 2, 3, 3, 1};
        r0[44] = new int[]{1, 3, 2, 1, 3, 1};
        r0[45] = new int[]{1, 1, 3, 1, 2, 3};
        r0[46] = new int[]{1, 1, 3, 3, 2, 1};
        r0[47] = new int[]{1, 3, 3, 1, 2, 1};
        r0[48] = new int[]{3, 1, 3, 1, 2, 1};
        r0[49] = new int[]{2, 1, 1, 3, 3, 1};
        r0[50] = new int[]{2, 3, 1, 1, 3, 1};
        r0[51] = new int[]{2, 1, 3, 1, 1, 3};
        r0[52] = new int[]{2, 1, 3, 3, 1, 1};
        r0[53] = new int[]{2, 1, 3, 1, 3, 1};
        r0[54] = new int[]{3, 1, 1, 1, 2, 3};
        r0[55] = new int[]{3, 1, 1, 3, 2, 1};
        r0[56] = new int[]{3, 3, 1, 1, 2, 1};
        r0[57] = new int[]{3, 1, 2, 1, 1, 3};
        r0[58] = new int[]{3, 1, 2, 3, 1, 1};
        r0[59] = new int[]{3, 3, 2, 1, 1, 1};
        r0[60] = new int[]{3, 1, 4, 1, 1, 1};
        r0[61] = new int[]{2, 2, 1, 4, 1, 1};
        r0[62] = new int[]{4, 3, 1, 1, 1, 1};
        r0[63] = new int[]{1, 1, 1, 2, 2, 4};
        r0[64] = new int[]{1, 1, 1, 4, 2, 2};
        r0[65] = new int[]{1, 2, 1, 1, 2, 4};
        r0[66] = new int[]{1, 2, 1, 4, 2, 1};
        r0[67] = new int[]{1, 4, 1, 1, 2, 2};
        r0[68] = new int[]{1, 4, 1, 2, 2, 1};
        r0[69] = new int[]{1, 1, 2, 2, 1, 4};
        r0[70] = new int[]{1, 1, 2, 4, 1, 2};
        r0[71] = new int[]{1, 2, 2, 1, 1, 4};
        r0[72] = new int[]{1, 2, 2, 4, 1, 1};
        r0[73] = new int[]{1, 4, 2, 1, 1, 2};
        r0[74] = new int[]{1, 4, 2, 2, 1, 1};
        r0[75] = new int[]{2, 4, 1, 2, 1, 1};
        r0[76] = new int[]{2, 2, 1, 1, 1, 4};
        r0[77] = new int[]{4, 1, 3, 1, 1, 1};
        r0[78] = new int[]{2, 4, 1, 1, 1, 2};
        r0[79] = new int[]{1, 3, 4, 1, 1, 1};
        r0[80] = new int[]{1, 1, 1, 2, 4, 2};
        r0[81] = new int[]{1, 2, 1, 1, 4, 2};
        r0[82] = new int[]{1, 2, 1, 2, 4, 1};
        r0[83] = new int[]{1, 1, 4, 2, 1, 2};
        r0[84] = new int[]{1, 2, 4, 1, 1, 2};
        r0[85] = new int[]{1, 2, 4, 2, 1, 1};
        r0[86] = new int[]{4, 1, 1, 2, 1, 2};
        r0[87] = new int[]{4, 2, 1, 1, 1, 2};
        r0[88] = new int[]{4, 2, 1, 2, 1, 1};
        r0[89] = new int[]{2, 1, 2, 1, 4, 1};
        r0[90] = new int[]{2, 1, 4, 1, 2, 1};
        r0[91] = new int[]{4, 1, 2, 1, 2, 1};
        r0[92] = new int[]{1, 1, 1, 1, 4, 3};
        r0[93] = new int[]{1, 1, 1, 3, 4, 1};
        r0[94] = new int[]{1, 3, 1, 1, 4, 1};
        r0[95] = new int[]{1, 1, 4, 1, 1, 3};
        r0[96] = new int[]{1, 1, 4, 3, 1, 1};
        r0[97] = new int[]{4, 1, 1, 1, 1, 3};
        r0[98] = new int[]{4, 1, 1, 3, 1, 1};
        r0[99] = new int[]{1, 1, 3, 1, 4, 1};
        r0[100] = new int[]{1, 1, 4, 1, 3, 1};
        r0[101] = new int[]{3, 1, 1, 1, 4, 1};
        r0[102] = new int[]{4, 1, 1, 1, 3, 1};
        r0[103] = new int[]{2, 1, 1, 4, 1, 2};
        r0[104] = new int[]{2, 1, 1, 2, 1, 4};
        r0[105] = new int[]{2, 1, 1, 2, 3, 2};
        r0[106] = new int[]{2, 3, 3, 1, 1, 1, 2};
        CODE_PATTERNS = r0;
    }

    private static int decodeCode(BitArray bitArray, int[] iArr, int i) throws NotFoundException {
        OneDReader.recordPattern(bitArray, i, iArr);
        float f = 0.25f;
        int i2 = -1;
        for (int i3 = 0; i3 < CODE_PATTERNS.length; i3++) {
            float patternMatchVariance = OneDReader.patternMatchVariance(iArr, CODE_PATTERNS[i3], 0.7f);
            if (patternMatchVariance < f) {
                f = patternMatchVariance;
                i2 = i3;
            }
        }
        if (i2 >= 0) {
            return i2;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int[] findStartPattern(BitArray bitArray) throws NotFoundException {
        int size = bitArray.getSize();
        int nextSet = bitArray.getNextSet(0);
        int i = 0;
        Object obj = new int[6];
        int i2 = nextSet;
        int i3 = 0;
        int length = obj.length;
        int i4 = nextSet;
        while (i4 < size) {
            if ((bitArray.get(i4) ^ i3) == 0) {
                if (i != length - 1) {
                    i++;
                } else {
                    float f = 0.25f;
                    int i5 = -1;
                    for (int i6 = 103; i6 <= 105; i6++) {
                        float patternMatchVariance = OneDReader.patternMatchVariance(obj, CODE_PATTERNS[i6], 0.7f);
                        if (patternMatchVariance < f) {
                            f = patternMatchVariance;
                            i5 = i6;
                        }
                    }
                    if (i5 >= 0 && bitArray.isRange(Math.max(0, i2 - ((i4 - i2) / 2)), i2, false)) {
                        return new int[]{i2, i4, i5};
                    }
                    i2 += obj[0] + obj[1];
                    System.arraycopy(obj, 2, obj, 0, length - 2);
                    obj[length - 2] = null;
                    obj[length - 1] = null;
                    i--;
                }
                obj[i] = 1;
                i3 = i3 == 0 ? 1 : 0;
            } else {
                obj[i] = obj[i] + 1;
            }
            i4++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException, ChecksumException {
        int i2;
        Object obj = (map != null && map.containsKey(DecodeHintType.ASSUME_GS1)) ? 1 : null;
        int[] findStartPattern = findStartPattern(bitArray);
        int i3 = findStartPattern[2];
        List arrayList = new ArrayList(20);
        arrayList.add(Byte.valueOf((byte) i3));
        switch (i3) {
            case 103:
                i2 = 101;
                break;
            case 104:
                i2 = 100;
                break;
            case 105:
                i2 = 99;
                break;
            default:
                throw FormatException.getFormatInstance();
        }
        Object obj2 = null;
        Object obj3 = null;
        StringBuilder stringBuilder = new StringBuilder(20);
        int i4 = findStartPattern[0];
        int i5 = findStartPattern[1];
        int[] iArr = new int[6];
        int i6 = 0;
        int i7 = 0;
        int i8 = i3;
        int i9 = 0;
        Object obj4 = 1;
        Object obj5 = null;
        Object obj6 = null;
        while (obj2 == null) {
            Object obj7 = obj3;
            obj3 = null;
            i6 = i7;
            i7 = decodeCode(bitArray, iArr, i5);
            arrayList.add(Byte.valueOf((byte) i7));
            if (i7 != 106) {
                obj4 = 1;
            }
            if (i7 != 106) {
                i9++;
                i8 += i9 * i7;
            }
            i4 = i5;
            for (int i10 : iArr) {
                i5 += i10;
            }
            switch (i7) {
                case 103:
                case 104:
                case 105:
                    throw FormatException.getFormatInstance();
                default:
                    switch (i2) {
                        case 99:
                            if (i7 < 100) {
                                if (i7 < 10) {
                                    stringBuilder.append('0');
                                }
                                stringBuilder.append(i7);
                                break;
                            }
                            if (i7 != 106) {
                                obj4 = null;
                            }
                            switch (i7) {
                                case 100:
                                    i2 = 100;
                                    break;
                                case 101:
                                    i2 = 101;
                                    break;
                                case 102:
                                    if (obj != null) {
                                        if (stringBuilder.length() == 0) {
                                            stringBuilder.append("]C1");
                                            break;
                                        }
                                        stringBuilder.append('\u001d');
                                        break;
                                    }
                                    break;
                                case 103:
                                case 104:
                                case 105:
                                    break;
                                case 106:
                                    obj2 = 1;
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 100:
                            if (i7 < 96) {
                                if (obj6 != obj5) {
                                    stringBuilder.append((char) ((i7 + 32) + 128));
                                } else {
                                    stringBuilder.append((char) (i7 + 32));
                                }
                                obj6 = null;
                                break;
                            }
                            if (i7 != 106) {
                                obj4 = null;
                            }
                            switch (i7) {
                                case 96:
                                case 97:
                                case 103:
                                case 104:
                                case 105:
                                    break;
                                case 98:
                                    obj3 = 1;
                                    i2 = 101;
                                    break;
                                case 99:
                                    i2 = 99;
                                    break;
                                case 100:
                                    if (obj5 != null || obj6 == null) {
                                        if (obj5 != null && obj6 != null) {
                                            obj5 = null;
                                            obj6 = null;
                                            break;
                                        }
                                        obj6 = 1;
                                        break;
                                    }
                                    obj5 = 1;
                                    obj6 = null;
                                    break;
                                    break;
                                case 101:
                                    i2 = 101;
                                    break;
                                case 102:
                                    if (obj != null) {
                                        if (stringBuilder.length() == 0) {
                                            stringBuilder.append("]C1");
                                            break;
                                        }
                                        stringBuilder.append('\u001d');
                                        break;
                                    }
                                    break;
                                case 106:
                                    obj2 = 1;
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 101:
                            if (i7 >= 64) {
                                if (i7 < 96) {
                                    if (obj6 != obj5) {
                                        stringBuilder.append((char) (i7 + 64));
                                    } else {
                                        stringBuilder.append((char) (i7 - 64));
                                    }
                                    obj6 = null;
                                    break;
                                }
                                if (i7 != 106) {
                                    obj4 = null;
                                }
                                switch (i7) {
                                    case 96:
                                    case 97:
                                    case 103:
                                    case 104:
                                    case 105:
                                        break;
                                    case 98:
                                        obj3 = 1;
                                        i2 = 100;
                                        break;
                                    case 99:
                                        i2 = 99;
                                        break;
                                    case 100:
                                        i2 = 100;
                                        break;
                                    case 101:
                                        if (obj5 != null || obj6 == null) {
                                            if (obj5 != null && obj6 != null) {
                                                obj5 = null;
                                                obj6 = null;
                                                break;
                                            }
                                            obj6 = 1;
                                            break;
                                        }
                                        obj5 = 1;
                                        obj6 = null;
                                        break;
                                        break;
                                    case 102:
                                        if (obj != null) {
                                            if (stringBuilder.length() == 0) {
                                                stringBuilder.append("]C1");
                                                break;
                                            }
                                            stringBuilder.append('\u001d');
                                            break;
                                        }
                                        break;
                                    case 106:
                                        obj2 = 1;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (obj6 != obj5) {
                                stringBuilder.append((char) ((i7 + 32) + 128));
                            } else {
                                stringBuilder.append((char) (i7 + 32));
                            }
                            obj6 = null;
                            break;
                            break;
                    }
                    if (obj7 != null) {
                        i2 = i2 != 101 ? 101 : 100;
                    }
                    break;
            }
        }
        int i11 = i5 - i4;
        i5 = bitArray.getNextUnset(i5);
        if (!bitArray.isRange(i5, Math.min(bitArray.getSize(), ((i5 - i4) / 2) + i5), false)) {
            throw NotFoundException.getNotFoundInstance();
        } else if ((i8 - (i9 * i6)) % 103 == i6) {
            int length = stringBuilder.length();
            if (length != 0) {
                if (length > 0 && r14 != null) {
                    if (i2 != 99) {
                        stringBuilder.delete(length - 1, length);
                    } else {
                        stringBuilder.delete(length - 2, length);
                    }
                }
                float f = ((float) (findStartPattern[1] + findStartPattern[0])) / 2.0f;
                float f2 = ((float) i4) + (((float) i11) / 2.0f);
                int size = arrayList.size();
                byte[] bArr = new byte[size];
                for (int i12 = 0; i12 < size; i12++) {
                    bArr[i12] = (byte) ((Byte) arrayList.get(i12)).byteValue();
                }
                return new Result(stringBuilder.toString(), bArr, new ResultPoint[]{new ResultPoint(f, (float) i), new ResultPoint(f2, (float) i)}, BarcodeFormat.CODE_128);
            }
            throw NotFoundException.getNotFoundInstance();
        } else {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
