package com.android.zxing;

import android.content.Context;
import android.os.HandlerThread;
import android.support.v7.recyclerview.C0049R;
import com.google.zxing.DecodeHintType;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

final class DecodeHandlerFactory {
    private final Context mContext;
    private DecodeHandler mHandler;
    private final CountDownLatch mHandlerInitLatch = new CountDownLatch(1);
    private final Hashtable<DecodeHintType, Object> mHints = new Hashtable(1);

    public DecodeHandlerFactory(Context context, boolean z) {
        this.mContext = context;
        Vector vector = new Vector();
        vector.addAll(DecodeFormats.QR_CODE_FORMATS);
        if (z) {
            vector.addAll(DecodeFormats.ONE_D_FORMATS);
            vector.addAll(DecodeFormats.DATA_MATRIX_FORMATS);
        }
        this.mHints.put(DecodeHintType.POSSIBLE_FORMATS, vector);
    }

    public DecodeHandler getHandler() {
        try {
            this.mHandlerInitLatch.await();
        } catch (InterruptedException e) {
        }
        return this.mHandler;
    }

    public void quit() {
        getHandler().cancel();
        getHandler().removeMessages(C0049R.id.decode);
        getHandler().getLooper().quit();
    }

    public void start() {
        HandlerThread handlerThread = new HandlerThread("DecodeThread");
        handlerThread.start();
        this.mHandler = new DecodeHandler(this.mContext, handlerThread.getLooper(), this.mHints);
        this.mHandlerInitLatch.countDown();
    }
}
