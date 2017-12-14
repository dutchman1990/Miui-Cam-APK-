package com.android.camera;

import com.android.camera.panorama.NativeMemoryAllocator;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PanoUtil {
    public static ByteBuffer createByteBuffer(byte[] bArr) {
        ByteBuffer allocateBuffer = NativeMemoryAllocator.allocateBuffer(bArr.length);
        allocateBuffer.order(ByteOrder.nativeOrder());
        allocateBuffer.position(0);
        allocateBuffer.put(bArr);
        allocateBuffer.position(0);
        return allocateBuffer;
    }
}
