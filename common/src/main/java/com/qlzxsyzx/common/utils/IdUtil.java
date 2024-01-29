package com.qlzxsyzx.common.utils;

public class IdUtil {
    public static String getUUID() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }
}
