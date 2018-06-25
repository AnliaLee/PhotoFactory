package com.anlia.photofactory.worker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.anlia.photofactory.FactoryHelperActivity;
import com.anlia.photofactory.base.BaseWorker;
import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.result.ResultData;

/**
 * Created by anlia on 2018/5/16.
 */

public class GalleryWorker extends BaseWorker {
    public GalleryWorker(Context context, String photoDir, String photoName) {
        super(context,photoDir,photoName);
    }

    @Override
    public void StartForResult(@NonNull final PhotoFactory.OnResultListener listener) {
        FactoryHelperActivity.selectPhotoFromGallery(mContext, new FactoryHelperActivity.ActivityResultListener() {
            @Override
            public void onResultCallback(int requestCode, int resultCode, Intent data) {
                if(data == null){
                    listener.OnCancel();
                }else {
                    mUri = data.getData();
                    listener.OnSuccess(new ResultData(mContext,mUri,requestCode,resultCode,data,PhotoFactory.CODE_SUCCESS));
                }
            }
        });
    }
}
