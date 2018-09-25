package com.anlia.photofactory.worker;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.anlia.photofactory.FactoryHelperActivity;
import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.permission.PermissionAlwaysDenied;
import com.anlia.photofactory.result.ResultData;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.anlia.photofactory.factory.PhotoFactory.CODE_SUCCESS;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS;
import static com.anlia.photofactory.factory.PhotoFactory.TYPE_PHOTO_UNTREATED;

/**
 * Created by anlia on 2018/5/16.
 */

public class CameraWorker extends BaseWorker {
    private int mRequestCode;

    public CameraWorker(Context context, String photoDir, String photoName) {
        super(context, photoDir, photoName);
        mRequestCode = TYPE_PHOTO_AUTO_COMPRESS;
    }

    public CameraWorker AddOutPutExtra() {
        mMap.put(MediaStore.EXTRA_OUTPUT, mUri);
        mRequestCode = TYPE_PHOTO_UNTREATED;
        return this;
    }

    @Override
    protected void initPermissions() {
        requestPermissions = new String[]{Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE};
    }

    @Override
    protected void doInGranted(@NonNull final PhotoFactory.OnResultListener listener) {
        FactoryHelperActivity.selectPhotoFromCamera(mContext, mMap, mRequestCode, new FactoryHelperActivity.ActivityResultListener() {
            @Override
            public void onResultCallback(int requestCode, int resultCode, Intent data, String error) {
                if (error != null) {
                    listener.onError(error);
                    return;
                }

                if (resultCode == RESULT_OK) {
                    if (requestCode == TYPE_PHOTO_UNTREATED) {
                        File photo = new File(mPhotoDir, mPhotoName);
                        if (!photo.exists()) {
                            listener.onCancel();
                        } else {
                            ShowPhotoInGallery();
                            listener.onSuccess(new ResultData(mContext, mUri, requestCode, resultCode, data, CODE_SUCCESS));
                        }
                    } else if (requestCode == TYPE_PHOTO_AUTO_COMPRESS) {
                        if (data == null) {
                            listener.onCancel();
                        } else {
                            listener.onSuccess(new ResultData(mContext, mUri, requestCode, resultCode, data, CODE_SUCCESS));
                        }
                    }
                } else {
                    listener.onCancel();
                }
            }
        });
    }

    /**
     * 在手机相册中显示刚拍摄的图片
     */
    private void ShowPhotoInGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mUri);
        mContext.sendBroadcast(mediaScanIntent);
    }
}
