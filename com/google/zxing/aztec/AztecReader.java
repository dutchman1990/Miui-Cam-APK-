package com.google.zxing.aztec;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.aztec.decoder.Decoder;
import com.google.zxing.aztec.detector.Detector;
import com.google.zxing.common.DecoderResult;
import java.util.List;
import java.util.Map;

public final class AztecReader implements Reader {
    public Result decode(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        NotFoundException notFoundException = null;
        FormatException formatException = null;
        Detector detector = new Detector(binaryBitmap.getBlackMatrix());
        ResultPoint[] resultPointArr = null;
        DecoderResult decoderResult = null;
        try {
            AztecDetectorResult detect = detector.detect(false);
            resultPointArr = detect.getPoints();
            decoderResult = new Decoder().decode(detect);
        } catch (NotFoundException e) {
            notFoundException = e;
        } catch (FormatException e2) {
            formatException = e2;
        }
        if (decoderResult == null) {
            try {
                detect = detector.detect(true);
                resultPointArr = detect.getPoints();
                decoderResult = new Decoder().decode(detect);
            } catch (Exception e3) {
                if (notFoundException != null) {
                    throw notFoundException;
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            } catch (Exception e42) {
                e42.printStackTrace();
            }
        }
        if (map != null) {
            ResultPointCallback resultPointCallback = (ResultPointCallback) map.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
            if (resultPointCallback != null) {
                for (ResultPoint foundPossibleResultPoint : resultPointArr) {
                    resultPointCallback.foundPossibleResultPoint(foundPossibleResultPoint);
                }
            }
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), resultPointArr, BarcodeFormat.AZTEC);
        List byteSegments = decoderResult.getByteSegments();
        if (byteSegments != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
        }
        String eCLevel = decoderResult.getECLevel();
        if (eCLevel != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, eCLevel);
        }
        return result;
        if (formatException == null) {
            throw e3;
        } else {
            throw formatException;
        }
    }

    public void reset() {
    }
}
