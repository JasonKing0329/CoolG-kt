package com.king.app.coolg_kt.utils;

import com.king.app.coolg_kt.model.http.bean.response.PathResponse;
import com.king.app.coolg_kt.model.setting.SettingProperty;

import io.reactivex.rxjava3.core.Observable;

public class UrlUtil {

    public static String toVideoUrl(String subPath) {
        String baseUrl = formatUrl(SettingProperty.Companion.getServerUrl());
        return baseUrl + subPath.replaceAll(" ", "%20");
    }

    public static String formatUrl(String ip) {
        if (!ip.startsWith("http://")) {
            ip = "http://" + ip;
        }
        if (!ip.endsWith("/")) {
            ip = ip + "/";
        }
        return ip;
    }

    public static Observable<String> toVideoUrl(PathResponse response) {
        return Observable.create(e -> {
            if (response.isAvailable()) {
                String baseUrl = formatUrl(SettingProperty.Companion.getServerUrl());
                // url中不能包含空格，用%20来代替可以达到目的
                e.onNext(baseUrl + response.getPath().replaceAll(" ", "%20"));
            }
            else {
                e.onError(new Exception("Video source is unavailable"));
            }
        });
    }

}
