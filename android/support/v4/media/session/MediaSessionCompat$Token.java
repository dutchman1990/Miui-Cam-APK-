package android.support.v4.media.session;

import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class MediaSessionCompat$Token implements Parcelable {
    public static final Creator<MediaSessionCompat$Token> CREATOR = new C00251();
    private final Object mInner;

    static class C00251 implements Creator<MediaSessionCompat$Token> {
        C00251() {
        }

        public MediaSessionCompat$Token createFromParcel(Parcel parcel) {
            return new MediaSessionCompat$Token(VERSION.SDK_INT >= 21 ? parcel.readParcelable(null) : parcel.readStrongBinder());
        }

        public MediaSessionCompat$Token[] newArray(int i) {
            return new MediaSessionCompat$Token[i];
        }
    }

    MediaSessionCompat$Token(Object obj) {
        this.mInner = obj;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaSessionCompat$Token)) {
            return false;
        }
        MediaSessionCompat$Token mediaSessionCompat$Token = (MediaSessionCompat$Token) obj;
        if (this.mInner != null) {
            return mediaSessionCompat$Token.mInner == null ? false : this.mInner.equals(mediaSessionCompat$Token.mInner);
        } else {
            if (mediaSessionCompat$Token.mInner != null) {
                z = false;
            }
            return z;
        }
    }

    public int hashCode() {
        return this.mInner == null ? 0 : this.mInner.hashCode();
    }

    public void writeToParcel(Parcel parcel, int i) {
        if (VERSION.SDK_INT >= 21) {
            parcel.writeParcelable((Parcelable) this.mInner, i);
        } else {
            parcel.writeStrongBinder((IBinder) this.mInner);
        }
    }
}
