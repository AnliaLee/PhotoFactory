package com.anlia.photofactory.worker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.anlia.photofactory.FactoryHelperActivity;
import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.result.ResultData;

import static android.app.Activity.RESULT_OK;

/**
 * Created by anlia on 2018/5/16.
 */

public class GalleryWorker extends BaseWorker {
    public GalleryWorker(Context context, String photoDir, String photoName) {
        super(context, photoDir, photoName);
    }

    @Override
    protected void doInGranted(final PhotoFactory.OnResultListener listener) {
        FactoryHelperActivity.selectPhotoFromGallery(mContext, new FactoryHelperActivity.ActivityResultListener() {
            @Override
            public void onResultCallback(int requestCode, int resultCode, Intent data, String error) {
                if (error != null) {
                    listener.onError(error);
                }

                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        listener.onCancel();
                    } else {
                        mUri = data.getData();
                        listener.onSuccess(new ResultData(mContext, mUri, requestCode, resultCode, data, PhotoFactory.CODE_SUCCESS));
                    }
                } else {
                    listener.onCancel();
                }
            }
        });
    }
}
