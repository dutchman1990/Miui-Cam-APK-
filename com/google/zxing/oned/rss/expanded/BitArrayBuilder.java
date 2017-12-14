package com.google.zxing.oned.rss.expanded;

import com.google.zxing.common.BitArray;
import java.util.List;

final class BitArrayBuilder {
    private BitArrayBuilder() {
    }

    static BitArray buildBitArray(List<ExpandedPair> list) {
        int i;
        int size = (list.size() * 2) - 1;
        if (((ExpandedPair) list.get(list.size() - 1)).getRightChar() == null) {
            size--;
        }
        BitArray bitArray = new BitArray(size * 12);
        int i2 = 0;
        int value = ((ExpandedPair) list.get(0)).getRightChar().getValue();
        for (i = 11; i >= 0; i--) {
            if (((1 << i) & value) != 0) {
                bitArray.set(i2);
            }
            i2++;
        }
        for (i = 1; i < list.size(); i++) {
            int i3;
            ExpandedPair expandedPair = (ExpandedPair) list.get(i);
            int value2 = expandedPair.getLeftChar().getValue();
            for (i3 = 11; i3 >= 0; i3--) {
                if (((1 << i3) & value2) != 0) {
                    bitArray.set(i2);
                }
                i2++;
            }
            if (expandedPair.getRightChar() != null) {
                int value3 = expandedPair.getRightChar().getValue();
                for (i3 = 11; i3 >= 0; i3--) {
                    if (((1 << i3) & value3) != 0) {
                        bitArray.set(i2);
                    }
                    i2++;
                }
            }
        }
        return bitArray;
    }
}
