package com.anlia.photofactory.base;

import android.content.Context;
import android.net.Uri;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.utils.UriUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anlia on 2018/5/16.
 */

public abstract class BaseWorker {
    protected Uri mUri;
    protected Map<String,Object> mMap;
    protected Context mContext;
    protected String mPhotoDir;
    protected String mPhotoName;

    public BaseWorker(Context context, String photoDir, String photoName){
        mContext = context;
        mPhotoDir = photoDir;
        mPhotoName = photoName;

        mUri = UriUtils.GetFileUri(mContext,new File(mPhotoDir, mPhotoName));
        mMap = new HashMap<>();
    }

    public abstract void StartForResult(PhotoFactory.OnResultListener listener);
}
