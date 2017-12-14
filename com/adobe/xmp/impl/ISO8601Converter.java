package com.adobe.xmp.impl;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.SimpleTimeZone;

public final class ISO8601Converter {
    private ISO8601Converter() {
    }

    public static XMPDateTime parse(String str) throws XMPException {
        return parse(str, new XMPDateTimeImpl());
    }

    public static XMPDateTime parse(String str, XMPDateTime xMPDateTime) throws XMPException {
        int gatherInt;
        ParameterAsserts.assertNotNull(str);
        ParseState parseState = new ParseState(str);
        Object obj = parseState.ch(0) != 'T' ? (parseState.length() < 2 || parseState.ch(1) != ':') ? (parseState.length() < 3 || parseState.ch(2) != ':') ? null : 1 : 1 : 1;
        if (obj == null) {
            if (parseState.ch(0) == '-') {
                parseState.skip();
            }
            gatherInt = parseState.gatherInt("Invalid year in date string", 9999);
            if (!parseState.hasNext() || parseState.ch() == '-') {
                if (parseState.ch(0) == '-') {
                    gatherInt = -gatherInt;
                }
                xMPDateTime.setYear(gatherInt);
                if (!parseState.hasNext()) {
                    return xMPDateTime;
                }
                parseState.skip();
                gatherInt = parseState.gatherInt("Invalid month in date string", 12);
                if (!parseState.hasNext() || parseState.ch() == '-') {
                    xMPDateTime.setMonth(gatherInt);
                    if (!parseState.hasNext()) {
                        return xMPDateTime;
                    }
                    parseState.skip();
                    gatherInt = parseState.gatherInt("Invalid day in date string", 31);
                    if (!parseState.hasNext() || parseState.ch() == 'T') {
                        xMPDateTime.setDay(gatherInt);
                        if (!parseState.hasNext()) {
                            return xMPDateTime;
                        }
                    }
                    throw new XMPException("Invalid date string, after day", 5);
                }
                throw new XMPException("Invalid date string, after month", 5);
            }
            throw new XMPException("Invalid date string, after year", 5);
        }
        xMPDateTime.setMonth(1);
        xMPDateTime.setDay(1);
        if (parseState.ch() == 'T') {
            parseState.skip();
        } else if (obj == null) {
            throw new XMPException("Invalid date string, missing 'T' after date", 5);
        }
        gatherInt = parseState.gatherInt("Invalid hour in date string", 23);
        if (parseState.ch() != ':') {
            throw new XMPException("Invalid date string, after hour", 5);
        }
        xMPDateTime.setHour(gatherInt);
        parseState.skip();
        gatherInt = parseState.gatherInt("Invalid minute in date string", 59);
        if (!parseState.hasNext() || parseState.ch() == ':' || parseState.ch() == 'Z' || parseState.ch() == '+' || parseState.ch() == '-') {
            xMPDateTime.setMinute(gatherInt);
            if (parseState.ch() == ':') {
                parseState.skip();
                gatherInt = parseState.gatherInt("Invalid whole seconds in date string", 59);
                if (!parseState.hasNext() || parseState.ch() == '.' || parseState.ch() == 'Z' || parseState.ch() == '+' || parseState.ch() == '-') {
                    xMPDateTime.setSecond(gatherInt);
                    if (parseState.ch() == '.') {
                        parseState.skip();
                        int pos = parseState.pos();
                        gatherInt = parseState.gatherInt("Invalid fractional seconds in date string", 999999999);
                        if (parseState.ch() == 'Z' || parseState.ch() == '+' || parseState.ch() == '-') {
                            pos = parseState.pos() - pos;
                            while (pos > 9) {
                                gatherInt /= 10;
                                pos--;
                            }
                            while (pos < 9) {
                                gatherInt *= 10;
                                pos++;
                            }
                            xMPDateTime.setNanoSecond(gatherInt);
                        } else {
                            throw new XMPException("Invalid date string, after fractional second", 5);
                        }
                    }
                }
                throw new XMPException("Invalid date string, after whole seconds", 5);
            }
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            if (parseState.ch() == 'Z') {
                parseState.skip();
            } else if (parseState.hasNext()) {
                if (parseState.ch() == '+') {
                    i = 1;
                } else if (parseState.ch() == '-') {
                    i = -1;
                } else {
                    throw new XMPException("Time zone must begin with 'Z', '+', or '-'", 5);
                }
                parseState.skip();
                i2 = parseState.gatherInt("Invalid time zone hour in date string", 23);
                if (parseState.ch() != ':') {
                    throw new XMPException("Invalid date string, after time zone hour", 5);
                }
                parseState.skip();
                i3 = parseState.gatherInt("Invalid time zone minute in date string", 59);
            }
            xMPDateTime.setTimeZone(new SimpleTimeZone((((i2 * 3600) * 1000) + ((i3 * 60) * 1000)) * i, ""));
            if (!parseState.hasNext()) {
                return xMPDateTime;
            }
            throw new XMPException("Invalid date string, extra chars at end", 5);
        }
        throw new XMPException("Invalid date string, after minute", 5);
    }

    public static String render(XMPDateTime xMPDateTime) {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat decimalFormat = new DecimalFormat("0000", new DecimalFormatSymbols(Locale.ENGLISH));
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getYear()));
        if (xMPDateTime.getMonth() == 0) {
            return stringBuffer.toString();
        }
        decimalFormat.applyPattern("'-'00");
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getMonth()));
        if (xMPDateTime.getDay() == 0) {
            return stringBuffer.toString();
        }
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getDay()));
        if (xMPDateTime.getHour() == 0 && xMPDateTime.getMinute() == 0 && xMPDateTime.getSecond() == 0 && xMPDateTime.getNanoSecond() == 0) {
            if (!(xMPDateTime.getTimeZone() == null || xMPDateTime.getTimeZone().getRawOffset() == 0)) {
            }
            return stringBuffer.toString();
        }
        stringBuffer.append('T');
        decimalFormat.applyPattern("00");
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getHour()));
        stringBuffer.append(':');
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getMinute()));
        if (!(xMPDateTime.getSecond() == 0 && xMPDateTime.getNanoSecond() == 0)) {
            double second = ((double) xMPDateTime.getSecond()) + (((double) xMPDateTime.getNanoSecond()) / 1.0E9d);
            decimalFormat.applyPattern(":00.#########");
            stringBuffer.append(decimalFormat.format(second));
        }
        if (xMPDateTime.getTimeZone() != null) {
            int offset = xMPDateTime.getTimeZone().getOffset(xMPDateTime.getCalendar().getTimeInMillis());
            if (offset == 0) {
                stringBuffer.append('Z');
            } else {
                int i = offset / 3600000;
                int abs = Math.abs((offset % 3600000) / 60000);
                decimalFormat.applyPattern("+00;-00");
                stringBuffer.append(decimalFormat.format((long) i));
                decimalFormat.applyPattern(":00");
                stringBuffer.append(decimalFormat.format((long) abs));
            }
        }
        return stringBuffer.toString();
    }
}
