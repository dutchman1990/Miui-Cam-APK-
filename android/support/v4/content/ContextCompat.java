package android.support.v4.content;

import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;

public class ContextCompat {
    private static final Object sLock = new Object();

    protected ContextCompat() {
    }

    public static int checkSelfPermission(@NonNull Context context, @NonNull String str) {
        if (str != null) {
            return context.checkPermission(str, Process.myPid(), Process.myUid());
        }
        throw new IllegalArgumentException("permission is null");
    }
}
