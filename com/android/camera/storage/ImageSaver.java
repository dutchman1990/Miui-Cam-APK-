package com.android.camera.storage;

import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import com.android.camera.ActivityBase;
import com.android.camera.Device;
import com.android.camera.Log;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.google.PhotosSpecialTypesProvider;
import com.android.camera.google.ProcessingMediaManager;
import com.android.camera.storage.Storage.StorageListener;
import com.android.gallery3d.exif.ExifInterface;
import java.util.ArrayList;

public class ImageSaver extends Thread {
    private ActivityBase mActivity;
    private Handler mHandler;
    private int mHostState;
    private boolean mIsImageCaptureIntent;
    private Uri mLastImageUri;
    private MemoryManager mMemoryManager;
    private Thumbnail mPendingThumbnail;
    private ArrayList<SaveRequest> mQueue;
    private boolean mShouldStop;
    private boolean mStop;
    private Runnable mUpdateThumbnail = new C01271();
    private Object mUpdateThumbnailLock = new Object();

    class C01271 implements Runnable {
        C01271() {
        }

        public void run() {
            ImageSaver.this.updateThumbnail();
            ImageSaver.this.mActivity.getScreenHint().updateHint();
        }
    }

    private class MemoryManager implements StorageListener {
        private long mMaxMemory;
        private int mMaxTotalMemory;
        private Runtime mRuntime;
        private int mSaveTaskMemoryLimit;
        private int mSavedQueueMemoryLimit;
        private int mSaverMemoryUse;

        private MemoryManager() {
            this.mRuntime = Runtime.getRuntime();
        }

        private void addUsedMemory(int i) {
            this.mSaverMemoryUse += i;
        }

        private long getBaseMemory() {
            switch (Util.sWindowWidth) {
                case 720:
                    return 20971520;
                case 1080:
                    return 41943040;
                case 1440:
                    return 62914560;
                default:
                    return this.mRuntime.totalMemory() - this.mRuntime.freeMemory();
            }
        }

        private int getBurstDelay() {
            int i = 0;
            if (isNeedSlowDown()) {
                i = this.mSaverMemoryUse >= (this.mSaveTaskMemoryLimit * 7) / 8 ? 8 : this.mSaverMemoryUse >= (this.mSaveTaskMemoryLimit * 5) / 6 ? 5 : this.mSaverMemoryUse >= (this.mSaveTaskMemoryLimit * 4) / 5 ? 4 : this.mSaverMemoryUse >= (this.mSaveTaskMemoryLimit * 3) / 4 ? 3 : 1;
            }
            log("getBurstDelay: delayMultiple=" + i);
            return i * 100;
        }

        private int getTotalUsedMemory() {
            long totalMemory = this.mRuntime.totalMemory();
            long freeMemory = this.mRuntime.freeMemory();
            long j = totalMemory - freeMemory;
            log("getLeftMemory: maxMemory=" + this.mMaxMemory + ", total=" + totalMemory + ", free=" + freeMemory + ", totalUsed=" + j);
            return (int) j;
        }

        private void initLimit() {
            long baseMemory = this.mMaxMemory - getBaseMemory();
            if (Storage.isUsePhoneStorage()) {
                this.mSaveTaskMemoryLimit = (int) (((float) baseMemory) * 0.6f);
            } else {
                this.mSaveTaskMemoryLimit = (int) (((float) baseMemory) * 0.5f);
                if (62914560 < this.mSaveTaskMemoryLimit) {
                    this.mSaveTaskMemoryLimit = 62914560;
                }
            }
            this.mSavedQueueMemoryLimit = (int) (((float) this.mSaveTaskMemoryLimit) * 1.3f);
        }

        private void initMemory() {
            this.mMaxMemory = this.mRuntime.maxMemory();
            this.mMaxTotalMemory = (int) (((float) this.mMaxMemory) * 0.95f);
            this.mSaverMemoryUse = 0;
            initLimit();
            Storage.setStorageListener(this);
            Log.m0d("CameraMemoryManager", "initMemory: maxMemory=" + this.mMaxMemory);
        }

