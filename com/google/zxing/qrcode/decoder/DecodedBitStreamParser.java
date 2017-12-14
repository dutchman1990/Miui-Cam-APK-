package com.google.zxing.qrcode.decoder;

import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.StringUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

final class DecodedBitStreamParser {
    private static final char[] ALPHANUMERIC_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':'};

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bArr, Version version, ErrorCorrectionLevel errorCorrectionLevel, Map<DecodeHintType, ?> map) throws FormatException {
        BitSource bitSource = new BitSource(bArr);
        StringBuilder stringBuilder = new StringBuilder(50);
        Collection arrayList = new ArrayList(1);
        int i = -1;
        int i2 = -1;
        CharacterSetECI characterSetECI = null;
        boolean z = false;
        Mode forBits;
        do {
            try {
                forBits = bitSource.available() >= 4 ? Mode.forBits(bitSource.readBits(4)) : Mode.TERMINATOR;
                if (forBits != Mode.TERMINATOR) {
                    if (forBits == Mode.FNC1_FIRST_POSITION || forBits == Mode.FNC1_SECOND_POSITION) {
                        z = true;
                    } else if (forBits != Mode.STRUCTURED_APPEND) {
                        if (forBits == Mode.ECI) {
                            characterSetECI = CharacterSetECI.getCharacterSetECIByValue(parseECIValue(bitSource));
                            if (characterSetECI == null) {
                                throw FormatException.getFormatInstance();
                            }
                        } else if (forBits != Mode.HANZI) {
                            int readBits = bitSource.readBits(forBits.getCharacterCountBits(version));
                            if (forBits == Mode.NUMERIC) {
                                decodeNumericSegment(bitSource, stringBuilder, readBits);
                            } else if (forBits == Mode.ALPHANUMERIC) {
                                decodeAlphanumericSegment(bitSource, stringBuilder, readBits, z);
                            } else if (forBits == Mode.BYTE) {
                                decodeByteSegment(bitSource, stringBuilder, readBits, characterSetECI, arrayList, map);
                            } else if (forBits != Mode.KANJI) {
                                throw FormatException.getFormatInstance();
                            } else {
                                decodeKanjiSegment(bitSource, stringBuilder, readBits);
                            }
                        } else {
                            int readBits2 = bitSource.readBits(4);
                            int readBits3 = bitSource.readBits(forBits.getCharacterCountBits(version));
                            if (readBits2 == 1) {
                                decodeHanziSegment(bitSource, stringBuilder, readBits3);
                            }
                        }
                    } else if (bitSource.available() >= 16) {
                        i = bitSource.readBits(8);
                        i2 = bitSource.readBits(8);
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
            } catch (IllegalArgumentException e) {
                throw FormatException.getFormatInstance();
            }
        } while (forBits != Mode.TERMINATOR);
        return new DecoderResult(bArr, stringBuilder.toString(), !arrayList.isEmpty() ? arrayList : null, errorCorrectionLevel != null ? errorCorrectionLevel.toString() : null, i, i2);
    }

    private static void decodeAlphanumericSegment(BitSource bitSource, StringBuilder stringBuilder, int i, boolean z) throws FormatException {
        int length = stringBuilder.length();
        while (i > 1) {
            if (bitSource.available() >= 11) {
                int readBits = bitSource.readBits(11);
                stringBuilder.append(toAlphaNumericChar(readBits / 45));
                stringBuilder.append(toAlphaNumericChar(readBits % 45));
                i -= 2;
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (i == 1) {
            if (bitSource.available() >= 6) {
                stringBuilder.append(toAlphaNumericChar(bitSource.readBits(6)));
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (z) {
            int i2 = length;
            while (i2 < stringBuilder.length()) {
                if (stringBuilder.charAt(i2) == '%') {
                    if (i2 < stringBuilder.length() - 1 && stringBuilder.charAt(i2 + 1) == '%') {
                        stringBuilder.deleteCharAt(i2 + 1);
                    } else {
                        stringBuilder.setCharAt(i2, '\u001d');
                    }
                }
                i2++;
            }
        }
    }

    private static void decodeByteSegment(BitSource bitSource, StringBuilder stringBuilder, int i, CharacterSetECI characterSetECI, Collection<byte[]> collection, Map<DecodeHintType, ?> map) throws FormatException {
        if (i * 8 <= bitSource.available()) {
            Object obj = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                obj[i2] = (byte) ((byte) bitSource.readBits(8));
            }
            try {
                stringBuilder.append(new String(obj, characterSetECI != null ? characterSetECI.name() : StringUtils.guessEncoding(obj, map)));
                collection.add(obj);
                return;
            } catch (UnsupportedEncodingException e) {
                throw FormatException.getFormatInstance();
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static void decodeHanziSegment(BitSource bitSource, StringBuilder stringBuilder, int i) throws FormatException {
        if (i * 13 <= bitSource.available()) {
            byte[] bArr = new byte[(i * 2)];
            int i2 = 0;
            while (i > 0) {
                int readBits = bitSource.readBits(13);
                int i3 = ((readBits / 96) << 8) | (readBits % 96);
                i3 = i3 >= 959 ? i3 + 42657 : i3 + 41377;
                bArr[i2] = (byte) ((byte) ((i3 >> 8) & 255));
                bArr[i2 + 1] = (byte) ((byte) (i3 & 255));
                i2 += 2;
                i--;
            }
            try {
                stringBuilder.append(new String(bArr, "GB2312"));
                return;
            } catch (UnsupportedEncodingException e) {
                throw FormatException.getFormatInstance();
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static void decodeKanjiSegment(BitSource bitSource, StringBuilder stringBuilder, int i) throws FormatException {
        if (i * 13 <= bitSource.available()) {
            byte[] bArr = new byte[(i * 2)];
            int i2 = 0;
            while (i > 0) {
                int readBits = bitSource.readBits(13);
                int i3 = ((readBits / 192) << 8) | (readBits % 192);
                i3 = i3 >= 7936 ? i3 + 49472 : i3 + 33088;
                bArr[i2] = (byte) ((byte) (i3 >> 8));
                bArr[i2 + 1] = (byte) ((byte) i3);
                i2 += 2;
                i--;
            }
            try {
                stringBuilder.append(new String(bArr, "SJIS"));
                return;
            } catch (UnsupportedEncodingException e) {
                throw FormatException.getFormatInstance();
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static void decodeNumericSegment(BitSource bitSource, StringBuilder stringBuilder, int i) throws FormatException {
        while (i >= 3) {
            if (bitSource.available() >= 10) {
                int readBits = bitSource.readBits(10);
                if (readBits < 1000) {
                    stringBuilder.append(toAlphaNumericChar(readBits / 100));
                    stringBuilder.append(toAlphaNumericChar((readBits / 10) % 10));
                    stringBuilder.append(toAlphaNumericChar(readBits % 10));
                    i -= 3;
                } else {
                    throw FormatException.getFormatInstance();
                }
            }
            throw FormatException.getFormatInstance();
        }
        if (i != 2) {
            if (i == 1) {
                if (bitSource.available() >= 4) {
                    int readBits2 = bitSource.readBits(4);
                    if (readBits2 < 10) {
                        stringBuilder.append(toAlphaNumericChar(readBits2));
                        return;
                    }
                    throw FormatException.getFormatInstance();
                }
                throw FormatException.getFormatInstance();
            }
        } else if (bitSource.available() >= 7) {
            int readBits3 = bitSource.readBits(7);
            if (readBits3 < 100) {
                stringBuilder.append(toAlphaNumericChar(readBits3 / 10));
                stringBuilder.append(toAlphaNumericChar(readBits3 % 10));
                return;
            }
            throw FormatException.getFormatInstance();
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    private static int parseECIValue(BitSource bitSource) throws FormatException {
        int readBits = bitSource.readBits(8);
        if ((readBits & 128) == 0) {
            return readBits & 127;
        }
        if ((readBits & 192) == 128) {
            return ((readBits & 63) << 8) | bitSource.readBits(8);
        } else if ((readBits & 224) != 192) {
            throw FormatException.getFormatInstance();
        } else {
            return ((readBits & 31) << 16) | bitSource.readBits(16);
        }
    }

    private static char toAlphaNumericChar(int i) throws FormatException {
        if (i < ALPHANUMERIC_CHARS.length) {
            return ALPHANUMERIC_CHARS[i];
        }
        throw FormatException.getFormatInstance();
    }
}
