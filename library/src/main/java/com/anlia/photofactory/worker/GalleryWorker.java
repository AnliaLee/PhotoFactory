package com.anlia.photofactory.worker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.anlia.photofactory.base.BaseWorker;
import com.anlia.photofactory.factory.PhotoFactory;

/**
 * Created by anlia on 2018/5/16.
 */

public class GalleryWorker extends BaseWorker {
    public GalleryWorker(Activity activity, Uri uri) {
        super(activity, uri);
        mIntent.setType("image/*");// 设置文件类型
        mIntent.setAction(Intent.ACTION_PICK);
        mIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        REQUEST_CODE = PhotoFactory.TYPE_PHOTO_FROM_GALLERY;
    }
}
