package com.adobe.xmp.impl;

public class Utils {
    private static boolean[] xmlNameChars;
    private static boolean[] xmlNameStartChars;

    static {
        initCharTables();
    }

    private Utils() {
    }

    static boolean checkUUIDFormat(String str) {
        boolean z = false;
        Object obj = 1;
        int i = 0;
        if (str == null) {
            return false;
        }
        int i2 = 0;
        while (i2 < str.length()) {
            if (str.charAt(i2) == '-') {
                i++;
                obj = obj != null ? (i2 == 8 || i2 == 13 || i2 == 18 || i2 == 23) ? 1 : null : null;
            }
            i2++;
        }
        if (obj != null && 4 == i && 36 == i2) {
            z = true;
        }
        return z;
    }

    public static String escapeXML(String str, boolean z, boolean z2) {
        int i;
        Object obj = null;
        for (i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt == '<' || charAt == '>' || charAt == '&' || ((z2 && (charAt == '\t' || charAt == '\n' || charAt == '\r')) || (z && charAt == '\"'))) {
                obj = 1;
                break;
            }
        }
        if (obj == null) {
            return str;
        }
        StringBuffer stringBuffer = new StringBuffer((str.length() * 4) / 3);
        for (i = 0; i < str.length(); i++) {
            charAt = str.charAt(i);
            Object obj2 = (z2 && (charAt == '\t' || charAt == '\n' || charAt == '\r')) ? 1 : null;
            if (obj2 == null) {
                switch (charAt) {
                    case '\"':
                        stringBuffer.append(z ? "&quot;" : "\"");
                        break;
                    case '&':
                        stringBuffer.append("&amp;");
                        break;
                    case '<':
                        stringBuffer.append("&lt;");
                        break;
                    case '>':
                        stringBuffer.append("&gt;");
                        break;
                    default:
                        stringBuffer.append(charAt);
                        break;
                }
            }
            stringBuffer.append("&#x");
            stringBuffer.append(Integer.toHexString(charAt).toUpperCase());
            stringBuffer.append(';');
        }
        return stringBuffer.toString();
    }

    private static void initCharTables() {
        xmlNameChars = new boolean[256];
        xmlNameStartChars = new boolean[256];
        int i = 0;
        while (i < xmlNameChars.length) {
            boolean[] zArr = xmlNameStartChars;
            boolean z = ((97 > i || i > 122) && ((65 > i || i > 90) && i != 58 && i != 95 && (192 > i || i > 214))) ? 216 <= i && i <= 246 : true;
            zArr[i] = z;
            zArr = xmlNameChars;
            z = ((97 > i || i > 122) && ((65 > i || i > 90) && !((48 <= i && i <= 57) || i == 58 || i == 95 || i == 45 || i == 46 || i == 183 || (192 <= i && i <= 214)))) ? 216 <= i && i <= 246 : true;
            zArr[i] = z;
            i = (char) (i + 1);
        }
    }

    static boolean isControlChar(char c) {
        return ((c > '\u001f' && c != '') || c == '\t' || c == '\n' || c == '\r') ? false : true;
    }

    private static boolean isNameChar(char c) {
        return c <= 'ÿ' ? xmlNameChars[c] : true;
    }

    private static boolean isNameStartChar(char c) {
        return c <= 'ÿ' ? xmlNameStartChars[c] : true;
    }

    public static boolean isXMLName(String str) {
        if (str.length() > 0 && !isNameStartChar(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            if (!isNameChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isXMLNameNS(String str) {
        if (str.length() > 0 && (!isNameStartChar(str.charAt(0)) || str.charAt(0) == ':')) {
            return false;
        }
        int i = 1;
        while (i < str.length()) {
            if (!isNameChar(str.charAt(i)) || str.charAt(i) == ':') {
                return false;
            }
            i++;
        }
        return true;
    }

    public static String normalizeLangValue(String str) {
        if ("x-default".equals(str)) {
            return str;
        }
        int i = 1;
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < str.length(); i2++) {
            switch (str.charAt(i2)) {
                case ' ':
                    break;
                case '-':
                case '_':
                    stringBuffer.append('-');
                    i++;
                    break;
                default:
                    if (i == 2) {
                        stringBuffer.append(Character.toUpperCase(str.charAt(i2)));
                        break;
                    }
                    stringBuffer.append(Character.toLowerCase(str.charAt(i2)));
                    break;
            }
        }
        return stringBuffer.toString();
    }

    static String removeControlChars(String str) {
        StringBuffer stringBuffer = new StringBuffer(str);
        for (int i = 0; i < stringBuffer.length(); i++) {
            if (isControlChar(stringBuffer.charAt(i))) {
                stringBuffer.setCharAt(i, ' ');
            }
        }
        return stringBuffer.toString();
    }

    static String[] splitNameAndValue(String str) {
        int indexOf = str.indexOf(61);
        int i = 1;
        if (str.charAt(1) == '?') {
            i = 2;
        }
        String substring = str.substring(i, indexOf);
        i = indexOf + 1;
        char charAt = str.charAt(i);
        i++;
        int length = str.length() - 2;
        StringBuffer stringBuffer = new StringBuffer(length - indexOf);
        while (i < length) {
            stringBuffer.append(str.charAt(i));
            i++;
            if (str.charAt(i) == charAt) {
                i++;
            }
        }
        return new String[]{substring, stringBuffer.toString()};
    }
}
