package com.android.zxing;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraAppImpl;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.zxing.ui.QRCodeFragmentLayout;
import com.google.zxing.Result;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.WeakHashMap;

public class QRCodeManager implements OnClickListener {
    private static int CENTER_FRAME_WIDTH;
    private static final int DECODE_TOTAL_TIME = (Device.isLowPowerQRScan() ? 15000 : -1);
    private static int MAX_FRAME_HEIGHT;
    private static int MAX_FRAME_WIDTH;
    private static Rect mRectFinderCenter = new Rect(0, 0, 0, 0);
    private static Rect mRectFinderFocusArea = new Rect(0, 0, 0, 0);
    private static Rect mRectPreviewCenter = new Rect(0, 0, 0, 0);
    private static Rect mRectPreviewFocusArea = new Rect(0, 0, 0, 0);
    private static WeakHashMap<Context, QRCodeManager> sMap = new WeakHashMap();
    private Activity mActivity;
    private CameraProxy mCameraDevice;
    private boolean mDecode;
    private DecodeHandlerFactory mDecodeHandlerFactory;
    private Handler mHandler;
    private QRCodeManagerListener mListener;
    private boolean mNeedScan;
    private PreviewCallback mPreviewCallback = new C01781();
    private int mPreviewFormat = 17;
    private int mPreviewHeight;
    private int mPreviewLayoutHeight;
    private int mPreviewLayoutWidth;
    private int mPreviewWidth;
    private String mResult;
    private long mResumeTime = -1;
    private boolean mUIInitialized;
    private TextView mViewFinderButton;
    private ViewGroup mViewFinderFrame;

    public interface QRCodeManagerListener {
        void findQRCode();

        boolean scanQRCodeEnabled();
    }

    class C01781 implements PreviewCallback {
        C01781() {
        }

        public void onPreviewFrame(byte[] bArr, Camera camera) {
            if (QRCodeManager.this.mDecodeHandlerFactory != null && bArr != null) {
                QRCodeManager.this.mDecodeHandlerFactory.getHandler().removeMessages(C0049R.id.decode);
                QRCodeManager.this.mDecodeHandlerFactory.getHandler().obtainMessage(C0049R.id.decode, QRCodeManager.this.mPreviewHeight, QRCodeManager.this.mPreviewWidth, bArr).sendToTarget();
            }
        }
    }

    private class MyHander extends Handler {
        public MyHander(Looper looper) {
            super(looper);
        }

        public void dispatchMessage(Message message) {
            switch (message.what) {
                case C0049R.id.find_timeout:
                    if (QRCodeManager.this.mViewFinderFrame != null) {
                        QRCodeManager.this.mViewFinderFrame.setVisibility(8);
                        return;
                    }
                    return;
                case C0049R.id.find_qrcode:
                    QRCodeManager.this.mResult = QRCodeManager.recode(((Result) message.obj).getText());
                    if (!(QRCodeManager.this.mViewFinderFrame == null || QRCodeManager.this.mActivity == null || !QRCodeManager.this.scanQRCodeEnabled())) {
                        QRCodeManager.this.mViewFinderFrame.setVisibility(0);
                        QRCodeManager.this.mListener.findQRCode();
                    }
                    QRCodeManager.this.sendDecodeMessageSafe(4000);
                    return;
                case C0049R.id.try_decode_qrcode:
                    if (QRCodeManager.this.scanQRCodeEnabled()) {
                        QRCodeManager.this.mCameraDevice.setOneShotPreviewCallback(QRCodeManager.this.mPreviewCallback);
                    }
                    QRCodeManager.this.sendDecodeMessageSafe(1000);
                    return;
                case C0049R.id.decode_exit:
                    QRCodeManager.this.exitDecode();
                    Log.e("QRCodeManager", "exit decode qrcode for timeout, mResumeTime=" + QRCodeManager.this.mResumeTime + " now=" + System.currentTimeMillis() + " decodetime=" + QRCodeManager.DECODE_TOTAL_TIME);
                    return;
                default:
                    return;
            }
        }
    }

