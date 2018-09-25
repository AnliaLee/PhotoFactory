package com.anlia.photofactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.anlia.photofactory.factory.PhotoFactory.ERROR_CAMERA_NOT_FOUND;
import static com.anlia.photofactory.factory.PhotoFactory.ERROR_PICK_NOT_FOUND;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_CROP;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_FROM_GALLERY;

/**
 * Created by anlia on 2018/6/16.
 */

public class FactoryHelperActivity extends Activity {
    private static ActivityResultListener mActivityResultListener;

    private Intent mRequestIntent;// FactoryHelperActivity 请求系统API的Intent
    private Intent mGetIntent;// Worker 与 FactoryHelperActivity 之间的中转Intent
    private Map<String, Object> mGetIntentParamMap;//存放GetIntent参数的集合

    private static final String KEY_JOB = "KEY_JOB";//GetIntent的任务类型
    private static final String KEY_PARAM = "KEY_PARAM";//GetIntent的携带参数
    private static final String KEY_REQUEST = "KEY_REQUEST";//GetIntent的请求码
    private static final int JOB_SELECT_PHOTO_FROM_GALLERY = 1;
    private static final int JOB_SELECT_PHOTO_FROM_CAMERA = 2;
    private static final int JOB_CROP_PHOTO = 3;

    public static void selectPhotoFromGallery(Context context, ActivityResultListener listener) {
        FactoryHelperActivity.mActivityResultListener = listener;

        Intent intent = new Intent(context, FactoryHelperActivity.class);
        intent.putExtra(KEY_JOB, JOB_SELECT_PHOTO_FROM_GALLERY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void selectPhotoFromCamera(Context context, Map<String, Object> map, int requestCode, ActivityResultListener listener) {
        FactoryHelperActivity.mActivityResultListener = listener;

        Intent intent = new Intent(context, FactoryHelperActivity.class);
        intent.putExtra(KEY_JOB, JOB_SELECT_PHOTO_FROM_CAMERA);
        intent.putExtra(KEY_PARAM, (Serializable) map);
        intent.putExtra(KEY_REQUEST, requestCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void cropPhoto(Context context, Map<String, Object> map, ActivityResultListener listener) {
        FactoryHelperActivity.mActivityResultListener = listener;

        Intent intent = new Intent(context, FactoryHelperActivity.class);
        intent.putExtra(KEY_JOB, JOB_CROP_PHOTO);
        intent.putExtra(KEY_PARAM, (Serializable) map);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestIntent = new Intent();
        mGetIntent = getIntent();
        mGetIntentParamMap = (HashMap) mGetIntent.getSerializableExtra(KEY_PARAM);

        if (mGetIntentParamMap != null) {
            for (Map.Entry<String, Object> entry : mGetIntentParamMap.entrySet()) {
                if (entry.getKey().equals("DataAndType")) {
                    continue;
                }

                if (entry.getValue() instanceof Uri) {
                    mRequestIntent.putExtra(entry.getKey(), (Uri) entry.getValue());
                } else if (entry.getValue() instanceof Integer) {
                    mRequestIntent.putExtra(entry.getKey(), (int) entry.getValue());
                }
            }
        }

        switch (mGetIntent.getIntExtra(KEY_JOB, 0)) {
            case JOB_SELECT_PHOTO_FROM_GALLERY:
                try {
                    mRequestIntent.setType("image/*");// 设置文件类型
                    mRequestIntent.setAction(Intent.ACTION_PICK);
                    mRequestIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(mRequestIntent, TYPE_PHOTO_FROM_GALLERY);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mActivityResultListener != null) {
                        mActivityResultListener.onResultCallback(0, 0, null, ERROR_PICK_NOT_FOUND);
                    }
                    finish();
                }
                break;
            case JOB_SELECT_PHOTO_FROM_CAMERA:
                try {
                    mRequestIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(mRequestIntent, mGetIntent.getIntExtra(KEY_REQUEST, -99));
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mActivityResultListener != null) {
                        mActivityResultListener.onResultCallback(0, 0, null, ERROR_CAMERA_NOT_FOUND);
                    }
                    finish();
                }
                break;
            case JOB_CROP_PHOTO:
                mRequestIntent.setAction("com.android.camera.action.CROP");
                mRequestIntent.putExtra("crop", "true");
                mRequestIntent.setDataAndType((Uri) mGetIntentParamMap.get("DataAndType"), "image/*");
                startActivityForResult(mRequestIntent, TYPE_PHOTO_CROP);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityResultListener != null) {
            mActivityResultListener.onResultCallback(requestCode, resultCode, data, null);
        }
        finish();
    }

    public interface ActivityResultListener {
        void onResultCallback(int requestCode, int resultCode, Intent data, String error);
    }
}
