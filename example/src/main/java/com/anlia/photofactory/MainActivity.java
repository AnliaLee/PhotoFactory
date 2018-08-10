package com.anlia.photofactory;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.result.ResultData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private ImageView imgPhoto;
    private Button btnGallery;
    private Button btnCamera;

    private PhotoFactory photoFactory;

    private String picName;
    private final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);
        btnGallery = findViewById(R.id.btn_gallery);
        btnCamera = findViewById(R.id.btn_camera);

        picName = Calendar.getInstance().getTimeInMillis() + ".png";

        photoFactory = new PhotoFactory(this, Environment.getExternalStorageDirectory() + "/" + "DCIM", picName);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //申请写入权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 100);
                } else {
                    photoFactory.FromGallery()
                            .StartForResult(new PhotoFactory.OnResultListener() {
                                @Override
                                public void OnCancel() {
                                    Log.e(TAG, "取消从相册选择");
                                }

                                @Override
                                public void OnSuccess(ResultData resultData) {
                                    dealSelectPhoto(resultData);
//                            Uri uri = resultData.GetUri();
//                            imgPhoto.setImageURI(uri);
                                }
                            });
                }
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //申请写入权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 101);
                } else {
                    photoFactory.FromCamera()
                            .AddOutPutExtra()
                            .StartForResult(new PhotoFactory.OnResultListener() {
                                @Override
                                public void OnCancel() {
                                    Log.e(TAG, "取消从相册选择");
                                }

                                @Override
                                public void OnSuccess(ResultData resultData) {
                                    dealSelectPhoto(resultData);
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
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
                                    dealSelectPhoto(resultData);
//                                    Uri uri = resultData.GetUri();
//                                    imgPhoto.setImageURI(uri);
                                }
                            });
                } else {// 没有获取到权限，做特殊处理
                    Toast.makeText(this, "请授予权限！", Toast.LENGTH_SHORT).show();
                }
                break;

            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFactory.FromCamera()
                            .AddOutPutExtra()
                            .StartForResult(new PhotoFactory.OnResultListener() {
                                @Override
                                public void OnCancel() {
                                    Log.e(TAG, "取消从相册选择");
                                }

                                @Override
                                public void OnSuccess(ResultData resultData) {
                                    dealSelectPhoto(resultData);
                                }
                            });
                } else {// 没有获取到权限，做特殊处理
                    Toast.makeText(this, "请授予权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void dealSelectPhoto(ResultData resultData) {
        photoFactory.FromCrop(resultData.addScaleCompress(1000, 1000).GetUri())
                .AddAspectX(1)
                .AddAspectY(1)
                .StartForResult(new PhotoFactory.OnResultListener() {
                    @Override
                    public void OnCancel() {
                        Log.e(TAG, "取消裁剪");
                    }

                    @Override
                    public void OnSuccess(ResultData data) {
                        dealCropPhoto(data.addScaleCompress(164, 164).GetBitmap());
                    }
                });
    }

    private void dealCropPhoto(Bitmap bitmap) {
        imgPhoto.setImageBitmap(bitmap);
    }

    private void dealCropPhoto(ResultData resultData) {
        try {
            final File file = new File(Environment.getExternalStorageDirectory() + "/" + "DCIM", picName);
            FileOutputStream fos = new FileOutputStream(file);
            Bitmap bitmap = resultData.addScaleCompress(164, 164).GetBitmap();
            imgPhoto.setImageBitmap(bitmap);
            fos.flush();
            fos.close();
        } catch (IOException e) {

        }

    }
}
