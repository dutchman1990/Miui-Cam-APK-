package com.google.zxing.datamatrix.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.DecoderResult;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DecodedBitStreamParser {
    private static /* synthetic */ int[] f5xd23d60a3;
    private static final char[] C40_BASIC_SET_CHARS = new char[]{'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] C40_SHIFT2_SET_CHARS = new char[]{'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_'};
    private static final char[] TEXT_BASIC_SET_CHARS = new char[]{'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] TEXT_SHIFT3_SET_CHARS = new char[]{'`', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '|', '}', '~', ''};

    private enum Mode {
        PAD_ENCODE,
        ASCII_ENCODE,
        C40_ENCODE,
        TEXT_ENCODE,
        ANSIX12_ENCODE,
        EDIFACT_ENCODE,
        BASE256_ENCODE
    }

    static /* synthetic */ int[] m6xd23d60a3() {
        int[] iArr = f5xd23d60a3;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[Mode.values().length];
        try {
            iArr[Mode.ANSIX12_ENCODE.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.ASCII_ENCODE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.BASE256_ENCODE.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.C40_ENCODE.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Mode.EDIFACT_ENCODE.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Mode.PAD_ENCODE.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Mode.TEXT_ENCODE.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        f5xd23d60a3 = iArr;
        return iArr;
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bArr) throws FormatException {
        BitSource bitSource = new BitSource(bArr);
        StringBuilder stringBuilder = new StringBuilder(100);
        CharSequence stringBuilder2 = new StringBuilder(0);
        List arrayList = new ArrayList(1);
        Mode mode = Mode.ASCII_ENCODE;
        while (true) {
            if (mode != Mode.ASCII_ENCODE) {
                switch (m6xd23d60a3()[mode.ordinal()]) {
                    case 3:
                        decodeC40Segment(bitSource, stringBuilder);
                        break;
                    case 4:
                        decodeTextSegment(bitSource, stringBuilder);
                        break;
                    case 5:
                        decodeAnsiX12Segment(bitSource, stringBuilder);
                        break;
                    case 6:
                        decodeEdifactSegment(bitSource, stringBuilder);
                        break;
                    case 7:
                        decodeBase256Segment(bitSource, stringBuilder, arrayList);
                        break;
                    default:
                        throw FormatException.getFormatInstance();
                }
                mode = Mode.ASCII_ENCODE;
            } else {
                mode = decodeAsciiSegment(bitSource, stringBuilder, stringBuilder2);
            }
            if (mode == Mode.PAD_ENCODE || bitSource.available() <= 0) {
                if (stringBuilder2.length() > 0) {
                    stringBuilder.append(stringBuilder2);
                }
                String stringBuilder3 = stringBuilder.toString();
                if (arrayList.isEmpty()) {
                    arrayList = null;
                }
                return new DecoderResult(bArr, stringBuilder3, arrayList, null);
            }
        }
    }

    private static void decodeAnsiX12Segment(BitSource bitSource, StringBuilder stringBuilder) throws FormatException {
        int[] iArr = new int[3];
        while (bitSource.available() != 8) {
            int readBits = bitSource.readBits(8);
            if (readBits != 254) {
                parseTwoBytes(readBits, bitSource.readBits(8), iArr);
                for (int i = 0; i < 3; i++) {
                    int i2 = iArr[i];
                    if (i2 == 0) {
                        stringBuilder.append('\r');
                    } else if (i2 == 1) {
                        stringBuilder.append('*');
                    } else if (i2 == 2) {
                        stringBuilder.append('>');
                    } else if (i2 == 3) {
                        stringBuilder.append(' ');
                    } else if (i2 < 14) {
                        stringBuilder.append((char) (i2 + 44));
                    } else if (i2 >= 40) {
                        throw FormatException.getFormatInstance();
                    } else {
                        stringBuilder.append((char) (i2 + 51));
                    }
                }
                if (bitSource.available() <= 0) {
                    return;
                }
            }
            return;
        }
    }

    private static Mode decodeAsciiSegment(BitSource bitSource, StringBuilder stringBuilder, StringBuilder stringBuilder2) throws FormatException {
        Object obj = null;
        do {
            int readBits = bitSource.readBits(8);
            if (readBits == 0) {
                throw FormatException.getFormatInstance();
            } else if (readBits <= 128) {
                if (obj != null) {
                    readBits += 128;
                }
                stringBuilder.append((char) (readBits - 1));
                return Mode.ASCII_ENCODE;
            } else if (readBits == 129) {
                return Mode.PAD_ENCODE;
            } else {
                if (readBits <= 229) {
                    int i = readBits - 130;
                    if (i < 10) {
                        stringBuilder.append('0');
                    }
                    stringBuilder.append(i);
                } else if (readBits == 230) {
                    return Mode.C40_ENCODE;
                } else {
                    if (readBits == 231) {
                        return Mode.BASE256_ENCODE;
                    }
                    if (readBits == 232) {
                        stringBuilder.append('\u001d');
                    } else if (!(readBits == 233 || readBits == 234)) {
                        if (readBits == 235) {
                            obj = 1;
                        } else if (readBits == 236) {
                            stringBuilder.append("[)>\u001e05\u001d");
                            stringBuilder2.insert(0, "\u001e\u0004");
                        } else if (readBits == 237) {
                            stringBuilder.append("[)>\u001e06\u001d");
                            stringBuilder2.insert(0, "\u001e\u0004");
                        } else if (readBits == 238) {
                            return Mode.ANSIX12_ENCODE;
                        } else {
                            if (readBits == 239) {
                                return Mode.TEXT_ENCODE;
                            }
                            if (readBits == 240) {
                                return Mode.EDIFACT_ENCODE;
                            }
                            if (readBits != 241 && readBits >= 242) {
                                if (readBits != 254 || bitSource.available() != 0) {
                                    throw FormatException.getFormatInstance();
                                }
                            }
                        }
                    }
                }
            }
        } while (bitSource.available() > 0);
        return Mode.ASCII_ENCODE;
    }

    private static void decodeBase256Segment(BitSource bitSource, StringBuilder stringBuilder, Collection<byte[]> collection) throws FormatException {
        int available;
        int byteOffset = bitSource.getByteOffset() + 1;
        int i = byteOffset + 1;
        int unrandomize255State = unrandomize255State(bitSource.readBits(8), byteOffset);
        if (unrandomize255State == 0) {
            available = bitSource.available() / 8;
            byteOffset = i;
        } else if (unrandomize255State >= 250) {
            byteOffset = i + 1;
            available = ((unrandomize255State - 249) * 250) + unrandomize255State(bitSource.readBits(8), i);
        } else {
            available = unrandomize255State;
            byteOffset = i;
        }
        if (available >= 0) {
            Object obj = new byte[available];
            int i2 = 0;
            i = byteOffset;
            while (i2 < available) {
                if (bitSource.available() >= 8) {
                    byteOffset = i + 1;
                    obj[i2] = (byte) ((byte) unrandomize255State(bitSource.readBits(8), i));
                    i2++;
                    i = byteOffset;
                } else {
                    throw FormatException.getFormatInstance();
                }
            }
            collection.add(obj);
            try {
                stringBuilder.append(new String(obj, "ISO8859_1"));
                return;
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Platform does not support required encoding: " + e);
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static void decodeC40Segment(BitSource bitSource, StringBuilder stringBuilder) throws FormatException {
        Object obj = null;
        int[] iArr = new int[3];
        int i = 0;
        while (bitSource.available() != 8) {
            int readBits = bitSource.readBits(8);
            if (readBits != 254) {
                parseTwoBytes(readBits, bitSource.readBits(8), iArr);
                for (int i2 = 0; i2 < 3; i2++) {
                    int i3 = iArr[i2];
                    char c;
                    switch (i) {
                        case 0:
                            if (i3 >= 3) {
                                if (i3 < C40_BASIC_SET_CHARS.length) {
                                    c = C40_BASIC_SET_CHARS[i3];
                                    if (obj != null) {
                                        stringBuilder.append((char) (c + 128));
                                        obj = null;
                                        break;
                                    }
                                    stringBuilder.append(c);
                                    break;
                                }
                                throw FormatException.getFormatInstance();
                            }
                            i = i3 + 1;
                            break;
                        case 1:
                            if (obj == null) {
                                stringBuilder.append((char) i3);
                            } else {
                                stringBuilder.append((char) (i3 + 128));
                                obj = null;
                            }
                            i = 0;
                            break;
                        case 2:
                            if (i3 < C40_SHIFT2_SET_CHARS.length) {
                                c = C40_SHIFT2_SET_CHARS[i3];
                                if (obj == null) {
                                    stringBuilder.append(c);
                                } else {
                                    stringBuilder.append((char) (c + 128));
                                    obj = null;
                                }
                            } else if (i3 == 27) {
                                stringBuilder.append('\u001d');
                            } else if (i3 != 30) {
                                throw FormatException.getFormatInstance();
                            } else {
                                obj = 1;
                            }
                            i = 0;
                            break;
                        case 3:
                            if (obj == null) {
                                stringBuilder.append((char) (i3 + 96));
                            } else {
                                stringBuilder.append((char) (i3 + 224));
                                obj = null;
                            }
                            i = 0;
                            break;
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
                if (bitSource.available() <= 0) {
                    return;
                }
            }
            return;
        }
    }

    private static void decodeEdifactSegment(BitSource bitSource, StringBuilder stringBuilder) {
        while (bitSource.available() > 16) {
            int i = 0;
            while (i < 4) {
                int readBits = bitSource.readBits(6);
                if (readBits != 31) {
                    if ((readBits & 32) == 0) {
                        readBits |= 64;
                    }
                    stringBuilder.append((char) readBits);
                    i++;
                } else {
                    int bitOffset = 8 - bitSource.getBitOffset();
                    if (bitOffset != 8) {
                        bitSource.readBits(bitOffset);
                    }
                    return;
                }
            }
            if (bitSource.available() <= 0) {
                return;
            }
        }
    }

    private static void decodeTextSegment(BitSource bitSource, StringBuilder stringBuilder) throws FormatException {
        Object obj = null;
        int[] iArr = new int[3];
        int i = 0;
        while (bitSource.available() != 8) {
            int readBits = bitSource.readBits(8);
            if (readBits != 254) {
                parseTwoBytes(readBits, bitSource.readBits(8), iArr);
                for (int i2 = 0; i2 < 3; i2++) {
                    int i3 = iArr[i2];
                    char c;
                    switch (i) {
                        case 0:
                            if (i3 >= 3) {
                                if (i3 < TEXT_BASIC_SET_CHARS.length) {
                                    c = TEXT_BASIC_SET_CHARS[i3];
                                    if (obj != null) {
                                        stringBuilder.append((char) (c + 128));
                                        obj = null;
                                        break;
                                    }
                                    stringBuilder.append(c);
                                    break;
                                }
                                throw FormatException.getFormatInstance();
                            }
                            i = i3 + 1;
                            break;
                        case 1:
                            if (obj == null) {
                                stringBuilder.append((char) i3);
                            } else {
                                stringBuilder.append((char) (i3 + 128));
                                obj = null;
                            }
                            i = 0;
                            break;
                        case 2:
                            if (i3 < C40_SHIFT2_SET_CHARS.length) {
                                char c2 = C40_SHIFT2_SET_CHARS[i3];
                                if (obj == null) {
                                    stringBuilder.append(c2);
                                } else {
                                    stringBuilder.append((char) (c2 + 128));
                                    obj = null;
                                }
                            } else if (i3 == 27) {
                                stringBuilder.append('\u001d');
                            } else if (i3 != 30) {
                                throw FormatException.getFormatInstance();
                            } else {
                                obj = 1;
                            }
                            i = 0;
                            break;
                        case 3:
                            if (i3 < TEXT_SHIFT3_SET_CHARS.length) {
                                c = TEXT_SHIFT3_SET_CHARS[i3];
                                if (obj == null) {
                                    stringBuilder.append(c);
                                } else {
                                    stringBuilder.append((char) (c + 128));
                                    obj = null;
                                }
                                i = 0;
                                break;
                            }
                            throw FormatException.getFormatInstance();
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
                if (bitSource.available() <= 0) {
                    return;
                }
            }
            return;
        }
    }

    private static void parseTwoBytes(int i, int i2, int[] iArr) {
        int i3 = ((i << 8) + i2) - 1;
        int i4 = i3 / 1600;
        iArr[0] = i4;
        i3 -= i4 * 1600;
        i4 = i3 / 40;
        iArr[1] = i4;
        iArr[2] = i3 - (i4 * 40);
    }

    private static int unrandomize255State(int i, int i2) {
        int i3 = i - (((i2 * 149) % 255) + 1);
        return i3 < 0 ? i3 + 256 : i3;
    }
}
