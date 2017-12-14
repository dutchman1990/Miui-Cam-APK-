package com.adobe.xmp.impl;

import com.adobe.xmp.XMPException;

/* compiled from: ISO8601Converter */
class ParseState {
    private int pos = 0;
    private String str;

    public ParseState(String str) {
        this.str = str;
    }

    public char ch() {
        return this.pos < this.str.length() ? this.str.charAt(this.pos) : '\u0000';
    }

    public char ch(int i) {
        return i < this.str.length() ? this.str.charAt(i) : '\u0000';
    }

    public int gatherInt(String str, int i) throws XMPException {
        int i2 = 0;
        Object obj = null;
        int ch = ch(this.pos);
        while (48 <= ch && ch <= 57) {
            i2 = (i2 * 10) + (ch - 48);
            obj = 1;
            this.pos++;
            ch = ch(this.pos);
        }
        if (obj != null) {
            return i2 > i ? i : i2 < 0 ? 0 : i2;
        } else {
            throw new XMPException(str, 5);
        }
    }

    public boolean hasNext() {
        return this.pos < this.str.length();
    }

    public int length() {
        return this.str.length();
    }

    public int pos() {
        return this.pos;
    }

    public void skip() {
        this.pos++;
    }
}
