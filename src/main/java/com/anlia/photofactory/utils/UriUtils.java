package com.anlia.photofactory.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by anlia on 2017/12/18.
 */

public class UriUtils {
    /**
     * 获取文件Uri地址
     * @param context
     * @param file
     * @return
     */
    public static Uri GetFileUri(Context context, File file){
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
            return Uri.fromFile(file);
        }else{
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        }
    }
}