        private boolean isNeedSlowDown() {
            boolean z = Device.isMTKPlatform() ? this.mSaverMemoryUse >= (this.mSaveTaskMemoryLimit * 3) / 4 : this.mSaverMemoryUse >= this.mSaveTaskMemoryLimit / 2;
            log("isNeedSlowDown: return " + z + " mSaverMemoryUse=" + this.mSaverMemoryUse + " mSaveTaskMemoryLimit=" + this.mSaveTaskMemoryLimit);
            return z;
        }

        private boolean isNeedStopCapture() {
            if (!isReachedMemoryLimit() && this.mMaxTotalMemory > getTotalUsedMemory()) {
                if (Storage.getLeftSpace() > ((long) this.mSaverMemoryUse)) {
                    return false;
                }
            }
            Log.m0d("CameraMemoryManager", "isNeedStopCapture: needStop=" + true);
            return true;
        }

        private boolean isReachedMemoryLimit() {
            log("isReachedMemoryLimit: usedMemory=" + this.mSaverMemoryUse);
            return this.mSaverMemoryUse >= this.mSaveTaskMemoryLimit;
        }

        private boolean isSaveQueueFull() {
            log("isSaveQueueFull: usedMemory=" + this.mSaverMemoryUse);
            return this.mSaverMemoryUse >= this.mSavedQueueMemoryLimit;
        }

        private void log(String str) {
            if (Util.sIsDumpLog) {
                Log.m5v("CameraMemoryManager", str);
            }
        }

        private void reduceUsedMemory(int i) {
            this.mSaverMemoryUse -= i;
        }

        public void onStoragePathChanged() {
            initMemory();
        }
    }

    public static class SaveRequest {
        public byte[] data;
        public long date;
        public ExifInterface exif;
        public boolean finalImage;
        public int height;
        public boolean isHide;
        public boolean isMap;
        public boolean isPortrait;
        public Location loc;
        public String oldTitle;
        public int orientation;
        public String title;
        public Uri uri;
        public int width;
    }

    public ImageSaver(ActivityBase activityBase, Handler handler, boolean z) {
        this.mActivity = activityBase;
        this.mHandler = handler;
        this.mIsImageCaptureIntent = z;
        this.mQueue = new ArrayList();
        this.mMemoryManager = new MemoryManager();
        start();
    }

    private boolean isLastImageForThumbnail() {
        int i = 0;
        while (i < this.mQueue.size()) {
            if (i > 0 && ((SaveRequest) this.mQueue.get(i)).finalImage) {
                return false;
            }
            i++;
        }
        return true;
    }

    private void storeImage(byte[] bArr, Uri uri, String str, long j, Location location, int i, int i2, ExifInterface exifInterface, int i3, boolean z, boolean z2, boolean z3, String str2, boolean z4) {
        if (uri != null) {
            Storage.updateImage(this.mActivity, bArr, exifInterface, uri, str, location, i3, i, i2, str2, z4);
        } else if (bArr != null) {
            uri = Storage.addImage(this.mActivity, str, j, location, i3, bArr, i, i2, false, z, z2, false, false, z4);
        }
        if (z4) {
            PhotosSpecialTypesProvider.markPortraitSpecialType(this.mActivity, uri);
        }
        ProcessingMediaManager.instance().removeProcessingMedia(this.mActivity, uri);
        Storage.getAvailableSpace();
        if (uri != null) {
            boolean z5;
            synchronized (this) {
                z5 = (this.mHostState == 0 && isLastImageForThumbnail() && !this.mIsImageCaptureIntent) ? z3 : false;
            }
            if (z5) {
                Thumbnail createThumbnailFromUri = z2 ? Thumbnail.createThumbnailFromUri(this.mActivity.getContentResolver(), uri, false) : Thumbnail.createThumbnail(bArr, i3, Integer.highestOneBit((int) Math.ceil(Math.max((double) i, (double) i2) / 512.0d)), uri, false);
                synchronized (this.mUpdateThumbnailLock) {
                    this.mPendingThumbnail = createThumbnailFromUri;
                    this.mHandler.post(this.mUpdateThumbnail);
                }
            }
            synchronized (this) {
                if (!this.mIsImageCaptureIntent) {
                    Util.broadcastNewPicture(this.mActivity, uri);
                    this.mLastImageUri = uri;
                    if (z3) {
                        this.mActivity.addSecureUri(uri);
                    }
                }
            }
        }
    }

