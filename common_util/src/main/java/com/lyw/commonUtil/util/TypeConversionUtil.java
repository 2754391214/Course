package com.lyw.commonUtil.util;

public class TypeConversionUtil {
    public static Long toLong(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof String) {
            return Long.parseLong((String) obj);
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else {
            throw new IllegalArgumentException("无法将类型 " + obj.getClass() + " 转换为 Long");
        }
    }
}
