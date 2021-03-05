package com.king.app.coolg_kt.model.log;

import android.os.Environment;

import com.king.app.coolg_kt.conf.AppConfig;
import com.king.app.coolg_kt.utils.AppUtil;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/19 13:31
 */
public class LogWriter {

    private static LogWriter instance;

    private ExecutorService executorService;

    private LogWriter() {
        executorService = Executors.newFixedThreadPool(5);
    }

    public static LogWriter getInstance() {
        synchronized (LogWriter.class) {
            if (instance == null) {
                instance = new LogWriter();
            }
        }
        return instance;
    }

    public void log(String message) {
        executorService.execute(new LogThread(message));
    }

    private class LogThread implements Runnable {

        private String message;

        public LogThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            logMessage(message);
        }
    }

    private synchronized void logMessage(String msg) {
        BufferedWriter out = null;
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fileName = day + ".trace";
            //以当前时间创建log文件
            File f = new File(AppConfig.INSTANCE.getAPP_DIR_LOG());
            if (!f.exists()) {
                f.mkdirs();
            }
            File file = new File(f, fileName);
            String appVersion = null;
            if (!file.exists()) {
                // 新文件第一行写入版本信息
                appVersion = AppUtil.getAppVersionName();
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),"utf-8"));
            String time = new SimpleDateFormat("HH:mm:ss SSSS").format(System.currentTimeMillis());
            if (appVersion != null) {
                out.write("[AppVersion]" + appVersion + "\r\n");
            }
            out.write(time+":"+msg+"\r\n");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closed(out);
        }
    }

    private synchronized void closed(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
