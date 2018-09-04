package com.anlia.photofactory.factory;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.LoaderManager;

import com.anlia.photofactory.result.ResultData;
import com.anlia.photofactory.utils.SystemUtils;
import com.anlia.photofactory.worker.CameraWorker;
import com.anlia.photofactory.worker.CropWorker;
import com.anlia.photofactory.worker.GalleryWorker;
import com.anlia.photofactory.worker.SearchWorker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anlia on 2017/12/18.
 */

public class PhotoFactory {
    private Context mContext;

    private String mPhotoDir;
    private String mPhotoName;

    public static final int TYPE_PHOTO_AUTO_COMPRESS = 100;
    public static final int TYPE_PHOTO_UNTREATED = 101;
    public static final int TYPE_PHOTO_FROM_GALLERY = 102;
    public static final int TYPE_PHOTO_CROP = 103;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_CANCELED = 201;

    public static final String ERROR_CROP_DATA = "ERROR_CROP_DATA";//获取裁剪图片uri时出现异常，通常发生在手机存储空间不足、存储空间被占用或oom的情况
    public static final String ERROR_RESULT_DATA = "ERROR_RESULT_DATA";//从ActivityResult的intent中获取数据出现异常
    public static final String ERROR_CAMERA_NOT_FOUND = "ERROR_CAMERA_NOT_FOUND";//寻找照相设备异常，通常出现在一些没有相机的android设备上
    public static final String ERROR_MEDIA_INSERT_IMAGE = "ERROR_MEDIA_INSERT_IMAGE";//插入图片异常，通常发生在手机存储空间不足或存储空间被占用的情况
    public static final String ERROR_MEDIA_GET_BITMAP = "ERROR_MEDIA_GET_BITMAP";//获取bitmap异常，通常发生在通过uri查找不到对应照片的情况
    public static final String ERROR_COMPRESS = "ERROR_COMPRESS";//压缩图片异常，通常发生在某些配置较低的机型内存不足的情况
    public static final String ERROR_PICK_NOT_FOUND = "ERROR_PICK_NOT_FOUND";//启动系统相册失败

    public PhotoFactory(Context context){
        this(context, Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ "DCIM" +File.separator,
                "original_"+System.currentTimeMillis()+ ".png");
    }

    public PhotoFactory(Context context, String photoDir, String photoName){
        mContext = context;
        mPhotoDir = photoDir;
        mPhotoName = photoName;

        if(!SystemUtils.HasSdcard()){
            throw new NullPointerException("SD卡读取失败");
        }else {
            File dir= new File(mPhotoDir);
            if (!dir.exists()) {
                dir.mkdirs();// 创建照片的存储目录
            }
        }
    }

    /**
     * 初始化相机照相Worker
     * @return
     */
    public CameraWorker FromCamera(){
        return new CameraWorker(mContext,mPhotoDir,mPhotoName);
    }

    /**
     * 初始化相册选图Worker
     * @return
     */
    public GalleryWorker FromGallery(){
        return new GalleryWorker(mContext,mPhotoDir,mPhotoName);
    }

    /**
     * 初始化裁剪Worker
     * @param data
     * @return
     */
    public CropWorker FromCrop(Uri data){
        return new CropWorker(mContext, mPhotoDir, mPhotoName, data);
    }

    /**
     * 初始化图片搜索Worker
     * @param loaderManager
     * @param applicationContext
     * @param projection 加载数据的映射（MediaStore.Images.Media.DATA等）
     * @return
     */
    public SearchWorker FromSearch(LoaderManager loaderManager, Context applicationContext, String[] projection){
        Map<String,Object> map = new HashMap<>();
        map.put("lm",loaderManager);
        map.put("ac",applicationContext);
        map.put("isQueryByFormat",false);
        map.put("selections",new String[]{""});
        map.put("projection",projection);
        return new SearchWorker(map);
    }

    public interface OnResultListener {
        void onCancel();
        void onSuccess(ResultData resultData);
        void onError(String error);
    }
}
