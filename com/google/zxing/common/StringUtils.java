package com.google.zxing.common;

import com.google.zxing.DecodeHintType;
import java.nio.charset.Charset;
import java.util.Map;

public final class StringUtils {
    private static final boolean ASSUME_SHIFT_JIS;
    private static final String PLATFORM_DEFAULT_ENCODING = Charset.defaultCharset().name();

    static {
        boolean z = false;
        if (!"SJIS".equalsIgnoreCase(PLATFORM_DEFAULT_ENCODING)) {
            if ("EUC_JP".equalsIgnoreCase(PLATFORM_DEFAULT_ENCODING)) {
            }
            ASSUME_SHIFT_JIS = z;
        }
        z = true;
        ASSUME_SHIFT_JIS = z;
    }

    private StringUtils() {
    }

    public static String guessEncoding(byte[] bArr, Map<DecodeHintType, ?> map) {
        if (map != null) {
            String str = (String) map.get(DecodeHintType.CHARACTER_SET);
            if (str != null) {
                return str;
            }
        }
        Object obj = 1;
        Object obj2 = 1;
        Object obj3 = 1;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        Object obj4 = (bArr.length > 3 && bArr[0] == (byte) -17 && bArr[1] == (byte) -69 && bArr[2] == (byte) -65) ? 1 : null;
        for (byte b : bArr) {
            if (obj == null && obj2 == null) {
                if (obj3 == null) {
                    break;
                }
            }
            int i12 = b & 255;
            if (obj3 != null) {
                if (i <= 0) {
                    if ((i12 & 128) != 0) {
                        if ((i12 & 64) != 0) {
                            i++;
                            if ((i12 & 32) != 0) {
                                i++;
                                if ((i12 & 16) != 0) {
                                    i++;
                                    if ((i12 & 8) != 0) {
                                        obj3 = null;
                                    } else {
                                        i4++;
                                    }
                                } else {
                                    i3++;
                                }
                            } else {
                                i2++;
                            }
                        } else {
                            obj3 = null;
                        }
                    }
                } else if ((i12 & 128) != 0) {
                    i--;
                } else {
                    obj3 = null;
                }
            }
            if (obj != null) {
                if (i12 > 127 && i12 < 160) {
                    obj = null;
                } else if (i12 > 159) {
                    if (i12 < 192 || i12 == 215 || i12 == 247) {
                        i11++;
                    }
                }
            }
            if (obj2 != null) {
                if (i5 <= 0) {
                    if (i12 == 128 || i12 == 160 || i12 > 239) {
                        obj2 = null;
                    } else if (i12 > 160 && i12 < 224) {
                        i6++;
                        i8 = 0;
                        i7++;
                        if (i7 > i9) {
                            i9 = i7;
                        }
                    } else if (i12 <= 127) {
                        i7 = 0;
                        i8 = 0;
                    } else {
                        i5++;
                        i7 = 0;
                        i8++;
                        if (i8 > i10) {
                            i10 = i8;
                        }
                    }
                } else if (i12 >= 64 && i12 != 127 && i12 <= 252) {
                    i5--;
                } else {
                    obj2 = null;
                }
            }
        }
        if (obj3 != null && i > 0) {
            obj3 = null;
        }
        if (obj2 != null && i5 > 0) {
            obj2 = null;
        }
        if (obj3 != null) {
            if (obj4 != null || (i2 + i3) + i4 > 0) {
                return "UTF8";
            }
        }
        if (obj2 != null) {
            if (ASSUME_SHIFT_JIS || i9 >= 3 || i10 >= 3) {
                return "SJIS";
            }
        }
        if (obj == null || obj2 == null) {
            return obj == null ? obj2 == null ? obj3 == null ? PLATFORM_DEFAULT_ENCODING : "UTF8" : "SJIS" : "ISO8859_1";
        } else {
            String str2 = ((i9 == 2 && i6 == 2) || i11 * 10 >= r8) ? "SJIS" : "ISO8859_1";
            return str2;
        }
    }
}
