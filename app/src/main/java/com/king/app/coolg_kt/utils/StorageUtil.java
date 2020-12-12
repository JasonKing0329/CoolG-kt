package com.king.app.coolg_kt.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2017/7/18 10:56
 */
public class StorageUtil {

    /**
     * 内置SD卡
     * @return
     */
    public static String getInnerStoragePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 扩展SD卡路径，可能为null
     * @param context
     * @return
     */
    public static String getOutterStoragePath(Context context) {
        return getStoragePath(context, true);
    }

    /**
     *
     * @param mContext
     * @param is_removale true是指可移除的，即扩展SD卡，false则是不可移除的，即内置SD卡（和Environment.getExternalStorageDirectory().getPath一样）
     * @return
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
