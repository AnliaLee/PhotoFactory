package com.anlia.photofactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.permission.PermissionAlwaysDenied;
import com.anlia.photofactory.result.ResultData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static com.anlia.photofactory.factory.PhotoFactory.ERROR_CROP_DATA;

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

        PhotoFactory.setPermissionAlwaysDeniedAction(new PermissionAlwaysDenied.Action() {
            @Override
            public void onAction(Context context, List<String> permissions, final PermissionAlwaysDenied.Executor executor) {
                List<String> permissionNames = PhotoFactory.transformPermissionText(context, permissions);
                String permissionText = TextUtils.join("权限\n", permissionNames);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("权限说明");
                builder.setMessage("您禁止了以下权限的动态申请：\n\n" + permissionText + "权限\n\n是否去应用权限管理中手动授权呢？");
                builder.setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.toSetting();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoFactory = new PhotoFactory(MainActivity.this, Environment.getExternalStorageDirectory() + "/" + "DCIM", picName);
                photoFactory.FromGallery()
                        .StartForResult(new PhotoFactory.OnResultListener() {
                            @Override
                            public void onCancel() {
                                Log.e(TAG, "取消从相册选择");
                            }

                            @Override
                            public void onSuccess(ResultData resultData) {
                                dealSelectPhoto(resultData);
//                            Uri uri = resultData.GetUri();
//                            imgPhoto.setImageURI(uri);
                            }

                            @Override
                            public void onError(String error) {

                            }
                        });
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoFactory = new PhotoFactory(MainActivity.this, Environment.getExternalStorageDirectory() + "/" + "DCIM", picName);
                photoFactory.FromCamera()
                        .AddOutPutExtra()
                        .StartForResult(new PhotoFactory.OnResultListener() {
                            @Override
                            public void onCancel() {
                                Log.e(TAG, "取消从相册选择");
                            }

                            @Override
                            public void onSuccess(ResultData resultData) {
                                dealSelectPhoto(resultData);
                            }

                            @Override
                            public void onError(String error) {

                            }
                        });
            }
        });
    }

    private void dealSelectPhoto(ResultData resultData) {
        Uri uri = resultData
                .setExceptionListener(new ResultData.OnExceptionListener() {
                    @Override
                    public void onCatch(String error, Exception e) {
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                })
                .GetUri();
        photoFactory.FromCrop(uri)
                .AddAspectX(1)
                .AddAspectY(1)
                .StartForResult(new PhotoFactory.OnResultListener() {
                    @Override
                    public void onCancel() {
                        Log.e(TAG, "取消裁剪");
                    }

                    @Override
                    public void onSuccess(ResultData data) {
                        dealCropPhoto(data.addScaleCompress(164, 164).GetBitmap());
                    }

                    @Override
                    public void onError(String error) {
                        switch (error) {
                            case ERROR_CROP_DATA:
                                Toast.makeText(MainActivity.this, "data为空", Toast.LENGTH_SHORT).show();
                                break;
                        }
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
