package com.anlia.photofactory.worker;

import android.app.Activity;
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

public class CameraWorker extends BaseWorker{
    public CameraWorker(Activity activity, Uri uri){
        super(activity, uri);
        mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        REQUEST_CODE = PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS;
    }

    public CameraWorker AddOutPutExtra(){
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        REQUEST_CODE = PhotoFactory.TYPE_PHOTO_UNTREATED;
        return this;
    }

    public CameraWorker AddOutPutExtra(String photoPath){
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, UriUtils.GetFileUri(mActivity,new File(photoPath)));
        REQUEST_CODE = PhotoFactory.TYPE_PHOTO_UNTREATED;
        return this;
    }
}
