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

import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_CROP;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_FROM_GALLERY;

/**
 * Created by anlia on 2018/6/16.
 */

public class FactoryHelperActivity extends Activity {
    private static ActivityResultListener mActivityResultListener;

    private static final String KEY_JOB = "KEY_JOB";
    private static final String KEY_PARAM = "KEY_PARAM";
    private static final String KEY_REQUEST = "KEY_REQUEST";
    private static final int JOB_SELECT_PHOTO_FROM_GALLERY = 1;
    private static final int JOB_SELECT_PHOTO_FROM_CAMERA = 2;
    private static final int JOB_CROP_PHOTO = 3;

    public static void selectPhotoFromGallery(Context context, ActivityResultListener listener){
        FactoryHelperActivity.mActivityResultListener = listener;

        Intent intent = new Intent(context,FactoryHelperActivity.class);
        intent.putExtra(KEY_JOB, JOB_SELECT_PHOTO_FROM_GALLERY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void selectPhotoFromCamera(Context context, Map<String,Object> map, int requestCode, ActivityResultListener listener){
        FactoryHelperActivity.mActivityResultListener = listener;

        Intent intent = new Intent(context,FactoryHelperActivity.class);
        intent.putExtra(KEY_JOB, JOB_SELECT_PHOTO_FROM_CAMERA);
        intent.putExtra(KEY_PARAM,(Serializable) map);
        intent.putExtra(KEY_REQUEST,requestCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void cropPhoto(Context context, Map<String,Object> map, ActivityResultListener listener){
        FactoryHelperActivity.mActivityResultListener = listener;

        Intent intent = new Intent(context,FactoryHelperActivity.class);
        intent.putExtra(KEY_JOB, JOB_CROP_PHOTO);
        intent.putExtra(KEY_PARAM,(Serializable) map);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        Intent requestIntent = new Intent();
        Map<String,Object> map = (HashMap) intent.getSerializableExtra(KEY_PARAM);
        if(map!=null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if(entry.getValue().equals("DataAndType")){
                    continue;
                }

                if(entry.getValue() instanceof Uri){
                    requestIntent.putExtra(entry.getKey(),(Uri) entry.getValue());
                }else if(entry.getValue() instanceof Integer){
                    requestIntent.putExtra(entry.getKey(),(int) entry.getValue());
                }
            }
        }

        switch (intent.getIntExtra(KEY_JOB, 0)){
            case JOB_SELECT_PHOTO_FROM_GALLERY:
                requestIntent.setType("image/*");// 设置文件类型
                requestIntent.setAction(Intent.ACTION_PICK);
                requestIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(requestIntent,TYPE_PHOTO_FROM_GALLERY);
                break;
            case JOB_SELECT_PHOTO_FROM_CAMERA:
                requestIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(requestIntent,intent.getIntExtra(KEY_REQUEST,-99));
                break;

            case JOB_CROP_PHOTO:
                requestIntent.setAction("com.android.camera.action.CROP");
                requestIntent.putExtra("crop", "true");
                requestIntent.setDataAndType((Uri) map.get("DataAndType"),"image/*");
                startActivityForResult(requestIntent,TYPE_PHOTO_CROP);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mActivityResultListener!=null){
            mActivityResultListener.onResultCallback(requestCode,resultCode,data);
        }
        finish();
    }

    public interface ActivityResultListener{
        void onResultCallback(int requestCode, int resultCode, Intent data);
    }
}
