package com.anlia.photofactory.worker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.anlia.photofactory.base.BaseWorker;
import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.utils.UriUtils;

import java.io.File;

/**
 * Created by anlia on 2018/5/16.
 */

public class CropWorker extends BaseWorker {
    private Uri cropData;
    private Context mContext;

    public CropWorker(Activity activity, Uri uri, Context context, String photoPath, String photoName) {
        super(activity, uri);
        mContext = context;
        mIntent.setAction("com.android.camera.action.CROP");
        mIntent.putExtra("crop", "true");
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoPath,photoName)));//缓存裁剪图片得用这种格式的uri
        REQUEST_CODE = PhotoFactory.TYPE_PHOTO_CROP;
    }

    public CropWorker SetCropData(Uri data){
        cropData = UriUtils.GetUriForCrop(mContext,data);
        mIntent.setDataAndType(cropData, "image/*");
        return this;
    }

    public CropWorker AddAspectX(int value){
        mIntent.putExtra("aspectX",value);
        return this;
    }

    public CropWorker AddAspectY(int value){
        mIntent.putExtra("aspectY",value);
        return this;
    }

    @Override
    public void Start() {
        if(cropData == null){
            throw new NullPointerException("必须调用SetCropData设置需要裁剪图片的位置");
        }
        mActivity.startActivityForResult(mIntent, REQUEST_CODE);
    }
}
