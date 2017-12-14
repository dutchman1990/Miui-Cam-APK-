package com.android.camera.google;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import com.google.android.apps.photos.api.PhotosOemApi;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessingMediaManager {
    private Map<String, JpegThumbnail> mProcessingTable;

    private static class InstanceHolder {
        private static final ProcessingMediaManager sInstance = new ProcessingMediaManager();

        private InstanceHolder() {
        }
    }

    public static class JpegThumbnail {
        final byte[] data;
        final int orientation;
        private Bitmap thumbBitmap;

        public JpegThumbnail(int i, byte[] bArr) {
            this.orientation = i;
            this.data = bArr;
        }

        public Bitmap decodeBitmap() throws FileNotFoundException {
            if (this.thumbBitmap != null) {
                return this.thumbBitmap;
            }
            if (this.data == null) {
                Log.d("ProcessingMedia", "decodeBitmap, empty thumbnail");
                this.thumbBitmap = null;
                throw new FileNotFoundException("Empty thumbnail");
            }
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeByteArray(this.data, 0, this.data.length);
            } catch (Throwable e) {
                Log.d("ProcessingMedia", "decodeBitmap, first try failed ", e);
                Options options = new Options();
                options.inSampleSize = 2;
                try {
                    bitmap = BitmapFactory.decodeByteArray(this.data, 0, this.data.length, options);
                } catch (Throwable e2) {
                    Log.d("ProcessingMedia", "decodeBitmap, second try failed again, ", e2);
                }
            }
            if (bitmap == null) {
                Log.d("ProcessingMedia", "decodeBitmap, no bitmap, pass bytes directly");
                this.thumbBitmap = null;
                return null;
            } else if (this.orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate((float) this.orientation);
                Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (createBitmap != bitmap) {
                    bitmap.recycle();
                }
                this.thumbBitmap = createBitmap;
                return createBitmap;
            } else {
                this.thumbBitmap = bitmap;
                return bitmap;
            }
        }
    }

    private ProcessingMediaManager() {
        this.mProcessingTable = new ConcurrentHashMap();
    }

    public static ProcessingMediaManager instance() {
        return InstanceHolder.sInstance;
    }

    public static void notifyProcessingUri(Context context, long j) {
        if (context != null) {
            Uri queryProcessingUri = PhotosOemApi.getQueryProcessingUri(context, j);
            Log.d("ProcessingMedia", "notifyProcessingUri uri-> " + queryProcessingUri);
            context.getContentResolver().notifyChange(queryProcessingUri, null);
        }
    }

    public void addProcessingMedia(Context context, Uri uri, JpegThumbnail jpegThumbnail) {
        if (uri != null) {
            Log.d("ProcessingMedia", "addProcessingMedia uri -> " + uri);
            long parseId = ContentUris.parseId(uri);
            this.mProcessingTable.put(String.valueOf(parseId), jpegThumbnail);
            notifyProcessingUri(context, parseId);
        }
    }

    public JpegThumbnail getProcessingMedia(long j) {
        return (JpegThumbnail) this.mProcessingTable.get(String.valueOf(j));
    }

    public List<String> getProcessingMedias() {
        List<String> arrayList = new ArrayList();
        for (String add : this.mProcessingTable.keySet()) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public boolean isProcessingMedia(long j) {
        return this.mProcessingTable.containsKey(String.valueOf(j));
    }

    public boolean isProcessingMedia(Uri uri) {
        return isProcessingMedia(ContentUris.parseId(uri));
    }

    public void removeProcessingMedia(Context context, Uri uri) {
        if (uri != null) {
            Log.d("ProcessingMedia", "removeProcessingMedia uri->" + uri);
            long parseId = ContentUris.parseId(uri);
            if (this.mProcessingTable.containsKey(String.valueOf(parseId))) {
                this.mProcessingTable.remove(String.valueOf(parseId));
                notifyProcessingUri(context, parseId);
            }
        }
    }
}
