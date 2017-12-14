package com.android.camera;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import java.io.File;

public class ThumbnailUpdater {
    private ActivityBase mActivityBase;
    private ContentResolver mContentResolver = this.mActivityBase.getContentResolver();
    private AsyncTask<Void, Void, Thumbnail> mLoadThumbnailTask;
    private Thumbnail mThumbnail;

    private class LoadThumbnailTask extends AsyncTask<Void, Void, Thumbnail> {
        private boolean mLookAtCache;

        public LoadThumbnailTask(boolean z) {
            this.mLookAtCache = z;
        }

        protected Thumbnail doInBackground(Void... voidArr) {
            Thumbnail thumbnail = null;
            if (isCancelled()) {
                return null;
            }
            if (ThumbnailUpdater.this.mThumbnail != null) {
                Uri uri = ThumbnailUpdater.this.mThumbnail.getUri();
                if (Util.isUriValid(uri, ThumbnailUpdater.this.mContentResolver) && uri.equals(Thumbnail.getLastThumbnailUri(ThumbnailUpdater.this.mContentResolver))) {
                    return ThumbnailUpdater.this.mThumbnail;
                }
            }
            if ((!ThumbnailUpdater.this.mActivityBase.startFromSecureKeyguard() || ThumbnailUpdater.this.mActivityBase.getSecureUriList().size() > 0) && this.mLookAtCache) {
                thumbnail = Thumbnail.getLastThumbnailFromFile(ThumbnailUpdater.this.mActivityBase.getFilesDir(), ThumbnailUpdater.this.mContentResolver);
            }
            if (isCancelled()) {
                return null;
            }
            Uri uri2 = null;
            if (thumbnail != null) {
                uri2 = thumbnail.getUri();
            }
            Thumbnail[] thumbnailArr = new Thumbnail[1];
            switch (!ThumbnailUpdater.this.mActivityBase.startFromSecureKeyguard() ? Thumbnail.getLastThumbnailFromContentResolver(ThumbnailUpdater.this.mContentResolver, thumbnailArr, uri2) : Thumbnail.getLastThumbnailFromUriList(ThumbnailUpdater.this.mContentResolver, thumbnailArr, ThumbnailUpdater.this.mActivityBase.getSecureUriList(), uri2)) {
                case -1:
                    return thumbnail;
                case 0:
                    return null;
                case 1:
                    return thumbnailArr[0];
                case 2:
                    cancel(true);
                    return null;
                default:
                    return null;
            }
        }

        protected void onPostExecute(Thumbnail thumbnail) {
            if (!isCancelled()) {
                ThumbnailUpdater.this.mThumbnail = thumbnail;
                ThumbnailUpdater.this.updateThumbnailView();
            }
        }
    }

    private class SaveThumbnailTask extends AsyncTask<Thumbnail, Void, Void> {
        private SaveThumbnailTask() {
        }

        protected Void doInBackground(Thumbnail... thumbnailArr) {
            File filesDir = ThumbnailUpdater.this.mActivityBase.getFilesDir();
            for (Thumbnail saveLastThumbnailToFile : thumbnailArr) {
                saveLastThumbnailToFile.saveLastThumbnailToFile(filesDir);
            }
            return null;
        }
    }

    public ThumbnailUpdater(ActivityBase activityBase) {
        this.mActivityBase = activityBase;
    }

    public void cancelTask() {
        if (this.mLoadThumbnailTask != null) {
            this.mLoadThumbnailTask.cancel(true);
            this.mLoadThumbnailTask = null;
        }
    }

    public void getLastThumbnail() {
        if (this.mLoadThumbnailTask != null) {
            this.mLoadThumbnailTask.cancel(true);
        }
        this.mLoadThumbnailTask = new LoadThumbnailTask(true).execute(new Void[0]);
    }

    public void getLastThumbnailUncached() {
        if (this.mLoadThumbnailTask != null) {
            this.mLoadThumbnailTask.cancel(true);
        }
        this.mLoadThumbnailTask = new LoadThumbnailTask(false).execute(new Void[0]);
    }

    public Thumbnail getThumbnail() {
        return this.mThumbnail;
    }

    public void saveThumbnailToFile() {
        if (this.mThumbnail != null && !this.mThumbnail.fromFile()) {
            new SaveThumbnailTask().execute(new Thumbnail[]{this.mThumbnail});
        }
    }

    public void setThumbnail(Thumbnail thumbnail) {
        setThumbnail(thumbnail, true);
    }

    public void setThumbnail(Thumbnail thumbnail, boolean z) {
        this.mThumbnail = thumbnail;
        if (z) {
            updateThumbnailView();
        }
    }

    public void updateThumbnailView() {
        this.mActivityBase.getUIController().updateThumbnailView(this.mThumbnail);
    }
}
