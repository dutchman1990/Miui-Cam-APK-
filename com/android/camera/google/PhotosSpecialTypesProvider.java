package com.android.camera.google;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.Log;
import com.android.camera.google.ProcessingMediaManager.JpegThumbnail;
import com.google.android.apps.photos.api.IconQuery$Type;
import com.google.android.apps.photos.api.PhotosOemApi;
import com.google.android.apps.photos.api.Preconditions;
import com.google.android.apps.photos.api.signature.TrustedPartners;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class PhotosSpecialTypesProvider extends ContentProvider {
    private static final String[] TYPE_URI_PROJECTION = new String[]{"special_type_id"};
    private String authority;
    private TrustedPartners trustedPartners;
    private UriMatcher uriMatcher;

    private boolean deleteSpecialType(Uri uri) {
        Log.d("PhotoTypes", "delete uri->" + uri + ", media id -> " + uri.getLastPathSegment());
        ProviderDbHelper.get(getContext()).getReadableDatabase().delete("type_uri", "media_store_id=?", new String[]{r2});
        return true;
    }

    @Nullable
    private ParcelFileDescriptor loadIcon(Uri uri, IconQuery$Type iconQuery$Type) throws FileNotFoundException {
        Log.d("PhotoTypes", "load Icon uri->" + uri);
        SpecialType fromTypeId = SpecialType.fromTypeId(Integer.valueOf((String) uri.getPathSegments().get(1)).intValue());
        int i = iconQuery$Type == IconQuery$Type.BADGE ? fromTypeId.iconBadgeResourceId : fromTypeId.iconDialogResourceId;
        Resources resources = getContext().getResources();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) resources.getDrawable(i);
        int dimensionPixelSize = resources.getDimensionPixelSize(iconQuery$Type.getDimensionResourceId());
        return writeBitmapToFd(Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), dimensionPixelSize, dimensionPixelSize, false), CompressFormat.PNG);
    }

    private ParcelFileDescriptor loadProcessingThumb(Uri uri) throws FileNotFoundException {
        long parseId = ContentUris.parseId(uri);
        if (ProcessingMediaManager.instance().isProcessingMedia(parseId)) {
            JpegThumbnail processingMedia = ProcessingMediaManager.instance().getProcessingMedia(parseId);
            if (processingMedia == null) {
                throw new FileNotFoundException("Empty thumbnail");
            }
            Bitmap decodeBitmap = processingMedia.decodeBitmap();
            return decodeBitmap == null ? writeBytesToFd(processingMedia.data) : writeBitmapToFd(decodeBitmap, CompressFormat.JPEG);
        } else {
            throw new FileNotFoundException("Cannot find processing thumb for " + parseId);
        }
    }

    public static void markPortraitSpecialType(Context context, Uri uri) {
        if (uri != null) {
            long parseId = ContentUris.parseId(uri);
            if (parseId > 0) {
                Uri build = new Builder().scheme("content").authority(context.getString(C0049R.string.photos_special_types_authority)).appendEncodedPath(Uri.encode(SpecialType.PORTRAIT_TYPE.toString())).build();
                ContentResolver contentResolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put("media_store_id", Long.valueOf(parseId));
                contentResolver.insert(build, contentValues);
            }
        }
    }

    private static Cursor queryOrScanAndQuery(SQLiteDatabase sQLiteDatabase, long j) {
        SpecialType querySpecialTypeId = querySpecialTypeId(sQLiteDatabase, j);
        Log.d("PhotoTypes", "queryOrScanAndQuery from query -> " + querySpecialTypeId);
        Cursor matrixCursor = new MatrixCursor(new String[]{"special_type_id"});
        if (!(querySpecialTypeId == SpecialType.NONE || querySpecialTypeId == SpecialType.UNKNOWN)) {
            matrixCursor.addRow(new Object[]{querySpecialTypeId.name()});
        }
        return matrixCursor;
    }

    private Cursor queryProcessingMetadata(@Nullable Long l) {
        Log.d("PhotoTypes", "queryProcessingMetaData -> " + l);
        Cursor matrixCursor = new MatrixCursor(new String[]{"media_store_id", "progress_status", "progress_percentage"});
        if (l == null) {
            Iterable processingMedias = ProcessingMediaManager.instance().getProcessingMedias();
            Log.d("PhotoTypes", "query processing medias -> " + processingMedias);
            Iterator it = processingMedias.iterator();
            while (it.hasNext()) {
                matrixCursor.addRow(new Object[]{(String) it.next(), Integer.valueOf(1), Integer.valueOf(0)});
            }
        } else if (ProcessingMediaManager.instance().isProcessingMedia(l.longValue())) {
            Log.d("PhotoTypes", "query processing add into resutl id => " + l);
            matrixCursor.addRow(new Object[]{l, Integer.valueOf(1), Integer.valueOf(0)});
        }
        matrixCursor.moveToPosition(-1);
        return matrixCursor;
    }

    private Cursor querySpecialTypeId(Uri uri) {
        Log.d("PhotoTypes", "querySepcial Type id uri->" + uri);
        return queryOrScanAndQuery(ProviderDbHelper.get(getContext()).getReadableDatabase(), PhotosOemApi.getMediaStoreIdFromQueryTypeUri(uri));
    }

    private static SpecialType querySpecialTypeId(SQLiteDatabase sQLiteDatabase, long j) {
        SpecialType specialType = SpecialType.UNKNOWN;
        SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
        Cursor query = sQLiteDatabase2.query("type_uri", TYPE_URI_PROJECTION, "media_store_id = ?", new String[]{String.valueOf(j)}, null, null, null);
        try {
            if (query.moveToFirst()) {
                specialType = SpecialType.valueOf(query.getString(query.getColumnIndexOrThrow("special_type_id")));
            }
            query.close();
            return specialType;
        } catch (Throwable th) {
            query.close();
        }
    }

    private Cursor querySpecialTypeMetadata(Uri uri, String[] strArr) {
        String specialTypeIdFromQueryDataUri = PhotosOemApi.getSpecialTypeIdFromQueryDataUri(uri);
        SpecialType valueOf = SpecialType.valueOf(specialTypeIdFromQueryDataUri);
        Log.d("PhotoTypes", "query special uri -> " + uri);
        Log.d("PhotoTypes", "query special type id str -> " + specialTypeIdFromQueryDataUri + ", specialType->" + valueOf);
        Cursor matrixCursor = new MatrixCursor(strArr);
        Object[] objArr = new Object[strArr.length];
        int i = 0;
        for (String str : strArr) {
            if (str.equals("configuration")) {
                objArr[i] = valueOf.getConfiguration().getKey();
            } else if (str.equals("special_type_name")) {
                objArr[i] = getContext().getString(valueOf.nameResourceId);
            } else if (str.equals("special_type_description")) {
                objArr[i] = getContext().getString(valueOf.descriptionResourceId);
            } else if (str.equals("special_type_icon_uri")) {
                objArr[i] = new Builder().scheme("content").authority(this.authority).appendPath("icon").appendPath(String.valueOf(valueOf.typeId));
            } else {
                objArr[i] = null;
            }
            i++;
        }
        matrixCursor.addRow(objArr);
        return matrixCursor;
    }

    private Bundle querySpecialTypesVersion() {
        Bundle bundle = new Bundle();
        bundle.putInt("version", 3);
        return bundle;
    }

    private void validateCallingPackage() {
        if (!this.trustedPartners.isTrustedApplication(getCallingPackage())) {
            throw new SecurityException();
        }
    }

    private ParcelFileDescriptor writeBitmapToFd(Bitmap bitmap, CompressFormat compressFormat) throws FileNotFoundException {
        IOException iOException;
        IOException e;
        Throwable th;
        ParcelFileDescriptor parcelFileDescriptor = null;
        OutputStream outputStream = null;
        try {
            ParcelFileDescriptor[] createPipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor parcelFileDescriptor2 = createPipe[0];
            parcelFileDescriptor = createPipe[1];
            OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));
            try {
                bitmap.compress(compressFormat, 100, bufferedOutputStream);
                bufferedOutputStream.close();
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e2) {
                        iOException = e2;
                    }
                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e22) {
                        iOException = e22;
                    }
                }
                return parcelFileDescriptor2;
            } catch (IOException e3) {
                e22 = e3;
                outputStream = bufferedOutputStream;
                iOException = e22;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e222) {
                        iOException = e222;
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222) {
                        iOException = e2222;
                    }
                }
                throw new FileNotFoundException(iOException.getMessage());
            } catch (Throwable th2) {
                th = th2;
                outputStream = bufferedOutputStream;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e22222) {
                        iOException = e22222;
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e222222) {
                        iOException = e222222;
                    }
                }
                throw th;
            }
        } catch (IOException e4) {
            e222222 = e4;
            iOException = e222222;
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            throw new FileNotFoundException(iOException.getMessage());
        } catch (Throwable th3) {
            th = th3;
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            throw th;
        }
    }

    private ParcelFileDescriptor writeBytesToFd(byte[] bArr) throws FileNotFoundException {
        IOException iOException;
        IOException e;
        Throwable th;
        ParcelFileDescriptor parcelFileDescriptor = null;
        OutputStream outputStream = null;
        try {
            ParcelFileDescriptor[] createPipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor parcelFileDescriptor2 = createPipe[0];
            parcelFileDescriptor = createPipe[1];
            OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));
            try {
                bufferedOutputStream.write(bArr, 0, bArr.length);
                bufferedOutputStream.close();
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e2) {
                        iOException = e2;
                    }
                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e22) {
                        iOException = e22;
                    }
                }
                return parcelFileDescriptor2;
            } catch (IOException e3) {
                e22 = e3;
                outputStream = bufferedOutputStream;
                iOException = e22;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e222) {
                        iOException = e222;
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222) {
                        iOException = e2222;
                    }
                }
                throw new FileNotFoundException(iOException.getMessage());
            } catch (Throwable th2) {
                th = th2;
                outputStream = bufferedOutputStream;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e22222) {
                        iOException = e22222;
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e222222) {
                        iOException = e222222;
                    }
                }
                throw th;
            }
        } catch (IOException e4) {
            e222222 = e4;
            iOException = e222222;
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            throw new FileNotFoundException(iOException.getMessage());
        } catch (Throwable th3) {
            th = th3;
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            throw th;
        }
    }

    public void attachInfo(Context context, ProviderInfo providerInfo) {
        super.attachInfo(context, providerInfo);
        this.trustedPartners = new TrustedPartners(context, new HashSet(Arrays.asList(context.getResources().getStringArray(C0049R.array.trusted_certificates))));
        this.authority = providerInfo.authority;
        this.uriMatcher = new UriMatcher(-1);
        this.uriMatcher.addURI(this.authority, "type/*", 1);
        this.uriMatcher.addURI(this.authority, "data/*", 2);
        this.uriMatcher.addURI(this.authority, "icon/#/badge", 3);
        this.uriMatcher.addURI(this.authority, "icon/#/interact", 4);
        this.uriMatcher.addURI(this.authority, "icon/#/dialog", 5);
        this.uriMatcher.addURI(this.authority, "delete/#", 6);
        this.uriMatcher.addURI(this.authority, "processing", 7);
        this.uriMatcher.addURI(this.authority, "processing/#", 8);
    }

    @Nullable
    public Bundle call(@NonNull String str, @Nullable String str2, @Nullable Bundle bundle) {
        return TextUtils.equals("version", str) ? querySpecialTypesVersion() : super.call(str, str2, bundle);
    }

    public int delete(@NonNull Uri uri, String str, String[] strArr) {
        validateCallingPackage();
        switch (this.uriMatcher.match(uri)) {
            case 6:
                Preconditions.checkArgument(str == null);
                Preconditions.checkArgument(strArr == null);
                return deleteSpecialType(uri) ? 1 : 0;
            default:
                throw new IllegalArgumentException("Unrecognized uri: " + uri);
        }
    }

    @Nullable
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        validateCallingPackage();
        Log.d("PhotoTypes", "insert uri->" + uri + ", values " + contentValues);
        SpecialType valueOf = SpecialType.valueOf(PhotosOemApi.getSpecialTypeIdFromQueryDataUri(uri));
        if (valueOf != SpecialType.PORTRAIT_TYPE) {
            return null;
        }
        SQLiteDatabase readableDatabase = ProviderDbHelper.get(getContext()).getReadableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("media_store_id", contentValues.getAsString("media_store_id"));
        contentValues2.put("special_type_id", valueOf.name());
        long replace = readableDatabase.replace("type_uri", null, contentValues2);
        return uri;
    }

    public boolean onCreate() {
        return true;
    }

    @Nullable
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String str) throws FileNotFoundException {
        validateCallingPackage();
        if ("r".equals(str)) {
            switch (this.uriMatcher.match(uri)) {
                case 3:
                    Log.i("PhotoTypes", "loading badge icon " + uri);
                    return loadIcon(uri, IconQuery$Type.BADGE);
                case 4:
                    Log.i("PhotoTypes", "loading interact icon " + uri);
                    return loadIcon(uri, IconQuery$Type.INTERACT);
                case 5:
                    Log.i("PhotoTypes", "loading dialog icon " + uri);
                    return loadIcon(uri, IconQuery$Type.DIALOG);
                case 8:
                    Log.i("PhotoTypes", "loading processing thumb " + uri);
                    return loadProcessingThumb(uri);
                default:
                    throw new IllegalArgumentException("Unrecognized format: " + uri);
            }
        }
        throw new IllegalArgumentException("Unsupported mode: " + str);
    }

    @Nullable
    public Cursor query(@NonNull Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        validateCallingPackage();
        Log.d("PhotoTypes", "query, uri-> " + uri);
        switch (this.uriMatcher.match(uri)) {
            case 1:
                return querySpecialTypeId(uri);
            case 2:
                return querySpecialTypeMetadata(uri, strArr);
            case 7:
                return queryProcessingMetadata(null);
            case 8:
                return queryProcessingMetadata(Long.valueOf(ContentUris.parseId(uri)));
            default:
                throw new IllegalArgumentException("Unrecognized uri: " + uri);
        }
    }

    public int update(@NonNull Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }
}