    private void updateThumbnail() {
        synchronized (this.mUpdateThumbnailLock) {
            this.mHandler.removeCallbacks(this.mUpdateThumbnail);
            Thumbnail thumbnail = this.mPendingThumbnail;
            this.mPendingThumbnail = null;
        }
        if (thumbnail != null) {
            this.mActivity.getThumbnailUpdater().setThumbnail(thumbnail);
        }
    }

    public void addImage(SaveRequest saveRequest) {
        synchronized (this) {
            if (2 == this.mHostState) {
                Log.m5v("ImageSaver", "addImage: host is being destroyed.");
                return;
            }
            if (this.mMemoryManager.isSaveQueueFull()) {
                this.mShouldStop = true;
            }
            if (saveRequest.data != null) {
                this.mMemoryManager.addUsedMemory(saveRequest.data.length);
            }
            this.mQueue.add(saveRequest);
            notifyAll();
        }
    }

    public void addImage(byte[] bArr, String str, long j, Uri uri, Location location, int i, int i2, ExifInterface exifInterface, int i3, boolean z, boolean z2, boolean z3) {
        addImage(bArr, str, null, j, uri, location, i, i2, exifInterface, i3, z, z2, z3, false);
    }

    public void addImage(byte[] bArr, String str, String str2, long j, Uri uri, Location location, int i, int i2, ExifInterface exifInterface, int i3, boolean z, boolean z2, boolean z3, boolean z4) {
        SaveRequest saveRequest = new SaveRequest();
        saveRequest.data = bArr;
        saveRequest.date = j;
        saveRequest.uri = uri;
        saveRequest.title = str;
        saveRequest.oldTitle = str2;
        saveRequest.loc = location == null ? null : new Location(location);
        saveRequest.width = i;
        saveRequest.height = i2;
        saveRequest.exif = exifInterface;
        saveRequest.orientation = i3;
        saveRequest.isHide = z;
        saveRequest.isMap = z2;
        saveRequest.finalImage = z3;
        saveRequest.isPortrait = z4;
        addImage(saveRequest);
    }

    public int getBurstDelay() {
        return this.mMemoryManager.getBurstDelay();
    }

    public float getSuitableBurstShotSpeed() {
        return 0.66f;
    }

    public boolean isNeedSlowDown() {
        return this.mMemoryManager.isNeedSlowDown();
    }

    public boolean isNeedStopCapture() {
        return this.mMemoryManager.isNeedStopCapture();
    }

    public void onHostDestroy() {
        synchronized (this) {
            this.mHostState = 2;
            this.mStop = true;
            notifyAll();
        }
        synchronized (this.mUpdateThumbnailLock) {
            this.mHandler.removeCallbacks(this.mUpdateThumbnail);
            this.mPendingThumbnail = null;
        }
        Log.m5v("ImageSaver", "onHostDestroy");
    }

    public void onHostPause() {
        synchronized (this) {
            this.mHostState = 1;
            boolean isEmpty = this.mQueue.isEmpty();
        }
        synchronized (this.mUpdateThumbnailLock) {
            this.mHandler.removeCallbacks(this.mUpdateThumbnail);
            this.mPendingThumbnail = null;
        }
        if (!isEmpty) {
            this.mActivity.getThumbnailUpdater().setThumbnail(null, false);
        }
        Log.m5v("ImageSaver", "onHostPause");
    }

