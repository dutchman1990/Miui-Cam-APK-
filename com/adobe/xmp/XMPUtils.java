package com.adobe.xmp;

import com.adobe.xmp.impl.Base64;
import com.adobe.xmp.impl.ISO8601Converter;

public class XMPUtils {
    private XMPUtils() {
    }

    public static String convertFromBoolean(boolean z) {
        return z ? "True" : "False";
    }

    public static String convertFromDate(XMPDateTime xMPDateTime) {
        return ISO8601Converter.render(xMPDateTime);
    }

    public static String convertFromDouble(double d) {
        return String.valueOf(d);
    }

    public static String convertFromInteger(int i) {
        return String.valueOf(i);
    }

    public static String convertFromLong(long j) {
        return String.valueOf(j);
    }

    public static XMPDateTime convertToDate(String str) throws XMPException {
        if (str != null && str.length() != 0) {
            return ISO8601Converter.parse(str);
        }
        throw new XMPException("Empty convert-string", 5);
    }

    public static String encodeBase64(byte[] bArr) {
        return new String(Base64.encode(bArr));
    }
}
