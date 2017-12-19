package com.anlia.photofactory.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

/**
 * Created by anlia on 2017/12/18.
 */

public class SystemUtils {
    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean HasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 有存储的SDCard
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context)
    {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
