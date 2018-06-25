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

        photoFactory = new PhotoFactory(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请写入权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        } else {
            photoFactory.FromGallery()
                    .StartForResult(new PhotoFactory.OnResultListener() {
                        @Override
                        public void OnCancel() {
                            Log.e(TAG,"取消从相册选择");
                        }

                        @Override
                        public void OnSuccess(ResultData resultData) {
                            Uri uri = resultData.GetUri();
                            imgPhoto.setImageURI(uri);
                        }
                    });

//            photoFactory.FromCamera()
//                    .AddOutPutExtra()
//                    .StartForResult(new PhotoFactory.OnResultListener() {
//                        @Override
//                        public void OnCancel() {
//                            Log.e(TAG,"取消从相册选择");
//                        }
//
//                        @Override
//                        public void OnSuccess(ResultData resultData) {
//                            photoFactory.FromCrop(resultData.GetUri())
//                                    .AddAspectX(1)
//                                    .AddAspectY(1)
//                                    .StartForResult(new PhotoFactory.OnResultListener() {
//                                        @Override
//                                        public void OnCancel() {
//                                            Log.e(TAG,"取消从相册选择");
//                                        }
//
//                                        @Override
//                                        public void OnSuccess(ResultData resultData) {
//                                            Uri uri = resultData.GetUri();
//                                            imgPhoto.setImageURI(uri);
//                                        }
//                                    });
//                        }
//                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFactory.FromGallery()
                            .StartForResult(new PhotoFactory.OnResultListener() {
                                @Override
                                public void OnCancel() {
                                    Log.e(TAG,"取消从相册选择");
                                }

                                @Override
                                public void OnSuccess(ResultData resultData) {
                                    Uri uri = resultData.GetUri();
                                    imgPhoto.setImageURI(uri);
                                }
                            });

//                    photoFactory.FromCamera()
//                            .AddOutPutExtra()
//                            .StartForResult(new PhotoFactory.OnResultListener() {
//                                @Override
//                                public void OnCancel() {
//                                    Log.e(TAG,"取消从相册选择");
//                                }
//
//                                @Override
//                                public void OnSuccess(ResultData resultData) {
//                                    photoFactory.FromCrop(resultData.GetUri())
//                                            .AddAspectX(1)
//                                            .AddAspectY(1)
//                                            .StartForResult(new PhotoFactory.OnResultListener() {
//                                                @Override
//                                                public void OnCancel() {
//
//                                                }
//
//                                                @Override
//                                                public void OnSuccess(ResultData resultData) {
//                                                    Uri uri = resultData.GetUri();
//                                                    imgPhoto.setImageURI(uri);
//                                                }
//                                            });
//                                }
//                            });
                }else{// 没有获取到权限，做特殊处理
                    Toast.makeText(this, "请授予权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
