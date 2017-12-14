package com.android.camera.aosp_porting;

import android.util.Log;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectUtil {
    private static Map<Character, Class> BASIC_TYPES = new HashMap();

    static {
        BASIC_TYPES.put(Character.valueOf('V'), Void.TYPE);
        BASIC_TYPES.put(Character.valueOf('Z'), Boolean.TYPE);
        BASIC_TYPES.put(Character.valueOf('B'), Byte.TYPE);
        BASIC_TYPES.put(Character.valueOf('C'), Character.TYPE);
        BASIC_TYPES.put(Character.valueOf('S'), Short.TYPE);
        BASIC_TYPES.put(Character.valueOf('I'), Integer.TYPE);
        BASIC_TYPES.put(Character.valueOf('J'), Long.TYPE);
        BASIC_TYPES.put(Character.valueOf('F'), Float.TYPE);
        BASIC_TYPES.put(Character.valueOf('D'), Double.TYPE);
    }

    public static Object callMethod(Class<?> cls, Object obj, String str, String str2, Object... objArr) {
        try {
            Method method = cls.getMethod(str, parseTypesFromSignature(str2));
            method.setAccessible(true);
            return method.invoke(obj, objArr);
        } catch (Throwable e) {
            Log.w("Camera", "ReflectUtil#callMethod ", e);
            return null;
        } catch (Throwable e2) {
            Log.w("Camera", "ReflectUtil#callMethod ", e2);
            return null;
        } catch (Throwable e3) {
            Log.w("Camera", "ReflectUtil#callMethod ", e3);
            return null;
        } catch (Throwable e4) {
            Log.w("Camera", "ReflectUtil#callMethod ", e4);
            return null;
        }
    }

    public static int getFieldInt(Class<?> cls, Object obj, String str, int i) {
        Object fieldValue = getFieldValue(cls, obj, str, "I");
        return fieldValue == null ? i : ((Integer) fieldValue).intValue();
    }

    public static Object getFieldValue(Class<?> cls, Object obj, String str, String str2) {
        Field declaredField;
        try {
            declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (NoSuchFieldException e) {
            try {
                declaredField = cls.getField(str);
                declaredField.setAccessible(true);
                return declaredField.get(obj);
            } catch (Throwable e2) {
                Log.w("Camera", "ReflectUtil#getFieldValue ", e2);
                return null;
            } catch (Throwable e3) {
                Log.w("Camera", "ReflectUtil#getFieldValue ", e3);
                return null;
            }
        } catch (Throwable e4) {
            Log.w("Camera", "ReflectUtil#getFieldValue ", e4);
            return null;
        }
    }

    private static Class<?>[] parseTypesFromSignature(String str) throws ClassNotFoundException {
        if (str == null || str == "") {
            return null;
        }
        String substring = str.substring(str.indexOf(40) + 1, str.indexOf(41));
        if (substring == null || substring == "") {
            return null;
        }
        int i;
        List arrayList = new ArrayList();
        int i2 = -1;
        Object obj = -1;
        Object obj2 = null;
        for (i = 0; i < substring.length(); i++) {
            char charAt = substring.charAt(i);
            if (i2 < 0 && BASIC_TYPES.containsKey(Character.valueOf(charAt))) {
                if (obj2 != null) {
                    arrayList.add(Array.newInstance((Class) BASIC_TYPES.get(Character.valueOf(charAt)), 0).getClass());
                } else {
                    arrayList.add((Class) BASIC_TYPES.get(Character.valueOf(charAt)));
                }
                obj2 = null;
            } else if (charAt == '[') {
                obj2 = 1;
            } else if (charAt == 'L') {
                if (obj == -1 && i2 == -1) {
                    i2 = i;
                }
            } else if (charAt == ';') {
                int i3 = i;
                String replaceAll = substring.substring(i2 + 1, i).replaceAll("/", ".");
                if (obj2 != null) {
                    arrayList.add(Array.newInstance(Class.forName(replaceAll), 0).getClass());
                } else {
                    arrayList.add(Class.forName(replaceAll));
                }
                obj2 = null;
                i2 = -1;
                obj = -1;
            }
        }
        Class<?>[] clsArr = new Class[arrayList.size()];
        for (i = 0; i < arrayList.size(); i++) {
            clsArr[i] = (Class) arrayList.get(i);
        }
        return clsArr;
    }
}