    static {
        MAX_FRAME_HEIGHT = 360;
        MAX_FRAME_WIDTH = 480;
        CENTER_FRAME_WIDTH = 720;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) CameraAppImpl.getAndroidContext().getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        CENTER_FRAME_WIDTH = displayMetrics.widthPixels;
        MAX_FRAME_HEIGHT = CENTER_FRAME_WIDTH;
        MAX_FRAME_WIDTH = CENTER_FRAME_WIDTH;
    }

    private QRCodeManager() {
    }

    private void exitDecode() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(C0049R.id.find_qrcode);
            this.mHandler.removeMessages(C0049R.id.try_decode_qrcode);
            this.mHandler.removeMessages(C0049R.id.find_timeout);
            this.mHandler.removeMessages(C0049R.id.decode_exit);
        }
        if (this.mDecodeHandlerFactory != null) {
            this.mDecodeHandlerFactory.quit();
        }
        this.mDecode = false;
        this.mDecodeHandlerFactory = null;
    }

    private boolean hide() {
        return false;
    }

    public static QRCodeManager instance(Context context) {
        QRCodeManager qRCodeManager = (QRCodeManager) sMap.get(context);
        if (qRCodeManager != null) {
            return qRCodeManager;
        }
        qRCodeManager = new QRCodeManager();
        sMap.put(context, qRCodeManager);
        return qRCodeManager;
    }

    public static String recode(String str) {
        String str2 = "";
        try {
            return Charset.forName("ISO-8859-1").newEncoder().canEncode(str) ? new String(str.getBytes("ISO-8859-1"), "GB2312") : str;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str2;
        }
    }

    public static void removeInstance(Context context) {
        QRCodeManager qRCodeManager = (QRCodeManager) sMap.remove(context);
        if (qRCodeManager != null) {
            qRCodeManager.onPause();
        }
    }

    private boolean scanQRCodeEnabled() {
        return (!this.mUIInitialized || this.mPreviewWidth == 0 || this.mPreviewLayoutWidth == 0 || this.mCameraDevice == null || this.mListener == null || !this.mListener.scanQRCodeEnabled() || this.mViewFinderFrame.getVisibility() == 0) ? false : true;
    }

    private void sendDecodeMessageSafe(int i) {
        if (this.mNeedScan && this.mUIInitialized && this.mDecode) {
            this.mHandler.removeMessages(C0049R.id.try_decode_qrcode);
            this.mHandler.sendEmptyMessageDelayed(C0049R.id.try_decode_qrcode, (long) i);
        }
    }

    private void show() {
        if (this.mUIInitialized) {
            this.mViewFinderFrame.setVisibility(8);
            try {
                ActivityBase activityBase = (ActivityBase) this.mActivity;
                activityBase.dismissKeyguard();
                Intent intent = new Intent("android.intent.action.receiverResultBarcodeScanner");
                intent.setPackage("com.xiaomi.scanner");
                intent.putExtra("result", this.mResult);
                activityBase.startActivity(intent);
                activityBase.setJumpFlag(3);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("QRCodeManager", "check if BarcodeScanner tool is installed or not");
            }
        }
    }

    private void startDecodeThreadIfNeeded() {
        if (this.mDecodeHandlerFactory == null) {
            this.mDecodeHandlerFactory = new DecodeHandlerFactory(this.mActivity, false);
            this.mDecodeHandlerFactory.start();
        }
    }

    private void updateRectInPreview() {
        if (this.mPreviewLayoutWidth != 0) {
            float f = ((float) this.mPreviewWidth) / ((float) this.mPreviewLayoutWidth);
            float f2 = ((float) this.mPreviewHeight) / ((float) this.mPreviewLayoutHeight);
            mRectPreviewFocusArea.set((int) (((float) mRectFinderFocusArea.left) * f), (int) (((float) mRectFinderFocusArea.top) * f2), (int) (((float) mRectFinderFocusArea.right) * f), (int) (((float) mRectFinderFocusArea.bottom) * f2));
            mRectPreviewCenter.set((int) (((float) mRectFinderCenter.left) * f), (int) (((float) mRectFinderCenter.top) * f2), (int) (((float) mRectFinderCenter.right) * f), (int) (((float) mRectFinderCenter.bottom) * f2));
        }
    }

    public YUVLuminanceSource buildLuminanceSource(byte[] bArr, int i, int i2, boolean z) {
        if (this.mPreviewFormat == 17 && scanQRCodeEnabled()) {
            if (z && !mRectPreviewCenter.isEmpty()) {
                return new YUVLuminanceSource(bArr, i, i2, mRectPreviewCenter.left, mRectPreviewCenter.top, mRectPreviewCenter.width(), mRectPreviewCenter.height());
            } else if (!(mRectPreviewFocusArea.isEmpty() || mRectPreviewCenter.contains(mRectPreviewFocusArea))) {
                return new YUVLuminanceSource(bArr, i, i2, mRectPreviewFocusArea.left, mRectPreviewFocusArea.top, mRectPreviewFocusArea.width(), mRectPreviewFocusArea.height());
            }
        }
        return null;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void hideViewFinderFrame() {
        if (this.mViewFinderFrame != null && this.mViewFinderFrame.getVisibility() == 0) {
            this.mViewFinderFrame.setVisibility(8);
        }
    }

    public void needScanQRCode(boolean z) {
        this.mNeedScan = this.mDecode ? z : false;
        if (this.mHandler != null) {
            if (z) {
                sendDecodeMessageSafe(1000);
            } else {
                this.mHandler.removeMessages(C0049R.id.try_decode_qrcode);
            }
        }
        if (this.mNeedScan) {
            startDecodeThreadIfNeeded();
        }
    }

    public boolean onBackPressed() {
        return hide();
    }

    public void onClick(View view) {
        AutoLockManager.getInstance(this.mActivity).onUserInteraction();
        CameraDataAnalytics.instance().trackEvent("qrcode_detected_times_key");
        show();
    }

    public void onCreate(Activity activity, Looper looper, QRCodeManagerListener qRCodeManagerListener) {
        this.mActivity = activity;
        this.mListener = qRCodeManagerListener;
        this.mHandler = new MyHander(looper);
        this.mViewFinderFrame = (QRCodeFragmentLayout) this.mActivity.findViewById(C0049R.id.qrcode_viewfinder_layout);
        this.mViewFinderButton = (TextView) this.mViewFinderFrame.findViewById(C0049R.id.qrcode_viewfinder_button);
        this.mViewFinderButton.setOnClickListener(this);
        this.mUIInitialized = true;
        resetQRScanExit(false);
    }

    public void onDestroy() {
        removeInstance(this.mActivity);
        this.mActivity = null;
        this.mHandler = null;
    }

    public void onPause() {
        if (this.mViewFinderFrame != null) {
            this.mViewFinderFrame.setVisibility(8);
        }
        exitDecode();
        this.mCameraDevice = null;
        this.mResult = null;
    }

    public void requestDecode() {
        if (this.mHandler != null) {
            sendDecodeMessageSafe(100);
        }
    }

    public void resetQRScanExit(boolean z) {
        if (z) {
            this.mResumeTime = System.currentTimeMillis();
        }
        long currentTimeMillis = System.currentTimeMillis();
        boolean z2 = (DECODE_TOTAL_TIME == -1 || this.mResumeTime == -1) ? true : !Util.isTimeout(currentTimeMillis, this.mResumeTime, (long) DECODE_TOTAL_TIME);
        this.mDecode = z2;
        if (!this.mDecode) {
            Log.e("QRCodeManager", "we should not decode qrcode, mResumeTime=" + this.mResumeTime + " now=" + currentTimeMillis + " resumetime=" + (currentTimeMillis - this.mResumeTime) + " decodetime=" + DECODE_TOTAL_TIME);
        }
        if (this.mDecode && this.mHandler != null && this.mResumeTime != -1 && DECODE_TOTAL_TIME != -1) {
            this.mHandler.removeMessages(C0049R.id.decode_exit);
            this.mHandler.sendEmptyMessageDelayed(C0049R.id.decode_exit, ((long) DECODE_TOTAL_TIME) - (currentTimeMillis - this.mResumeTime));
        }
    }

    public void setCameraDevice(CameraProxy cameraProxy) {
        this.mCameraDevice = cameraProxy;
    }

    public void setPreviewFormat(int i) {
        this.mPreviewFormat = i;
    }

    public void setPreviewLayoutSize(int i, int i2) {
        if (this.mPreviewLayoutWidth != i || this.mPreviewLayoutHeight != i2) {
            this.mPreviewLayoutWidth = i;
            this.mPreviewLayoutHeight = i2;
            updateViewFinderRect();
        }
    }

    public void setTransposePreviewSize(int i, int i2) {
        if (this.mPreviewWidth != i2 || this.mPreviewHeight != i) {
            this.mPreviewWidth = i2;
            this.mPreviewHeight = i;
            updateRectInPreview();
        }
    }

    public void updateViewFinderRect() {
        updateViewFinderRect(null);
    }

    public void updateViewFinderRect(Point point) {
        int min = Math.min(this.mPreviewLayoutWidth, CENTER_FRAME_WIDTH);
        int min2 = Math.min(this.mPreviewLayoutHeight, CENTER_FRAME_WIDTH);
        int i = (this.mPreviewLayoutWidth - min) / 2;
        int i2 = (this.mPreviewLayoutHeight - min2) / 2;
        mRectFinderCenter.set(i, i2, i + min, i2 + min2);
        if (point != null) {
            min = Math.min(this.mPreviewLayoutWidth, MAX_FRAME_WIDTH);
            min2 = Math.min(this.mPreviewLayoutHeight, MAX_FRAME_HEIGHT);
            mRectFinderFocusArea.set(Math.max(point.x - (min / 2), 0), Math.max(point.y - (min2 / 2), 0), Math.min(point.x + (min / 2), this.mPreviewLayoutWidth), Math.min(point.y + (min2 / 2), this.mPreviewLayoutHeight));
        } else {
            mRectFinderFocusArea.set(0, 0, 0, 0);
        }
        updateRectInPreview();
    }
}
