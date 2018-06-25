package com.anlia.photofactory.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by anlia on 2017/12/18.
 */

public class CompressUtils {
    private static int calculateNewBitmapSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        if (height == 0 || width == 0) inSampleSize = 5;
        return inSampleSize;
    }

    /**
     * 按目标宽高缩放
     * @param context
     * @param uri
     * @param newW 目标宽度
     * @param newH 目标高度
     * @return
     * @throws IOException
     */
    public static Bitmap ScaleCompressFormUri(Context context,Uri uri,float newW,float newH) throws IOException {
        Bitmap bitmap = null;
        // 首先设置 inJustDecodeBounds=true 来获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        AssetFileDescriptor fileDescriptor = null;
        try {
            // 计算 inSampleSize 的值
            options.inSampleSize = calculateNewBitmapSize(options,(int) newW,(int) newH);

            // 根据计算出的 inSampleSize 来解码图片生成Bitmap
            options.inJustDecodeBounds = false;
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return bitmap;
    }

    /**
     * 按目标宽高缩放
     * @param context
     * @param bitmap 目标bitmap
     * @param newW 目标宽度
     * @param newH 目标高度
     * @return
     * @throws IOException
     */
    public static Bitmap ScaleCompressFormBitmap(Context context, Bitmap bitmap, float newW, float newH) throws IOException{
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(cr, bitmap, null, null));
        return ScaleCompressFormUri(context,uri,newW,newH);
    }

    /**
     * 质量压缩
     * @param context
     * @param uri
     * @param targetSize 目标大小
     * @return
     * @throws IOException
     */
    public static Bitmap QualityCompressFromUri(Context context,Uri uri,int targetSize) throws IOException{
        InputStream input = context.getContentResolver().openInputStream(uri);
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
