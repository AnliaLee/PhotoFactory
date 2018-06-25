package com.anlia.photofactory.worker;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.anlia.photofactory.FactoryHelperActivity;
import com.anlia.photofactory.base.BaseWorker;
import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.result.ResultData;

import java.io.File;

import static com.anlia.photofactory.factory.PhotoFactory.CODE_SUCCESS;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_UNTREATED;

/**
 * Created by anlia on 2018/5/16.
 */

public class CameraWorker extends BaseWorker{
    private int mRequestCode;

    public CameraWorker(Context context, String photoDir, String photoName){
        super(context,photoDir,photoName);
        mRequestCode = TYPE_PHOTO_AUTO_COMPRESS;
    }

    public CameraWorker AddOutPutExtra(){
        mMap.put(MediaStore.EXTRA_OUTPUT,mUri);
        mRequestCode = TYPE_PHOTO_UNTREATED;
        return this;
    }

    @Override
    public void StartForResult(@NonNull final PhotoFactory.OnResultListener listener) {
        FactoryHelperActivity.selectPhotoFromCamera(mContext,mMap,mRequestCode,new FactoryHelperActivity.ActivityResultListener() {
            @Override
            public void onResultCallback(int requestCode, int resultCode, Intent data) {
                if(requestCode == TYPE_PHOTO_UNTREATED){
                    File photo = new File(mPhotoDir,mPhotoName);
                    if(!photo.exists()){
                        listener.OnCancel();
                    }else {
                        ShowPhotoInGallery();
                        listener.OnSuccess(new ResultData(mContext,mUri,requestCode,resultCode,data,CODE_SUCCESS));
                    }
                }else if(requestCode == TYPE_PHOTO_AUTO_COMPRESS){
                    if(data == null){
                        listener.OnCancel();
                    }else {
                        listener.OnSuccess(new ResultData(mContext,mUri,requestCode,resultCode,data,CODE_SUCCESS));
                    }
                }
            }
        });
    }

    /**
     * 在手机相册中显示刚拍摄的图片
     */
    private void ShowPhotoInGallery(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mUri);
        mContext.sendBroadcast(mediaScanIntent);
    }
}
