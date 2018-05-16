package com.anlia.photofactory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.result.ResultData;

public class MainActivity extends AppCompatActivity {
    private ImageView imgPhoto;

    private PhotoFactory photoFactory;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);

        photoFactory = new PhotoFactory(this,this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请写入权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        } else {
            photoFactory.FactoryStart()
                    .FromCamera()
                    .AddOutPutExtra()
                    .Start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFactory.FactoryStart()
                            .FromCamera()
                            .AddOutPutExtra()
                            .Start();
                }else{// 没有获取到权限，做特殊处理
                    Toast.makeText(this, "请授予权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //也这样调用setOnResultListener一步到位了
        if(requestCode == PhotoFactory.TYPE_PHOTO_UNTREATED){
            photoFactory.FactoryFinish(requestCode,resultCode,data)
                    .addScaleCompress(1000,1000)
                    .setOnResultListener(new PhotoFactory.OnResultListener() {
                        @Override
                        public void OnCancel() {
                            Log.e(TAG,"取消拍照");
                        }

                        @Override
                        public void OnSuccess(ResultData resultData) {
                            Uri uri = resultData.GetUri();
                            photoFactory.FactoryStart()
                                    .FromCrop()
                                    .SetCropData(uri)
                                    .AddAspectY(1)
                                    .AddAspectX(1)
                                    .Start();
                        }
                    });
        }else if(requestCode == PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS){
            photoFactory.FactoryFinish(requestCode,resultCode,data)
                    .setOnResultListener(new PhotoFactory.OnResultListener() {
                        @Override
                        public void OnCancel() {
                            Log.e(TAG,"取消拍照（自动压缩）");
                        }

                        @Override
                        public void OnSuccess(ResultData resultData) {
                            Uri uri = resultData.GetUri();
                            photoFactory.FactoryStart()
                                    .FromCrop()
                                    .SetCropData(uri)
                                    .AddAspectY(1)
                                    .AddAspectX(1)
                                    .Start();
                        }
                    });
        }else if(requestCode == PhotoFactory.TYPE_PHOTO_FROM_GALLERY){
            photoFactory.FactoryFinish(requestCode,resultCode,data)
                    .setOnResultListener(new PhotoFactory.OnResultListener() {
                        @Override
                        public void OnCancel() {
                            Log.e(TAG,"取消从相册选择");
                        }

                        @Override
                        public void OnSuccess(ResultData resultData) {
                            Uri uri = resultData.GetUri();
                            photoFactory.FactoryStart()
                                    .FromCrop()
                                    .SetCropData(uri)
                                    .AddAspectY(1)
                                    .AddAspectX(1)
                                    .Start();
                        }
                    });
        }else if(requestCode == PhotoFactory.TYPE_PHOTO_CROP){
            photoFactory.FactoryFinish(requestCode,resultCode,data)
                    .addScaleCompress(164,164)
                    .setOnResultListener(new PhotoFactory.OnResultListener() {
                        @Override
                        public void OnCancel() {
                            Log.e(TAG,"取消裁剪");
                        }

                        @Override
                        public void OnSuccess(ResultData resultData) {
//                            Uri uri = resultData.GetUri();
//                            imgPhoto.setImageURI(uri);

//                            imgPhoto.setImageBitmap(resultData.GetBitmap());
                            imgPhoto.setImageURI(resultData.GetUri());
                        }
                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
