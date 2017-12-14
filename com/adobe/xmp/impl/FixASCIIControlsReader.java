package com.adobe.xmp.impl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class FixASCIIControlsReader extends PushbackReader {
    private int control = 0;
    private int digits = 0;
    private int state = 0;

    public FixASCIIControlsReader(Reader reader) {
        super(reader, 8);
    }

    private char processChar(char c) {
        switch (this.state) {
            case 0:
                if (c == '&') {
                    this.state = 1;
                }
                return c;
            case 1:
                if (c == '#') {
                    this.state = 2;
                } else {
                    this.state = 5;
                }
                return c;
            case 2:
                if (c == 'x') {
                    this.control = 0;
                    this.digits = 0;
                    this.state = 3;
                } else if ('0' > c || c > '9') {
                    this.state = 5;
                } else {
                    this.control = Character.digit(c, 10);
                    this.digits = 1;
                    this.state = 4;
                }
                return c;
            case 3:
                if (('0' <= c && c <= '9') || (('a' <= c && c <= 'f') || ('A' <= c && c <= 'F'))) {
                    this.control = (this.control * 16) + Character.digit(c, 16);
                    this.digits++;
                    if (this.digits <= 4) {
                        this.state = 3;
                    } else {
                        this.state = 5;
                    }
                } else if (c == ';' && Utils.isControlChar((char) this.control)) {
                    this.state = 0;
                    return (char) this.control;
                } else {
                    this.state = 5;
                }
                return c;
            case 4:
                if ('0' <= c && c <= '9') {
                    this.control = (this.control * 10) + Character.digit(c, 10);
                    this.digits++;
                    if (this.digits <= 5) {
                        this.state = 4;
                    } else {
                        this.state = 5;
                    }
                } else if (c == ';' && Utils.isControlChar((char) this.control)) {
                    this.state = 0;
                    return (char) this.control;
                } else {
                    this.state = 5;
                }
                return c;
            case 5:
                this.state = 0;
                return c;
            default:
                return c;
        }
    }

    public int read(char[] cArr, int i, int i2) throws IOException {
        int i3 = 0;
        int i4 = 0;
        char[] cArr2 = new char[8];
        int i5 = 1;
        int i6 = i;
        while (i5 != 0 && i4 < i2) {
            int i7;
            i5 = super.read(cArr2, i3, 1) == 1 ? 1 : 0;
            if (i5 != 0) {
                char processChar = processChar(cArr2[i3]);
                if (this.state == 0) {
                    if (Utils.isControlChar(processChar)) {
                        processChar = ' ';
                    }
                    i7 = i6 + 1;
                    cArr[i6] = processChar;
                    i3 = 0;
                    i4++;
                } else if (this.state == 5) {
                    unread(cArr2, 0, i3 + 1);
                    i3 = 0;
                    i7 = i6;
                } else {
                    i3++;
                    i7 = i6;
                }
            } else if (i3 > 0) {
                unread(cArr2, 0, i3);
                this.state = 5;
                i3 = 0;
                i5 = 1;
                i7 = i6;
            } else {
                i7 = i6;
            }
            i6 = i7;
        }
        return (i4 > 0 || i5 != 0) ? i4 : -1;
    }
}
