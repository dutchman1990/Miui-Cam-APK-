package com.adobe.xmp.impl.xpath;

import java.util.ArrayList;
import java.util.List;

public class XMPPath {
    private List segments = new ArrayList(5);

    public void add(XMPPathSegment xMPPathSegment) {
        this.segments.add(xMPPathSegment);
    }

    public XMPPathSegment getSegment(int i) {
        return (XMPPathSegment) this.segments.get(i);
    }

    public int size() {
        return this.segments.size();
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 1; i < size(); i++) {
            stringBuffer.append(getSegment(i));
            if (i < size() - 1) {
                int kind = getSegment(i + 1).getKind();
                if (kind == 1 || kind == 2) {
                    stringBuffer.append('/');
                }
            }
        }
        return stringBuffer.toString();
    }
}
