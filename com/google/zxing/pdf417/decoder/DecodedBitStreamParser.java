package com.google.zxing.pdf417.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.pdf417.PDF417ResultMetadata;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

final class DecodedBitStreamParser {
    private static /* synthetic */ int[] f6xca07ee0;
    private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-1");
    private static final BigInteger[] EXP900 = new BigInteger[16];
    private static final char[] MIXED_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '&', '\r', '\t', ',', ':', '#', '-', '.', '$', '/', '+', '%', '*', '=', '^'};
    private static final char[] PUNCT_CHARS = new char[]{';', '<', '>', '@', '[', '\\', ']', '_', '`', '~', '!', '\r', '\t', ',', ':', '\n', '-', '.', '$', '/', '\"', '|', '*', '(', ')', '?', '{', '}', '\''};

    private enum Mode {
        ALPHA,
        LOWER,
        MIXED,
        PUNCT,
        ALPHA_SHIFT,
        PUNCT_SHIFT
    }

    static /* synthetic */ int[] m7xca07ee0() {
        int[] iArr = f6xca07ee0;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[Mode.values().length];
        try {
            iArr[Mode.ALPHA.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.ALPHA_SHIFT.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.LOWER.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.MIXED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Mode.PUNCT.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Mode.PUNCT_SHIFT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f6xca07ee0 = iArr;
        return iArr;
    }

    static {
        EXP900[0] = BigInteger.ONE;
        BigInteger valueOf = BigInteger.valueOf(900);
        EXP900[1] = valueOf;
        for (int i = 2; i < EXP900.length; i++) {
            EXP900[i] = EXP900[i - 1].multiply(valueOf);
        }
    }

    private DecodedBitStreamParser() {
    }

    private static int byteCompaction(int i, int[] iArr, Charset charset, int i2, StringBuilder stringBuilder) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        long j;
        Object obj;
        int i3;
        int i4;
        int i5;
        if (i == 901) {
            j = 0;
            int[] iArr2 = new int[6];
            obj = null;
            i3 = i2 + 1;
            int i6 = iArr[i2];
            int i7 = 0;
            while (i3 < iArr[0] && r8 == null) {
                i4 = i7 + 1;
                iArr2[i7] = i6;
                j = (900 * j) + ((long) i6);
                i2 = i3 + 1;
                i6 = iArr[i3];
                if (i6 == 900 || i6 == 901 || i6 == 902 || i6 == 924 || i6 == 928 || i6 == 923 || i6 == 922) {
                    obj = 1;
                    i7 = i4;
                    i3 = i2 - 1;
                } else if (i4 % 5 == 0 && i4 > 0) {
                    for (i5 = 0; i5 < 6; i5++) {
                        byteArrayOutputStream.write((byte) ((int) (j >> ((5 - i5) * 8))));
                    }
                    j = 0;
                    i7 = 0;
                    i3 = i2;
                } else {
                    i7 = i4;
                    i3 = i2;
                }
            }
            if (i3 == iArr[0] && i6 < 900) {
                i4 = i7 + 1;
                iArr2[i7] = i6;
            } else {
                i4 = i7;
            }
            for (int i8 = 0; i8 < i4; i8++) {
                byteArrayOutputStream.write((byte) iArr2[i8]);
            }
            i2 = i3;
        } else if (i == 924) {
            i4 = 0;
            j = 0;
            obj = null;
            i3 = i2;
            while (i3 < iArr[0]) {
                if (obj != null) {
                    i2 = i3;
                    break;
                }
                i2 = i3 + 1;
                int i9 = iArr[i3];
                if (i9 < 900) {
                    i4++;
                    j = (900 * j) + ((long) i9);
                } else if (i9 == 900 || i9 == 901 || i9 == 902 || i9 == 924 || i9 == 928 || i9 == 923 || i9 == 922) {
                    i2--;
                    obj = 1;
                }
                if (i4 % 5 == 0 && i4 > 0) {
                    for (i5 = 0; i5 < 6; i5++) {
                        byteArrayOutputStream.write((byte) ((int) (j >> ((5 - i5) * 8))));
                    }
                    j = 0;
                    i4 = 0;
                    i3 = i2;
                } else {
                    i3 = i2;
                }
            }
            i2 = i3;
        }
        stringBuilder.append(new String(byteArrayOutputStream.toByteArray(), charset));
        return i2;
    }

    static DecoderResult decode(int[] iArr, String str) throws FormatException {
        StringBuilder stringBuilder = new StringBuilder(iArr.length * 2);
        Charset charset = DEFAULT_ENCODING;
        int i = iArr[1];
        PDF417ResultMetadata pDF417ResultMetadata = new PDF417ResultMetadata();
        int i2 = 2;
        while (i2 < iArr[0]) {
            int textCompaction;
            switch (i) {
                case 900:
                    textCompaction = textCompaction(iArr, i2, stringBuilder);
                    break;
                case 901:
                case 924:
                    textCompaction = byteCompaction(i, iArr, charset, i2, stringBuilder);
                    break;
                case 902:
                    textCompaction = numericCompaction(iArr, i2, stringBuilder);
                    break;
                case 913:
                    textCompaction = i2 + 1;
                    stringBuilder.append((char) iArr[i2]);
                    break;
                case 922:
                case 923:
                    throw FormatException.getFormatInstance();
                case 925:
                    textCompaction = i2 + 1;
                    break;
                case 926:
                    textCompaction = i2 + 2;
                    break;
                case 927:
                    textCompaction = i2 + 1;
                    charset = Charset.forName(CharacterSetECI.getCharacterSetECIByValue(iArr[i2]).name());
                    break;
                case 928:
                    textCompaction = decodeMacroBlock(iArr, i2, pDF417ResultMetadata);
                    break;
                default:
                    textCompaction = textCompaction(iArr, i2 - 1, stringBuilder);
                    break;
            }
            if (textCompaction >= iArr.length) {
                throw FormatException.getFormatInstance();
            }
            i2 = textCompaction + 1;
            i = iArr[textCompaction];
        }
        if (stringBuilder.length() != 0) {
            DecoderResult decoderResult = new DecoderResult(null, stringBuilder.toString(), null, str);
            decoderResult.setOther(pDF417ResultMetadata);
            return decoderResult;
        }
        throw FormatException.getFormatInstance();
    }

    private static String decodeBase900toBase10(int[] iArr, int i) throws FormatException {
        BigInteger bigInteger = BigInteger.ZERO;
        for (int i2 = 0; i2 < i; i2++) {
            bigInteger = bigInteger.add(EXP900[(i - i2) - 1].multiply(BigInteger.valueOf((long) iArr[i2])));
        }
        String bigInteger2 = bigInteger.toString();
        if (bigInteger2.charAt(0) == '1') {
            return bigInteger2.substring(1);
        }
        throw FormatException.getFormatInstance();
    }

    private static int decodeMacroBlock(int[] iArr, int i, PDF417ResultMetadata pDF417ResultMetadata) throws FormatException {
        if (i + 2 <= iArr[0]) {
            int[] iArr2 = new int[2];
            int i2 = 0;
            while (i2 < 2) {
                iArr2[i2] = iArr[i];
                i2++;
                i++;
            }
            pDF417ResultMetadata.setSegmentIndex(Integer.parseInt(decodeBase900toBase10(iArr2, 2)));
            StringBuilder stringBuilder = new StringBuilder();
            i = textCompaction(iArr, i, stringBuilder);
            pDF417ResultMetadata.setFileId(stringBuilder.toString());
            if (iArr[i] == 923) {
                i++;
                int[] iArr3 = new int[(iArr[0] - i)];
                Object obj = null;
                int i3 = 0;
                int i4 = i;
                while (i4 < iArr[0] && r5 == null) {
                    i = i4 + 1;
                    int i5 = iArr[i4];
                    if (i5 >= 900) {
                        switch (i5) {
                            case 922:
                                pDF417ResultMetadata.setLastSegment(true);
                                obj = 1;
                                i4 = i + 1;
                                break;
                            default:
                                throw FormatException.getFormatInstance();
                        }
                    }
                    int i6 = i3 + 1;
                    iArr3[i3] = i5;
                    i3 = i6;
                    i4 = i;
                }
                pDF417ResultMetadata.setOptionalData(Arrays.copyOf(iArr3, i3));
                return i4;
            } else if (iArr[i] != 922) {
                return i;
            } else {
                pDF417ResultMetadata.setLastSegment(true);
                return i + 1;
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static void decodeTextCompaction(int[] iArr, int[] iArr2, int i, StringBuilder stringBuilder) {
        Mode mode = Mode.ALPHA;
        Mode mode2 = Mode.ALPHA;
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = iArr[i2];
            char c = '\u0000';
            switch (m7xca07ee0()[mode.ordinal()]) {
                case 1:
                    if (i3 >= 26) {
                        if (i3 != 26) {
                            if (i3 != 27) {
                                if (i3 != 28) {
                                    if (i3 != 29) {
                                        if (i3 != 913) {
                                            if (i3 == 900) {
                                                mode = Mode.ALPHA;
                                                break;
                                            }
                                        }
                                        stringBuilder.append((char) iArr2[i2]);
                                        break;
                                    }
                                    mode2 = mode;
                                    mode = Mode.PUNCT_SHIFT;
                                    break;
                                }
                                mode = Mode.MIXED;
                                break;
                            }
                            mode = Mode.LOWER;
                            break;
                        }
                        c = ' ';
                        break;
                    }
                    c = (char) (i3 + 65);
                    break;
                    break;
                case 2:
                    if (i3 >= 26) {
                        if (i3 != 26) {
                            if (i3 != 27) {
                                if (i3 != 28) {
                                    if (i3 != 29) {
                                        if (i3 != 913) {
                                            if (i3 == 900) {
                                                mode = Mode.ALPHA;
                                                break;
                                            }
                                        }
                                        stringBuilder.append((char) iArr2[i2]);
                                        break;
                                    }
                                    mode2 = mode;
                                    mode = Mode.PUNCT_SHIFT;
                                    break;
                                }
                                mode = Mode.MIXED;
                                break;
                            }
                            mode2 = mode;
                            mode = Mode.ALPHA_SHIFT;
                            break;
                        }
                        c = ' ';
                        break;
                    }
                    c = (char) (i3 + 97);
                    break;
                    break;
                case 3:
                    if (i3 >= 25) {
                        if (i3 != 25) {
                            if (i3 != 26) {
                                if (i3 != 27) {
                                    if (i3 != 28) {
                                        if (i3 != 29) {
                                            if (i3 != 913) {
                                                if (i3 == 900) {
                                                    mode = Mode.ALPHA;
                                                    break;
                                                }
                                            }
                                            stringBuilder.append((char) iArr2[i2]);
                                            break;
                                        }
                                        mode2 = mode;
                                        mode = Mode.PUNCT_SHIFT;
                                        break;
                                    }
                                    mode = Mode.ALPHA;
                                    break;
                                }
                                mode = Mode.LOWER;
                                break;
                            }
                            c = ' ';
                            break;
                        }
                        mode = Mode.PUNCT;
                        break;
                    }
                    c = MIXED_CHARS[i3];
                    break;
                    break;
                case 4:
                    if (i3 >= 29) {
                        if (i3 != 29) {
                            if (i3 != 913) {
                                if (i3 == 900) {
                                    mode = Mode.ALPHA;
                                    break;
                                }
                            }
                            stringBuilder.append((char) iArr2[i2]);
                            break;
                        }
                        mode = Mode.ALPHA;
                        break;
                    }
                    c = PUNCT_CHARS[i3];
                    break;
                    break;
                case 5:
                    mode = mode2;
                    if (i3 >= 26) {
                        if (i3 != 26) {
                            if (i3 == 900) {
                                mode = Mode.ALPHA;
                                break;
                            }
                        }
                        c = ' ';
                        break;
                    }
                    c = (char) (i3 + 65);
                    break;
                    break;
                case 6:
                    mode = mode2;
                    if (i3 >= 29) {
                        if (i3 != 29) {
                            if (i3 != 913) {
                                if (i3 == 900) {
                                    mode = Mode.ALPHA;
                                    break;
                                }
                            }
                            stringBuilder.append((char) iArr2[i2]);
                            break;
                        }
                        mode = Mode.ALPHA;
                        break;
                    }
                    c = PUNCT_CHARS[i3];
                    break;
                    break;
            }
            if (c != '\u0000') {
                stringBuilder.append(c);
            }
        }
    }

    private static int numericCompaction(int[] iArr, int i, StringBuilder stringBuilder) throws FormatException {
        int i2 = 0;
        Object obj = null;
        int[] iArr2 = new int[15];
        int i3 = i;
        while (i3 < iArr[0] && r3 == null) {
            i = i3 + 1;
            int i4 = iArr[i3];
            if (i == iArr[0]) {
                obj = 1;
            }
            if (i4 < 900) {
                iArr2[i2] = i4;
                i2++;
            } else if (i4 == 900 || i4 == 901 || i4 == 924 || i4 == 928 || i4 == 923 || i4 == 922) {
                i--;
                obj = 1;
            }
            if (i2 % 15 != 0 && i4 != 902 && r3 == null) {
                i3 = i;
            } else if (i2 <= 0) {
                i3 = i;
            } else {
                stringBuilder.append(decodeBase900toBase10(iArr2, i2));
                i2 = 0;
                i3 = i;
            }
        }
        return i3;
    }

    private static int textCompaction(int[] iArr, int i, StringBuilder stringBuilder) {
        int[] iArr2 = new int[((iArr[0] - i) * 2)];
        int[] iArr3 = new int[((iArr[0] - i) * 2)];
        Object obj = null;
        int i2 = 0;
        int i3 = i;
        while (i3 < iArr[0] && r3 == null) {
            i = i3 + 1;
            int i4 = iArr[i3];
            if (i4 >= 900) {
                switch (i4) {
                    case 900:
                        int i5 = i2 + 1;
                        iArr2[i2] = 900;
                        i2 = i5;
                        i3 = i;
                        break;
                    case 901:
                    case 902:
                    case 922:
                    case 923:
                    case 924:
                    case 928:
                        obj = 1;
                        i3 = i - 1;
                        break;
                    case 913:
                        iArr2[i2] = 913;
                        i3 = i + 1;
                        iArr3[i2] = iArr[i];
                        i2++;
                        break;
                    default:
                        i3 = i;
                        break;
                }
            }
            iArr2[i2] = i4 / 30;
            iArr2[i2 + 1] = i4 % 30;
            i2 += 2;
            i3 = i;
        }
        decodeTextCompaction(iArr2, iArr3, i2, stringBuilder);
        return i3;
    }
}
