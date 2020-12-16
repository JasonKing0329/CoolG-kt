package com.king.app.coolg_kt.utils;

import com.king.lib.banner.BannerFlipStyleProvider;
import com.king.lib.banner.CoolBanner;

import java.util.Random;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/12/16 10:30
 */
public class BannerHelper {
    public static void setBannerParams(CoolBanner banner, BannerParams params) {
        // 轮播切换时间
        banner.setDuration(params.getDuration());

        if (params.isRandom()) {
            Random random = new Random();
            int type = Math.abs(random.nextInt()) % BannerFlipStyleProvider.ANIM_TYPES.length;
            BannerFlipStyleProvider.setPagerAnim(banner, type);
        }
        else {
            BannerFlipStyleProvider.setPagerAnim(banner, params.getType());
        }
    }
    public static class BannerParams {

        private boolean isRandom = true;

        private int type;

        private int duration = 5000;

        public boolean isRandom() {
            return isRandom;
        }

        public void setRandom(boolean random) {
            isRandom = random;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
}
