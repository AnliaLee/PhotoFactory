package com.anlia.photofactory.base;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.anlia.photofactory.factory.PhotoFactory;

/**
 * Created by anlia on 2018/5/16.
 */

public abstract class BaseWorker {
    protected Uri mUri;
    protected Intent mIntent;
    protected Activity mActivity;
    protected int REQUEST_CODE = PhotoFactory.TYPE_ERROR;

    public BaseWorker(Activity activity, Uri uri){
        mActivity = activity;
        mUri = uri;
        mIntent = new Intent();
    }

    public void Start(){
        mActivity.startActivityForResult(mIntent, REQUEST_CODE);
    }
}
