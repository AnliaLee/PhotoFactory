package com.anlia.photofactory.result;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.utils.CompressUtils;

import java.io.IOException;

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
     * @param w
     * @param h
     * @return
     */
    public ResultData addScaleCompress(int w, int h){
        try {
            if(isCompress){
                bitmap = CompressUtils.ScaleCompressFormBitmap(mActivity,bitmap,w,h);
            }else {
                isCompress = true;
                bitmap = CompressUtils.ScaleCompressFormUri(mActivity,mUri,h,w);
            }
        }catch (Exception e){
            e.printStackTrace();
            mCancelCode = PhotoFactory.CODE_CANCELED;
        }

        if (bitmap == null){//进行一次校验，防止特殊机型从相册获取图片点击取消时返回的状态不为CANCELED
            mCancelCode = PhotoFactory.CODE_CANCELED;
        }
        return this;
    }

    /**
     * 质量压缩
     * @param targetSize 目标大小
     * @return
     */
    public ResultData addQualityCompress(int targetSize){
        try {
            if(isCompress){
                bitmap = CompressUtils.QualityCompressFromBitmap(bitmap,targetSize);
            }else {
                isCompress = true;
                bitmap = CompressUtils.QualityCompressFromUri(mActivity,mUri,targetSize);
            }
        }catch (Exception e){
            e.printStackTrace();
            mCancelCode = PhotoFactory.CODE_CANCELED;
        }

        if (bitmap == null){//进行一次校验，防止特殊机型从相册获取图片点击取消时返回的状态不为CANCELED
            mCancelCode = PhotoFactory.CODE_CANCELED;
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
