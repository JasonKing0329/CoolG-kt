package com.king.app.coolg_kt.utils;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2021/12/6 13:52
 */
public interface ZipProgressListener {
    void onProgress(int fileCount, int total);
    void onComplete();
}
