package com.anlia.photofactory.result;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.utils.CompressUtils;

/**
 * Created by anlia on 2018/5/16.
 */

public class ResultData {
    private Intent mData;
    private Bitmap bitmap = null;
    private Activity mActivity;
    private Uri mUri;
    private PhotoFactory.OnResultListener mOnResultListener;

    private int mRequestCode;
    private int mResultCode;
    private int mCancelCode;
    private boolean isCompress = false;

    public ResultData(Activity activity, Uri uri, Context context, int requestCode, int resultCode, Intent data, int cancelCode){
        mActivity = activity;
        mUri = uri;
        mRequestCode = requestCode;
        mResultCode = resultCode;
        mData = data;
        mCancelCode = cancelCode;
    }

    public void setOnResultListener(PhotoFactory.OnResultListener mOnResultListener){
        this.mOnResultListener = mOnResultListener;
        if(mCancelCode == PhotoFactory.CODE_SUCCESS){
            this.mOnResultListener.OnSuccess(this);
        }else if(mCancelCode == PhotoFactory.CODE_CANCELED){
            this.mOnResultListener.OnCancel();
        }
    }

    /**
     * 按目标宽高缩放
     * @see #addScaleCompress(int, int, boolean)
     */
    public ResultData addScaleCompress(int w, int h){
        return addScaleCompress(w,h,false);
    }

    /**
     * 按目标宽高缩放
     * @param w
     * @param h
     * @param isAccurate 是否精确压缩至新的尺寸
     * @return
     */
    public ResultData addScaleCompress(int w, int h, boolean isAccurate){
        if(isCompress){
            bitmap = CompressUtils.ScaleCompressFormBitmap(bitmap,w,h,isAccurate);
        }else {
            try {
                isCompress = true;
                bitmap = CompressUtils.ScaleCompressFormUri(mActivity,mUri,h,w,isAccurate);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 等比例缩放
     * @param scale 压缩比
     * @return
     */
    public ResultData addScaleCompress(int scale){
        if(isCompress){
            bitmap = CompressUtils.ScaleCompressFormBitmap(bitmap,scale);
        }else {
            try {
                isCompress = true;
                bitmap = CompressUtils.ScaleCompressFormUri(mActivity,mUri,scale);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 质量压缩
     * @param targetSize 目标大小
     * @return
     */
    public ResultData addQualityCompress(int targetSize){
        if(isCompress){
            bitmap = CompressUtils.QualityCompressFromBitmap(bitmap,targetSize);
        }else {
            try {
                isCompress = true;
                bitmap = CompressUtils.QualityCompressFromUri(mActivity,mUri,targetSize);
            }catch (Exception e){

            }
        }
        return this;
    }

    public Bitmap GetBitmap(){
        if(mCancelCode != PhotoFactory.CODE_SUCCESS){
            return null;
        }
        switch (mRequestCode){
            case PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS:
                try{
                    bitmap = mData.getParcelableExtra("data");
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
            case PhotoFactory.TYPE_PHOTO_UNTREATED:
            case PhotoFactory.TYPE_PHOTO_FROM_GALLERY:
            case PhotoFactory.TYPE_PHOTO_CROP:
                if(!isCompress){
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), mUri);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
        return bitmap;
    }

    public Uri GetUri(){
        if(mCancelCode != PhotoFactory.CODE_SUCCESS){
            return null;
        }
        switch (mRequestCode){
            case PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS:
                try{
                    bitmap = mData.getParcelableExtra("data");
                    mUri = Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bitmap, null,null));
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
            case PhotoFactory.TYPE_PHOTO_UNTREATED:
            case PhotoFactory.TYPE_PHOTO_FROM_GALLERY:
            case PhotoFactory.TYPE_PHOTO_CROP:
                if(isCompress){
                    mUri = Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bitmap, null,null));
                }
                break;
        }
        return mUri;
    }
}
