package com.king.app.coolg_kt.view.widget.video;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 15:29
 */
public interface OnVideoListener {
    int getStartSeek();
    void updatePlayPosition(long currentPosition);
    void onPlayComplete();
    void onPause();
    void onDestroy();
    void onStart();
}