    public void onHostResume(boolean z) {
        synchronized (this) {
            this.mIsImageCaptureIntent = z;
            this.mHostState = 0;
            Log.m5v("ImageSaver", "onHostResume: isCapture=" + this.mIsImageCaptureIntent);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
        r20 = this;
        r0 = r20;
        r2 = r0.mMemoryManager;
        r2.initMemory();
    L_0x0007:
        monitor-enter(r20);
        r0 = r20;
        r2 = r0.mQueue;	 Catch:{ all -> 0x00ba }
        r2 = r2.isEmpty();	 Catch:{ all -> 0x00ba }
        if (r2 == 0) goto L_0x002a;
    L_0x0012:
        r0 = r20;
        r2 = r0.mStop;	 Catch:{ all -> 0x00ba }
        if (r2 == 0) goto L_0x0023;
    L_0x0018:
        r2 = "ImageSaver";
        r3 = "run: exiting";
        com.android.camera.Log.m0d(r2, r3);	 Catch:{ all -> 0x00ba }
        monitor-exit(r20);
        return;
    L_0x0023:
        r20.wait();	 Catch:{ InterruptedException -> 0x0028 }
    L_0x0026:
        monitor-exit(r20);
        goto L_0x0007;
    L_0x0028:
        r18 = move-exception;
        goto L_0x0026;
    L_0x002a:
        r0 = r20;
        r2 = r0.mQueue;	 Catch:{ all -> 0x00ba }
        r3 = 0;
        r19 = r2.get(r3);	 Catch:{ all -> 0x00ba }
        r19 = (com.android.camera.storage.ImageSaver.SaveRequest) r19;	 Catch:{ all -> 0x00ba }
        monitor-exit(r20);
        r0 = r19;
        r2 = r0.oldTitle;
        if (r2 == 0) goto L_0x004a;
    L_0x003c:
        r0 = r19;
        r2 = r0.uri;
        if (r2 != 0) goto L_0x004a;
    L_0x0042:
        r0 = r20;
        r2 = r0.mLastImageUri;
        r0 = r19;
        r0.uri = r2;
    L_0x004a:
        r0 = r19;
        r3 = r0.data;
        r0 = r19;
        r4 = r0.uri;
        r0 = r19;
        r5 = r0.title;
        r0 = r19;
        r6 = r0.date;
        r0 = r19;
        r8 = r0.loc;
        r0 = r19;
        r9 = r0.width;
        r0 = r19;
        r10 = r0.height;
        r0 = r19;
        r11 = r0.exif;
        r0 = r19;
        r12 = r0.orientation;
        r0 = r19;
        r13 = r0.isHide;
        r0 = r19;
        r14 = r0.isMap;
        r0 = r19;
        r15 = r0.finalImage;
        r0 = r19;
        r0 = r0.oldTitle;
        r16 = r0;
        r0 = r19;
        r0 = r0.isPortrait;
        r17 = r0;
        r2 = r20;
        r2.storeImage(r3, r4, r5, r6, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17);
        monitor-enter(r20);
        r0 = r19;
        r2 = r0.data;	 Catch:{ all -> 0x00b7 }
        if (r2 == 0) goto L_0x009e;
    L_0x0092:
        r0 = r20;
        r2 = r0.mMemoryManager;	 Catch:{ all -> 0x00b7 }
        r0 = r19;
        r3 = r0.data;	 Catch:{ all -> 0x00b7 }
        r3 = r3.length;	 Catch:{ all -> 0x00b7 }
        r2.reduceUsedMemory(r3);	 Catch:{ all -> 0x00b7 }
    L_0x009e:
        r0 = r20;
        r2 = r0.mQueue;	 Catch:{ all -> 0x00b7 }
        r3 = 0;
        r2.remove(r3);	 Catch:{ all -> 0x00b7 }
        r0 = r20;
        r2 = r0.mMemoryManager;	 Catch:{ all -> 0x00b7 }
        r2 = r2.isSaveQueueFull();	 Catch:{ all -> 0x00b7 }
        if (r2 != 0) goto L_0x0026;
    L_0x00b0:
        r2 = 0;
        r0 = r20;
        r0.mShouldStop = r2;	 Catch:{ all -> 0x00b7 }
        goto L_0x0026;
    L_0x00b7:
        r2 = move-exception;
        monitor-exit(r20);
        throw r2;
    L_0x00ba:
        r2 = move-exception;
        monitor-exit(r20);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.storage.ImageSaver.run():void");
    }

    public boolean shouldStopShot() {
        return this.mShouldStop;
    }

    public void updateImage(String str, String str2) {
        SaveRequest saveRequest = new SaveRequest();
        saveRequest.title = str;
        saveRequest.oldTitle = str2;
        addImage(saveRequest);
    }
}
