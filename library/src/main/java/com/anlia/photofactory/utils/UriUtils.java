package com.anlia.photofactory.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".factory.provider", file);
        }
    }

    public static Uri GetUriForCrop(Context context, Uri uri){
        Bitmap bitmap = null;
        // 首先设置 inJustDecodeBounds=true 来获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AssetFileDescriptor fileDescriptor = null;
        try {
            // 根据计算出的 inSampleSize 来解码图片生成Bitmap
            options.inJustDecodeBounds = false;
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null));
    }
}
