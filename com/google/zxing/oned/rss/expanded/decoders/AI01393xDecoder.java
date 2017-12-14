package com.google.zxing.oned.rss.expanded.decoders;

import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;

final class AI01393xDecoder extends AI01decoder {
    AI01393xDecoder(BitArray bitArray) {
        super(bitArray);
    }

    public String parseInformation() throws NotFoundException, FormatException {
        if (getInformation().getSize() >= 48) {
            StringBuilder stringBuilder = new StringBuilder();
            encodeCompressedGtin(stringBuilder, 8);
            int extractNumericValueFromBitArray = getGeneralDecoder().extractNumericValueFromBitArray(48, 2);
            stringBuilder.append("(393");
            stringBuilder.append(extractNumericValueFromBitArray);
            stringBuilder.append(')');
            int extractNumericValueFromBitArray2 = getGeneralDecoder().extractNumericValueFromBitArray(50, 10);
            if (extractNumericValueFromBitArray2 / 100 == 0) {
                stringBuilder.append('0');
            }
            if (extractNumericValueFromBitArray2 / 10 == 0) {
                stringBuilder.append('0');
            }
            stringBuilder.append(extractNumericValueFromBitArray2);
            stringBuilder.append(getGeneralDecoder().decodeGeneralPurposeField(60, null).getNewString());
            return stringBuilder.toString();
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
