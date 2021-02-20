/*
 *                       Copyright (C) of Avery
 *
 *                              _ooOoo_
 *                             o8888888o
 *                             88" . "88
 *                             (| -_- |)
 *                             O\  =  /O
 *                          ____/`- -'\____
 *                        .'  \\|     |//  `.
 *                       /  \\|||  :  |||//  \
 *                      /  _||||| -:- |||||-  \
 *                      |   | \\\  -  /// |   |
 *                      | \_|  ''\- -/''  |   |
 *                      \  .-\__  `-`  ___/-. /
 *                    ___`. .' /- -.- -\  `. . __
 *                 ."" '<  `.___\_<|>_/___.'  >'"".
 *                | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *                \  \ `-.   \_ __\ /__ _/   .-` /  /
 *           ======`-.____`-.___\_____/___.-`____.-'======
 *                              `=- -='
 *           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *              Buddha bless, there will never be bug!!!
 */

package com.avery.subtitle;

import android.util.Log;

import com.avery.subtitle.model.Subtitle;

import java.util.List;

/**
 * @author AveryZhong.
 */

public class SubtitleFinder {
    private SubtitleFinder() {
        throw new AssertionError("No instance for you");
    }

    public static Subtitle find(long position, List<Subtitle> subtitles) {
        if (subtitles == null || subtitles.isEmpty()) {
            return null;
        }
        int start = 0;
        int end = subtitles.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            Subtitle middleSubtitle = subtitles.get(middle);
            if (position < middleSubtitle.start.mseconds) {
                end = middle - 1;
            } else if (position > middleSubtitle.end.mseconds) {
                start = middle + 1;
            } else if (position >= middleSubtitle.start.mseconds
                    && position <= middleSubtitle.end.mseconds) {
                logSubTitle(position, middleSubtitle);
                return middleSubtitle;
            }
        }
        return null;
    }

    private static void logSubTitle(long position, Subtitle bean) {
        Log.e("Subtitle", "[find]position=" + position);
        Log.e("Subtitle", "return start=" + bean.start.mseconds + ", end=" + bean.end.mseconds + ", content=" + bean.content);
    }
}
