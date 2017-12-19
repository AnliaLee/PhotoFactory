package com.anlia.photofactory.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by anlia on 2017/12/18.
 */

public class CompressUtils {
    /**
     * 按目标宽高缩放
     * @param activity
     * @param uri
     * @param newW 目标宽度
     * @param newH 目标高度
     * @return
     * @throws IOException
     */
    public static Bitmap ScaleCompressFormUri(Activity activity,Uri uri,float newW,float newH) throws IOException {
        InputStream input = activity.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return ScaleCompressFormBitmap(bitmap,newW,newH);
    }

    /**
     * 按目标宽高缩放
     * @param bitmap 目标bitmap
     * @param newW 目标宽度
     * @param newH 目标高度
     * @return
     */
    public static Bitmap ScaleCompressFormBitmap(Bitmap bitmap, float newW, float newH) {
        int oldW = bitmap.getWidth();
        int oldH = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = newW / oldW;
        float scaleHeight = newH / oldH;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, oldW, oldH, matrix, true);
        return bitmap;
    }

    /**
     * 等比例缩放
     * @param activity
     * @param uri
     * @param scale 缩放比例
     * @return
     * @throws IOException
     */
    public static Bitmap ScaleCompressFormUri(Activity activity,Uri uri,int scale) throws IOException {
        InputStream input = activity.getContentResolver().openInputStream(uri);
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = scale;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;
    }

    /**
     * 等比例缩放
     * @param bitmap
     * @param scale 目标比例
     * @return
     */
    public static Bitmap ScaleCompressFormBitmap(Bitmap bitmap, int scale) {
        int oldW = bitmap.getWidth();
        int oldH = bitmap.getHeight();
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        // 得到新的图片
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, oldW, oldH, matrix, true);
        return bitmap;
    }

    /**
     * 质量压缩
     * @param activity
     * @param uri
     * @param targetSize 目标大小
     * @return
     * @throws IOException
     */
    public static Bitmap QualityCompressFromUri(Activity activity,Uri uri,int targetSize) throws IOException{
        InputStream input = activity.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return QualityCompressFromBitmap(bitmap,targetSize);
    }

    /**
     * 质量压缩
     * @param image
     * @param targetSize 目标大小
     * @return
     */
    public static Bitmap QualityCompressFromBitmap(Bitmap image, int targetSize) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        int options = 100;
        while (byteArrayOutputStream.toByteArray().length / 1024 > targetSize) {  //循环判断如果压缩后图片是否大于targetSize,大于继续压缩
            byteArrayOutputStream.reset();
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream);//这里压缩options%，把压缩后的数据存放到byteArrayOutputStream中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());//把压缩后的数据存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}
