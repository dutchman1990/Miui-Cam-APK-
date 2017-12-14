package com.android.camera;

import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class XmpUtil {

    private static class Section {
        public byte[] data;
        public int length;
        public int marker;

        private Section() {
        }
    }

    static {
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace("http://ns.google.com/photos/1.0/camera/", "GCamera");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private XmpUtil() {
    }

    public static void addSpecialTypeMeta(String str) {
        XMPMeta extractOrCreateXMPMeta = extractOrCreateXMPMeta(str);
        try {
            extractOrCreateXMPMeta.setProperty("http://ns.google.com/photos/1.0/camera/", "SpecialTypeID", "PORTRAIT_TYPE");
        } catch (Throwable e) {
            Log.m1d("XmpUtil", "got exception when set metadata ", e);
        }
        writeXMPMeta(str, extractOrCreateXMPMeta);
    }

    public static XMPMeta createXMPMeta() {
        return XMPMetaFactory.create();
    }

    public static XMPMeta extractOrCreateXMPMeta(String str) {
        XMPMeta extractXMPMeta = extractXMPMeta(str);
        return extractXMPMeta == null ? createXMPMeta() : extractXMPMeta;
    }

    public static XMPMeta extractXMPMeta(InputStream inputStream) {
        Iterable<Section> parse = parse(inputStream, true);
        if (parse == null) {
            return null;
        }
        for (Section section : parse) {
            if (hasXMPHeader(section.data)) {
                byte[] bArr = new byte[(getXMPContentEnd(section.data) - 29)];
                System.arraycopy(section.data, 29, bArr, 0, bArr.length);
                try {
                    return XMPMetaFactory.parseFromBuffer(bArr);
                } catch (Throwable e) {
                    Log.m1d("XmpUtil", "XMP parse error", e);
                    return null;
                }
            }
        }
        return null;
    }

    public static XMPMeta extractXMPMeta(String str) {
        if (str.toLowerCase().endsWith(".jpg") || str.toLowerCase().endsWith(".jpeg")) {
            try {
                return extractXMPMeta(new FileInputStream(str));
            } catch (Throwable e) {
                Log.m3e("XmpUtil", "Could not read file: " + str, e);
                return null;
            }
        }
        Log.m0d("XmpUtil", "XMP parse: only jpeg file is supported");
        return null;
    }

    private static int getXMPContentEnd(byte[] bArr) {
        int length = bArr.length - 1;
        while (length >= 1) {
            if (bArr[length] == (byte) 62 && bArr[length - 1] != (byte) 63) {
                return length + 1;
            }
            length--;
        }
        return bArr.length;
    }

    private static boolean hasXMPHeader(byte[] bArr) {
        if (bArr.length < 29) {
            return false;
        }
        try {
            byte[] bArr2 = new byte[29];
            System.arraycopy(bArr, 0, bArr2, 0, 29);
            return new String(bArr2, "UTF-8").equals("http://ns.adobe.com/xap/1.0/\u0000");
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    private static List<Section> insertXMPSection(List<Section> list, XMPMeta xMPMeta) {
        int i = 1;
        if (list == null || list.size() <= 1) {
            return null;
        }
        try {
            SerializeOptions serializeOptions = new SerializeOptions();
            serializeOptions.setUseCompactFormat(true);
            serializeOptions.setOmitPacketWrapper(true);
            byte[] serializeToBuffer = XMPMetaFactory.serializeToBuffer(xMPMeta, serializeOptions);
            if (serializeToBuffer.length > 65502) {
                return null;
            }
            byte[] bArr = new byte[(serializeToBuffer.length + 29)];
            System.arraycopy("http://ns.adobe.com/xap/1.0/\u0000".getBytes(), 0, bArr, 0, 29);
            System.arraycopy(serializeToBuffer, 0, bArr, 29, serializeToBuffer.length);
            Section section = new Section();
            section.marker = 225;
            section.length = bArr.length + 2;
            section.data = bArr;
            int i2 = 0;
            while (i2 < list.size()) {
                if (((Section) list.get(i2)).marker == 225 && hasXMPHeader(((Section) list.get(i2)).data)) {
                    list.set(i2, section);
                    return list;
                }
                i2++;
            }
            List<Section> arrayList = new ArrayList();
            if (((Section) list.get(0)).marker != 225) {
                i = 0;
            }
            arrayList.addAll(list.subList(0, i));
            arrayList.add(section);
            arrayList.addAll(list.subList(i, list.size()));
            return arrayList;
        } catch (Throwable e) {
            Log.m1d("XmpUtil", "Serialize xmp failed", e);
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.util.List<com.android.camera.XmpUtil.Section> parse(java.io.InputStream r14, boolean r15) {
        /*
        r13 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r12 = -1;
        r11 = 0;
        r8 = r14.read();	 Catch:{ IOException -> 0x00a8 }
        if (r8 != r13) goto L_0x0012;
    L_0x000a:
        r8 = r14.read();	 Catch:{ IOException -> 0x00a8 }
        r9 = 216; // 0xd8 float:3.03E-43 double:1.067E-321;
        if (r8 == r9) goto L_0x001a;
    L_0x0012:
        if (r14 == 0) goto L_0x0017;
    L_0x0014:
        r14.close();	 Catch:{ IOException -> 0x0018 }
    L_0x0017:
        return r11;
    L_0x0018:
        r1 = move-exception;
        goto L_0x0017;
    L_0x001a:
        r7 = new java.util.ArrayList;	 Catch:{ IOException -> 0x00a8 }
        r7.<init>();	 Catch:{ IOException -> 0x00a8 }
    L_0x001f:
        r0 = r14.read();	 Catch:{ IOException -> 0x00a8 }
        if (r0 == r12) goto L_0x00c7;
    L_0x0025:
        if (r0 == r13) goto L_0x002f;
    L_0x0027:
        if (r14 == 0) goto L_0x002c;
    L_0x0029:
        r14.close();	 Catch:{ IOException -> 0x002d }
    L_0x002c:
        return r11;
    L_0x002d:
        r1 = move-exception;
        goto L_0x002c;
    L_0x002f:
        r0 = r14.read();	 Catch:{ IOException -> 0x00a8 }
        if (r0 == r13) goto L_0x002f;
    L_0x0035:
        if (r0 != r12) goto L_0x003f;
    L_0x0037:
        if (r14 == 0) goto L_0x003c;
    L_0x0039:
        r14.close();	 Catch:{ IOException -> 0x003d }
    L_0x003c:
        return r11;
    L_0x003d:
        r1 = move-exception;
        goto L_0x003c;
    L_0x003f:
        r5 = r0;
        r8 = 218; // 0xda float:3.05E-43 double:1.077E-321;
        if (r0 != r8) goto L_0x006d;
    L_0x0044:
        if (r15 != 0) goto L_0x0065;
    L_0x0046:
        r6 = new com.android.camera.XmpUtil$Section;	 Catch:{ IOException -> 0x00a8 }
        r8 = 0;
        r6.<init>();	 Catch:{ IOException -> 0x00a8 }
        r6.marker = r0;	 Catch:{ IOException -> 0x00a8 }
        r8 = -1;
        r6.length = r8;	 Catch:{ IOException -> 0x00a8 }
        r8 = r14.available();	 Catch:{ IOException -> 0x00a8 }
        r8 = new byte[r8];	 Catch:{ IOException -> 0x00a8 }
        r6.data = r8;	 Catch:{ IOException -> 0x00a8 }
        r8 = r6.data;	 Catch:{ IOException -> 0x00a8 }
        r9 = r6.data;	 Catch:{ IOException -> 0x00a8 }
        r9 = r9.length;	 Catch:{ IOException -> 0x00a8 }
        r10 = 0;
        r14.read(r8, r10, r9);	 Catch:{ IOException -> 0x00a8 }
        r7.add(r6);	 Catch:{ IOException -> 0x00a8 }
    L_0x0065:
        if (r14 == 0) goto L_0x006a;
    L_0x0067:
        r14.close();	 Catch:{ IOException -> 0x006b }
    L_0x006a:
        return r7;
    L_0x006b:
        r1 = move-exception;
        goto L_0x006a;
    L_0x006d:
        r3 = r14.read();	 Catch:{ IOException -> 0x00a8 }
        r4 = r14.read();	 Catch:{ IOException -> 0x00a8 }
        if (r3 == r12) goto L_0x0079;
    L_0x0077:
        if (r4 != r12) goto L_0x0081;
    L_0x0079:
        if (r14 == 0) goto L_0x007e;
    L_0x007b:
        r14.close();	 Catch:{ IOException -> 0x007f }
    L_0x007e:
        return r11;
    L_0x007f:
        r1 = move-exception;
        goto L_0x007e;
    L_0x0081:
        r8 = r3 << 8;
        r2 = r8 | r4;
        if (r15 == 0) goto L_0x008b;
    L_0x0087:
        r8 = 225; // 0xe1 float:3.15E-43 double:1.11E-321;
        if (r0 != r8) goto L_0x00b8;
    L_0x008b:
        r6 = new com.android.camera.XmpUtil$Section;	 Catch:{ IOException -> 0x00a8 }
        r8 = 0;
        r6.<init>();	 Catch:{ IOException -> 0x00a8 }
        r6.marker = r0;	 Catch:{ IOException -> 0x00a8 }
        r6.length = r2;	 Catch:{ IOException -> 0x00a8 }
        r8 = r2 + -2;
        r8 = new byte[r8];	 Catch:{ IOException -> 0x00a8 }
        r6.data = r8;	 Catch:{ IOException -> 0x00a8 }
        r8 = r6.data;	 Catch:{ IOException -> 0x00a8 }
        r9 = r2 + -2;
        r10 = 0;
        r14.read(r8, r10, r9);	 Catch:{ IOException -> 0x00a8 }
        r7.add(r6);	 Catch:{ IOException -> 0x00a8 }
        goto L_0x001f;
    L_0x00a8:
        r1 = move-exception;
        r8 = "XmpUtil";
        r9 = "Could not parse file.";
        com.android.camera.Log.m1d(r8, r9, r1);	 Catch:{ all -> 0x00c0 }
        if (r14 == 0) goto L_0x00b7;
    L_0x00b4:
        r14.close();	 Catch:{ IOException -> 0x00cf }
    L_0x00b7:
        return r11;
    L_0x00b8:
        r8 = r2 + -2;
        r8 = (long) r8;
        r14.skip(r8);	 Catch:{ IOException -> 0x00a8 }
        goto L_0x001f;
    L_0x00c0:
        r8 = move-exception;
        if (r14 == 0) goto L_0x00c6;
    L_0x00c3:
        r14.close();	 Catch:{ IOException -> 0x00d1 }
    L_0x00c6:
        throw r8;
    L_0x00c7:
        if (r14 == 0) goto L_0x00cc;
    L_0x00c9:
        r14.close();	 Catch:{ IOException -> 0x00cd }
    L_0x00cc:
        return r7;
    L_0x00cd:
        r1 = move-exception;
        goto L_0x00cc;
    L_0x00cf:
        r1 = move-exception;
        goto L_0x00b7;
    L_0x00d1:
        r1 = move-exception;
        goto L_0x00c6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.XmpUtil.parse(java.io.InputStream, boolean):java.util.List<com.android.camera.XmpUtil$Section>");
    }

    private static void writeJpegFile(OutputStream outputStream, List<Section> list) throws IOException {
        outputStream.write(255);
        outputStream.write(216);
        for (Section section : list) {
            outputStream.write(255);
            outputStream.write(section.marker);
            if (section.length > 0) {
                int i = section.length & 255;
                outputStream.write(section.length >> 8);
                outputStream.write(i);
            }
            outputStream.write(section.data);
        }
    }

    public static boolean writeXMPMeta(String str, XMPMeta xMPMeta) {
        Throwable e;
        OutputStream outputStream;
        Throwable th;
        if (str.toLowerCase().endsWith(".jpg") || str.toLowerCase().endsWith(".jpeg")) {
            try {
                List insertXMPSection = insertXMPSection(parse(new FileInputStream(str), false), xMPMeta);
                if (insertXMPSection == null) {
                    return false;
                }
                FileOutputStream fileOutputStream = null;
                try {
                    OutputStream fileOutputStream2 = new FileOutputStream(str);
                    try {
                        writeJpegFile(fileOutputStream2, insertXMPSection);
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (IOException e2) {
                            }
                        }
                        return true;
                    } catch (IOException e3) {
                        e = e3;
                        outputStream = fileOutputStream2;
                        try {
                            Log.m1d("XmpUtil", "Write file failed:" + str, e);
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e4) {
                                }
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e5) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        outputStream = fileOutputStream2;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e6) {
                    e = e6;
                    Log.m1d("XmpUtil", "Write file failed:" + str, e);
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    return false;
                }
            } catch (Throwable e7) {
                Log.m3e("XmpUtil", "Could not read file: " + str, e7);
                return false;
            }
        }
        Log.m0d("XmpUtil", "XMP parse: only jpeg file is supported");
        return false;
    }
}
