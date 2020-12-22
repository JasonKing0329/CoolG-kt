package com.king.app.coolg_kt.utils;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/7/30 9:16
 */
public class CostTimeUtil {

    private static long startTime;

    public static void start() {
        startTime = System.currentTimeMillis();
    }

    public static void end(String tag) {
        long end = System.currentTimeMillis();
        DebugLog.e("[" + tag + "] cost time " + (end - startTime));
    }
}
