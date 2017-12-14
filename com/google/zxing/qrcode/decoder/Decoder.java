package com.google.zxing.qrcode.decoder;

import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import java.util.Map;

public final class Decoder {
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);

    private void correctErrors(byte[] bArr, int i) throws ChecksumException {
        int i2;
        int length = bArr.length;
        int[] iArr = new int[length];
        for (i2 = 0; i2 < length; i2++) {
            iArr[i2] = bArr[i2] & 255;
        }
        try {
            this.rsDecoder.decode(iArr, bArr.length - i);
            for (i2 = 0; i2 < i; i2++) {
                bArr[i2] = (byte) ((byte) iArr[i2]);
            }
        } catch (ReedSolomonException e) {
            throw ChecksumException.getChecksumInstance();
        }
    }

    private DecoderResult decode(BitMatrixParser bitMatrixParser, Map<DecodeHintType, ?> map) throws FormatException, ChecksumException {
        DataBlock dataBlock;
        Version readVersion = bitMatrixParser.readVersion();
        ErrorCorrectionLevel errorCorrectionLevel = bitMatrixParser.readFormatInformation().getErrorCorrectionLevel();
        DataBlock[] dataBlocks = DataBlock.getDataBlocks(bitMatrixParser.readCodewords(), readVersion, errorCorrectionLevel);
        int i = 0;
        for (DataBlock dataBlock2 : dataBlocks) {
            i += dataBlock2.getNumDataCodewords();
        }
        byte[] bArr = new byte[i];
        int i2 = 0;
        int length = dataBlocks.length;
        int i3 = 0;
        while (i3 < length) {
            dataBlock2 = dataBlocks[i3];
            byte[] codewords = dataBlock2.getCodewords();
            int numDataCodewords = dataBlock2.getNumDataCodewords();
            correctErrors(codewords, numDataCodewords);
            int i4 = 0;
            int i5 = i2;
            while (i4 < numDataCodewords) {
                i2 = i5 + 1;
                bArr[i5] = (byte) codewords[i4];
                i4++;
                i5 = i2;
            }
            i3++;
            i2 = i5;
        }
        return DecodedBitStreamParser.decode(bArr, readVersion, errorCorrectionLevel, map);
    }

    public DecoderResult decode(BitMatrix bitMatrix, Map<DecodeHintType, ?> map) throws FormatException, ChecksumException {
        DecoderResult decode;
        BitMatrixParser bitMatrixParser = new BitMatrixParser(bitMatrix);
        FormatException formatException = null;
        ChecksumException checksumException = null;
        try {
            return decode(bitMatrixParser, (Map) map);
        } catch (FormatException e) {
            formatException = e;
            try {
                bitMatrixParser.remask();
                bitMatrixParser.setMirror(true);
                bitMatrixParser.readVersion();
                bitMatrixParser.readFormatInformation();
                bitMatrixParser.mirror();
                decode = decode(bitMatrixParser, (Map) map);
                decode.setOther(new QRCodeDecoderMetaData(true));
                return decode;
            } catch (FormatException e2) {
                if (formatException != null) {
                    throw formatException;
                } else if (checksumException == null) {
                    throw e2;
                } else {
                    throw checksumException;
                }
            } catch (ChecksumException e3) {
                if (formatException != null) {
                    throw formatException;
                } else if (checksumException == null) {
                    throw e3;
                } else {
                    throw checksumException;
                }
            }
        } catch (ChecksumException e32) {
            checksumException = e32;
            bitMatrixParser.remask();
            bitMatrixParser.setMirror(true);
            bitMatrixParser.readVersion();
            bitMatrixParser.readFormatInformation();
            bitMatrixParser.mirror();
            decode = decode(bitMatrixParser, (Map) map);
            decode.setOther(new QRCodeDecoderMetaData(true));
            return decode;
        }
    }
}
