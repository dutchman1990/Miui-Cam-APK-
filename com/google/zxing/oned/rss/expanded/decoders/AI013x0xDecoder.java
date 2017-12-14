package com.google.zxing.oned.rss.expanded.decoders;

import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;

abstract class AI013x0xDecoder extends AI01weightDecoder {
    AI013x0xDecoder(BitArray bitArray) {
        super(bitArray);
    }

    public String parseInformation() throws NotFoundException {
        if (getInformation().getSize() == 60) {
            StringBuilder stringBuilder = new StringBuilder();
            encodeCompressedGtin(stringBuilder, 5);
            encodeCompressedWeight(stringBuilder, 45, 15);
            return stringBuilder.toString();
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
