package com.android.camera;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.util.Log;
import com.android.camera.storage.Storage;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Thumbnail {
    private static Object sLock = new Object();
    private Bitmap mBitmap;
    private boolean mFromFile = false;
    private Uri mUri;

    private static class Media {
        public final long dateTaken;
        public final long id;
        public final int orientation;
        public final String path;
        public final Uri uri;

        public Media(long j, int i, long j2, Uri uri, String str) {
            this.id = j;
            this.orientation = i;
            this.dateTaken = j2;
            this.uri = uri;
            this.path = str;
        }
    }

    private Thumbnail(Uri uri, Bitmap bitmap, int i, boolean z) {
        this.mUri = uri;
        this.mBitmap = rotateImage(bitmap, i, z);
    }

    public static Bitmap createBitmap(byte[] bArr, int i, boolean z, int i2) {
        Options options = new Options();
        options.inSampleSize = i2;
        options.inPurgeable = true;
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        i %= 360;
        if (decodeByteArray != null && (i != 0 || z)) {
            Matrix matrix = new Matrix();
            Matrix matrix2 = new Matrix();
            if (i != 0) {
                matrix.setRotate((float) i, ((float) decodeByteArray.getWidth()) * 0.5f, ((float) decodeByteArray.getHeight()) * 0.5f);
            }
            if (z) {
                matrix2.setScale(-1.0f, 1.0f, ((float) decodeByteArray.getWidth()) * 0.5f, ((float) decodeByteArray.getHeight()) * 0.5f);
                matrix.postConcat(matrix2);
            }
            try {
                Bitmap createBitmap = Bitmap.createBitmap(decodeByteArray, 0, 0, decodeByteArray.getWidth(), decodeByteArray.getHeight(), matrix, true);
                if (createBitmap != decodeByteArray) {
                    decodeByteArray.recycle();
                }
                return createBitmap;
            } catch (Throwable e) {
                Log.w("Thumbnail", "Failed to rotate thumbnail", e);
            }
        }
        return decodeByteArray;
    }

    public static Thumbnail createThumbnail(Uri uri, Bitmap bitmap, int i, boolean z) {
        if (bitmap != null) {
            return new Thumbnail(uri, bitmap, i, z);
        }
        Log.e("Thumbnail", "Failed to create thumbnail from null bitmap");
        return null;
    }

    public static Thumbnail createThumbnail(byte[] bArr, int i, int i2, Uri uri, boolean z) {
        Options options = new Options();
        options.inSampleSize = i2;
        options.inPurgeable = true;
        return createThumbnail(uri, BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options), i, z);
    }

    public static Thumbnail createThumbnailFromUri(ContentResolver contentResolver, Uri uri, boolean z) {
        if (!(uri == null || uri.getPath() == null)) {
            boolean contains = uri.getPath().contains(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
            Cursor query = contentResolver.query(uri, contains ? new String[]{"_id", "_data", "orientation"} : new String[]{"_id", "_data"}, null, null, null);
            long j = -1;
            String str = null;
            int i = 0;
            Object obj = null;
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        j = query.getLong(0);
                        str = query.getString(1);
                        i = contains ? query.getInt(2) : 0;
                        obj = 1;
                    }
                } catch (Throwable th) {
                    if (query != null) {
                        query.close();
                    }
                }
            }
            if (query != null) {
                query.close();
            }
            if (obj != null) {
                Bitmap thumbnail;
                if (contains) {
                    thumbnail = Thumbnails.getThumbnail(contentResolver, j, 1, null);
                    if (thumbnail == null) {
                        thumbnail = ThumbnailUtils.createImageThumbnail(str, 1);
                    }
                } else {
                    thumbnail = Video.Thumbnails.getThumbnail(contentResolver, j, 1, null);
                    if (thumbnail == null) {
                        thumbnail = ThumbnailUtils.createVideoThumbnail(str, 1);
                    }
                }
                return createThumbnail(uri, thumbnail, i, z);
            }
        }
        return null;
    }

    public static Bitmap createVideoThumbnailBitmap(FileDescriptor fileDescriptor, int i) {
        return createVideoThumbnailBitmap(null, fileDescriptor, i);
    }

    public static Bitmap createVideoThumbnailBitmap(String str, int i) {
        return createVideoThumbnailBitmap(str, null, i);
    }

    private static Bitmap createVideoThumbnailBitmap(String str, FileDescriptor fileDescriptor, int i) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        if (str != null) {
            try {
                mediaMetadataRetriever.setDataSource(str);
            } catch (IllegalArgumentException e) {
                try {
                    mediaMetadataRetriever.release();
                } catch (RuntimeException e2) {
                }
            } catch (RuntimeException e3) {
                try {
                    mediaMetadataRetriever.release();
                } catch (RuntimeException e4) {
                }
            } catch (Throwable th) {
                try {
                    mediaMetadataRetriever.release();
                } catch (RuntimeException e5) {
                }
            }
        } else {
            mediaMetadataRetriever.setDataSource(fileDescriptor);
        }
        bitmap = mediaMetadataRetriever.getFrameAtTime(-1);
        try {
            mediaMetadataRetriever.release();
        } catch (RuntimeException e6) {
        }
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > i) {
            float f = ((float) i) / ((float) width);
            bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(((float) width) * f), Math.round(((float) height) * f), true);
        }
        return bitmap;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int min = Math.min(width, height);
        Paint paint = new Paint();
        Rect rect = new Rect((width - min) >> 1, (height - min) >> 1, (width + min) >> 1, (height + min) >> 1);
        if (min < 150) {
            min *= 2;
        }
        int i = min >> 1;
        Rect rect2 = new Rect(0, 0, min, min);
        Bitmap createBitmap = Bitmap.createBitmap(min, min, Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setColor(-1);
        paint.setStyle(Style.FILL);
        canvas.drawCircle((float) i, (float) i, (float) i, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect2, paint);
        return createBitmap;
    }

    private static String getImageBucketIds() {
        return Storage.secondaryStorageMounted() ? "bucket_id IN (" + Storage.PRIMARY_BUCKET_ID + "," + Storage.SECONDARY_BUCKET_ID + ")" : "bucket_id=" + Storage.BUCKET_ID;
    }

    private static Media getLastImageThumbnail(ContentResolver contentResolver) {
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Media media = "1";
        Uri build = uri.buildUpon().appendQueryParameter("limit", media).build();
        String[] strArr = new String[]{"_id", "orientation", "datetaken", "_data"};
        String str = "mime_type='image/jpeg' AND " + getImageBucketIds() + " AND " + "_size" + " > 0";
        String str2 = "datetaken DESC,_id DESC";
        Cursor cursor = null;
        Cursor cursor2 = null;
        Object obj = null;
        try {
            long j;
            cursor = contentResolver.query(build, strArr, str, null, str2);
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getString(3) == null || !new File(cursor.getString(3)).exists()) {
                    obj = 1;
                } else {
                    j = cursor.getLong(0);
                    media = new Media(j, cursor.getInt(1), cursor.getLong(2), ContentUris.withAppendedId(uri, j), cursor.getString(3));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return media;
                }
            }
            if (obj != null) {
                cursor2 = contentResolver.query(uri, strArr, str, null, str2);
                if (cursor2 != null) {
                    while (cursor2.moveToNext()) {
                        if (cursor2.getString(3) != null && new File(cursor2.getString(3)).exists()) {
                            j = cursor2.getLong(0);
                            media = new Media(j, cursor2.getInt(1), cursor2.getLong(2), ContentUris.withAppendedId(uri, j), cursor2.getString(3));
                            return media;
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        }
    }

    public static int getLastThumbnailFromContentResolver(ContentResolver contentResolver, Thumbnail[] thumbnailArr, Uri uri) {
        Media lastImageThumbnail = getLastImageThumbnail(contentResolver);
        Media lastVideoThumbnail = getLastVideoThumbnail(contentResolver);
        if (lastImageThumbnail == null && lastVideoThumbnail == null) {
            return 0;
        }
        Bitmap thumbnail;
        Media media;
        if (lastImageThumbnail == null || (lastVideoThumbnail != null && lastImageThumbnail.dateTaken < lastVideoThumbnail.dateTaken)) {
            if (uri != null && uri.equals(lastVideoThumbnail.uri)) {
                return -1;
            }
            thumbnail = Video.Thumbnails.getThumbnail(contentResolver, lastVideoThumbnail.id, 1, null);
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.createVideoThumbnail(lastVideoThumbnail.path, 1);
            }
            media = lastVideoThumbnail;
        } else if (uri != null && uri.equals(lastImageThumbnail.uri)) {
            return -1;
        } else {
            thumbnail = Thumbnails.getThumbnail(contentResolver, lastImageThumbnail.id, 1, null);
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.createImageThumbnail(lastImageThumbnail.path, 1);
            }
            media = lastImageThumbnail;
        }
        if (!Util.isUriValid(media.uri, contentResolver)) {
            return 2;
        }
        thumbnailArr[0] = createThumbnail(media.uri, thumbnail, media.orientation, false);
        return 1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.android.camera.Thumbnail getLastThumbnailFromFile(java.io.File r16, android.content.ContentResolver r17) {
        /*
        r9 = new java.io.File;
        r12 = "last_thumb";
        r0 = r16;
        r9.<init>(r0, r12);
        r11 = 0;
        r3 = 0;
        r7 = 0;
        r1 = 0;
        r4 = 0;
        r13 = sLock;
        monitor-enter(r13);
        r8 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x0060 }
        r8.<init>(r9);	 Catch:{ IOException -> 0x0060 }
        r2 = new java.io.BufferedInputStream;	 Catch:{ IOException -> 0x00a6, all -> 0x009a }
        r12 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        r2.<init>(r8, r12);	 Catch:{ IOException -> 0x00a6, all -> 0x009a }
        r5 = new java.io.DataInputStream;	 Catch:{ IOException -> 0x00a9, all -> 0x009d }
        r5.<init>(r2);	 Catch:{ IOException -> 0x00a9, all -> 0x009d }
        r12 = r5.readUTF();	 Catch:{ IOException -> 0x00ad, all -> 0x00a1 }
        r11 = android.net.Uri.parse(r12);	 Catch:{ IOException -> 0x00ad, all -> 0x00a1 }
        r0 = r17;
        r12 = com.android.camera.Util.isUriValid(r11, r0);	 Catch:{ IOException -> 0x00ad, all -> 0x00a1 }
        if (r12 != 0) goto L_0x0042;
    L_0x0033:
        r5.close();	 Catch:{ IOException -> 0x00ad, all -> 0x00a1 }
        com.android.camera.Util.closeSilently(r8);	 Catch:{ all -> 0x0095 }
        com.android.camera.Util.closeSilently(r2);	 Catch:{ all -> 0x0095 }
        com.android.camera.Util.closeSilently(r5);	 Catch:{ all -> 0x0095 }
        r12 = 0;
        monitor-exit(r13);
        return r12;
    L_0x0042:
        r3 = android.graphics.BitmapFactory.decodeStream(r5);	 Catch:{ IOException -> 0x00ad, all -> 0x00a1 }
        r5.close();	 Catch:{ IOException -> 0x00ad, all -> 0x00a1 }
        com.android.camera.Util.closeSilently(r8);	 Catch:{ all -> 0x0095 }
        com.android.camera.Util.closeSilently(r2);	 Catch:{ all -> 0x0095 }
        com.android.camera.Util.closeSilently(r5);	 Catch:{ all -> 0x0095 }
        monitor-exit(r13);
        r12 = 0;
        r13 = 0;
        r10 = createThumbnail(r11, r3, r12, r13);
        if (r10 == 0) goto L_0x005f;
    L_0x005b:
        r12 = 1;
        r10.setFromFile(r12);
    L_0x005f:
        return r10;
    L_0x0060:
        r6 = move-exception;
    L_0x0061:
        r12 = "Thumbnail";
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0087 }
        r14.<init>();	 Catch:{ all -> 0x0087 }
        r15 = "Fail to load bitmap. ";
        r14 = r14.append(r15);	 Catch:{ all -> 0x0087 }
        r14 = r14.append(r6);	 Catch:{ all -> 0x0087 }
        r14 = r14.toString();	 Catch:{ all -> 0x0087 }
        android.util.Log.i(r12, r14);	 Catch:{ all -> 0x0087 }
        com.android.camera.Util.closeSilently(r7);	 Catch:{ all -> 0x0092 }
        com.android.camera.Util.closeSilently(r1);	 Catch:{ all -> 0x0092 }
        com.android.camera.Util.closeSilently(r4);	 Catch:{ all -> 0x0092 }
        r12 = 0;
        monitor-exit(r13);
        return r12;
    L_0x0087:
        r12 = move-exception;
    L_0x0088:
        com.android.camera.Util.closeSilently(r7);	 Catch:{ all -> 0x0092 }
        com.android.camera.Util.closeSilently(r1);	 Catch:{ all -> 0x0092 }
        com.android.camera.Util.closeSilently(r4);	 Catch:{ all -> 0x0092 }
        throw r12;	 Catch:{ all -> 0x0092 }
    L_0x0092:
        r12 = move-exception;
    L_0x0093:
        monitor-exit(r13);
        throw r12;
    L_0x0095:
        r12 = move-exception;
        r4 = r5;
        r1 = r2;
        r7 = r8;
        goto L_0x0093;
    L_0x009a:
        r12 = move-exception;
        r7 = r8;
        goto L_0x0088;
    L_0x009d:
        r12 = move-exception;
        r1 = r2;
        r7 = r8;
        goto L_0x0088;
    L_0x00a1:
        r12 = move-exception;
        r4 = r5;
        r1 = r2;
        r7 = r8;
        goto L_0x0088;
    L_0x00a6:
        r6 = move-exception;
        r7 = r8;
        goto L_0x0061;
    L_0x00a9:
        r6 = move-exception;
        r1 = r2;
        r7 = r8;
        goto L_0x0061;
    L_0x00ad:
        r6 = move-exception;
        r4 = r5;
        r1 = r2;
        r7 = r8;
        goto L_0x0061;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.Thumbnail.getLastThumbnailFromFile(java.io.File, android.content.ContentResolver):com.android.camera.Thumbnail");
    }

    public static int getLastThumbnailFromUriList(ContentResolver contentResolver, Thumbnail[] thumbnailArr, ArrayList<Uri> arrayList, Uri uri) {
        if (arrayList == null || arrayList.size() == 0) {
            return 0;
        }
        int size = arrayList.size() - 1;
        while (size >= 0) {
            Uri uri2 = (Uri) arrayList.get(size);
            if (!Util.isUriValid(uri2, contentResolver)) {
                size--;
            } else if (uri != null && uri.equals(uri2)) {
                return -1;
            } else {
                thumbnailArr[0] = createThumbnailFromUri(contentResolver, uri2, false);
                return 1;
            }
        }
        return 0;
    }

    public static Uri getLastThumbnailUri(ContentResolver contentResolver) {
        Media lastImageThumbnail = getLastImageThumbnail(contentResolver);
        Media lastVideoThumbnail = getLastVideoThumbnail(contentResolver);
        return (lastImageThumbnail == null || (lastVideoThumbnail != null && lastImageThumbnail.dateTaken < lastVideoThumbnail.dateTaken)) ? (lastVideoThumbnail == null || (lastImageThumbnail != null && lastVideoThumbnail.dateTaken < lastImageThumbnail.dateTaken)) ? null : lastVideoThumbnail.uri : lastImageThumbnail.uri;
    }

    private static Media getLastVideoThumbnail(ContentResolver contentResolver) {
        Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Media media = "1";
        Uri build = uri.buildUpon().appendQueryParameter("limit", media).build();
        String[] strArr = new String[]{"_id", "_data", "datetaken"};
        String str = getVideoBucketIds() + " AND " + "_size" + " > 0";
        String str2 = "datetaken DESC,_id DESC";
        Cursor cursor = null;
        Cursor cursor2 = null;
        Object obj = null;
        try {
            long j;
            cursor = contentResolver.query(build, strArr, str, null, str2);
            if (cursor != null && cursor.moveToFirst()) {
                j = cursor.getLong(0);
                if (cursor.getString(1) == null || !new File(cursor.getString(1)).exists()) {
                    obj = 1;
                } else {
                    media = new Media(j, 0, cursor.getLong(2), ContentUris.withAppendedId(uri, j), cursor.getString(1));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return media;
                }
            }
            if (obj != null) {
                cursor2 = contentResolver.query(uri, strArr, str, null, str2);
                if (cursor2 != null) {
                    while (cursor2.moveToNext()) {
                        if (cursor2.getString(1) != null && new File(cursor2.getString(1)).exists()) {
                            j = cursor2.getLong(0);
                            media = new Media(j, 0, cursor2.getLong(2), ContentUris.withAppendedId(uri, j), cursor2.getString(1));
                            return media;
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        }
    }

    private static String getVideoBucketIds() {
        return Storage.secondaryStorageMounted() ? "bucket_id IN (" + Storage.PRIMARY_BUCKET_ID + "," + Storage.SECONDARY_BUCKET_ID + ")" : "bucket_id=" + Storage.BUCKET_ID;
    }

    private static Bitmap rotateImage(Bitmap bitmap, int i, boolean z) {
        Bitmap circleBitmap;
        if (i != 0 || z) {
            Matrix matrix = new Matrix();
            Matrix matrix2 = new Matrix();
            if (i != 0) {
                matrix.setRotate((float) i, ((float) bitmap.getWidth()) * 0.5f, ((float) bitmap.getHeight()) * 0.5f);
            }
            if (z) {
                matrix2.setScale(-1.0f, 1.0f, ((float) bitmap.getWidth()) * 0.5f, ((float) bitmap.getHeight()) * 0.5f);
                matrix.postConcat(matrix2);
            }
            try {
                Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                circleBitmap = getCircleBitmap(createBitmap);
                if (createBitmap != bitmap) {
                    createBitmap.recycle();
                }
                bitmap.recycle();
                return circleBitmap;
            } catch (Throwable e) {
                Log.w("Thumbnail", "Failed to rotate thumbnail", e);
            }
        }
        circleBitmap = getCircleBitmap(bitmap);
        bitmap.recycle();
        return circleBitmap;
    }

    public boolean fromFile() {
        return this.mFromFile;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void saveLastThumbnailToFile(File file) {
        Closeable dataOutputStream;
        Throwable e;
        Throwable th;
        File file2 = new File(file, "last_thumb");
        Closeable closeable = null;
        Closeable closeable2 = null;
        Closeable closeable3 = null;
        synchronized (sLock) {
            try {
                Closeable fileOutputStream = new FileOutputStream(file2);
                try {
                    Closeable bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 4096);
                    try {
                        dataOutputStream = new DataOutputStream(bufferedOutputStream);
                        try {
                            dataOutputStream.writeUTF(this.mUri.toString());
                            this.mBitmap.compress(CompressFormat.JPEG, 90, dataOutputStream);
                            dataOutputStream.close();
                        } catch (IOException e2) {
                            e = e2;
                            closeable3 = dataOutputStream;
                            closeable2 = bufferedOutputStream;
                            closeable = fileOutputStream;
                            try {
                                Log.e("Thumbnail", "Fail to store bitmap. path=" + file2.getPath(), e);
                            } catch (Throwable th2) {
                                th = th2;
                                Util.closeSilently(closeable);
                                Util.closeSilently(closeable2);
                                Util.closeSilently(closeable3);
                                throw th;
                            }
                            try {
                                Util.closeSilently(closeable);
                                Util.closeSilently(closeable2);
                                Util.closeSilently(closeable3);
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            closeable3 = dataOutputStream;
                            closeable2 = bufferedOutputStream;
                            closeable = fileOutputStream;
                            Util.closeSilently(closeable);
                            Util.closeSilently(closeable2);
                            Util.closeSilently(closeable3);
                            throw th;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        closeable2 = bufferedOutputStream;
                        closeable = fileOutputStream;
                        Log.e("Thumbnail", "Fail to store bitmap. path=" + file2.getPath(), e);
                        Util.closeSilently(closeable);
                        Util.closeSilently(closeable2);
                        Util.closeSilently(closeable3);
                    } catch (Throwable th5) {
                        th = th5;
                        closeable2 = bufferedOutputStream;
                        closeable = fileOutputStream;
                        Util.closeSilently(closeable);
                        Util.closeSilently(closeable2);
                        Util.closeSilently(closeable3);
                        throw th;
                    }
                    try {
                        Util.closeSilently(fileOutputStream);
                        Util.closeSilently(bufferedOutputStream);
                        Util.closeSilently(dataOutputStream);
                        closeable3 = dataOutputStream;
                        closeable2 = bufferedOutputStream;
                        closeable = fileOutputStream;
                    } catch (Throwable th6) {
                        th = th6;
                        closeable3 = dataOutputStream;
                        closeable2 = bufferedOutputStream;
                        closeable = fileOutputStream;
                        throw th;
                    }
                } catch (IOException e4) {
                    e = e4;
                    closeable = fileOutputStream;
                    Log.e("Thumbnail", "Fail to store bitmap. path=" + file2.getPath(), e);
                    Util.closeSilently(closeable);
                    Util.closeSilently(closeable2);
                    Util.closeSilently(closeable3);
                } catch (Throwable th7) {
                    th = th7;
                    closeable = fileOutputStream;
                    Util.closeSilently(closeable);
                    Util.closeSilently(closeable2);
                    Util.closeSilently(closeable3);
                    throw th;
                }
            } catch (IOException e5) {
                e = e5;
                Log.e("Thumbnail", "Fail to store bitmap. path=" + file2.getPath(), e);
                Util.closeSilently(closeable);
                Util.closeSilently(closeable2);
                Util.closeSilently(closeable3);
            }
        }
    }

    public void setFromFile(boolean z) {
        this.mFromFile = z;
    }
}
