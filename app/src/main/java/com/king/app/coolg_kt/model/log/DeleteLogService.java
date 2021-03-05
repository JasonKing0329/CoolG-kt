package com.king.app.coolg_kt.model.log;

import android.app.IntentService;
import android.content.Intent;

import com.king.app.coolg_kt.conf.AppConfig;
import com.king.app.coolg_kt.utils.DebugLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 描述:删除非3天内日志文件,在程序启动后的第一个界面启动即可
 * <p/>作者：wjx
 * <p/>创建时间: 2017/1/19 17:53
 */
public class DeleteLogService extends IntentService {
    private static final String TAG = "DeleteLogService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DeleteLogService() {
        super("DeleteLogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());
            calendar.add(GregorianCalendar.DAY_OF_YEAR, -9);// 只保留10天内的日志
            String dayStart = sdf.format(calendar.getTime());

            long timeStart = sdf.parse(dayStart).getTime();
            calendar.setTime(new Date());
            calendar.add(GregorianCalendar.DAY_OF_YEAR, 1);
            File[] files = new File(AppConfig.INSTANCE.getAPP_DIR_LOG()).listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    String name = file.getName();
                    if ("TCSLLOGS.zip".equals(name)) {
                        file.delete();
                        continue;
                    }
                    if (name.endsWith(".trace")) {
                        int index = name.lastIndexOf(".");
                        name = name.substring(0, index);
                    }
                    long time = sdf.parse(name).getTime();
                    if (time < timeStart) {
                        DebugLog.e("delete " + name);
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
