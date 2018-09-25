package com.anlia.photofactory.worker;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.anlia.photofactory.factory.PhotoFactory;
import com.anlia.photofactory.permission.PermissionAlwaysDenied;
import com.anlia.photofactory.utils.UriUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
    protected String[] requestPermissions;

    public BaseWorker(Context context, String photoDir, String photoName){
        mContext = context;
        mPhotoDir = photoDir;
        mPhotoName = photoName;

        mUri = UriUtils.GetFileUri(mContext,new File(mPhotoDir, mPhotoName));
        mMap = new HashMap<>();
    }

    protected void initPermissions() {
        requestPermissions = new String[]{Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE};
    }

    public void StartForResult(@NonNull final PhotoFactory.OnResultListener listener) {
        initPermissions();
        AndPermission.with(mContext)
                .runtime()
                .permission(requestPermissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        doInGranted(listener);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        if (AndPermission.hasAlwaysDeniedPermission(mContext, data)) {
                            if (PermissionAlwaysDenied.getInstance().getAction() != null) {
                                PermissionAlwaysDenied.getInstance().getAction().onAction(mContext, data, new PermissionAlwaysDenied.Executor() {
                                    @Override
                                    public void toSetting() {
                                        AndPermission.with(mContext).runtime().setting().start();
                                    }
                                });
                            } else {
                                Toast.makeText(mContext, "您已拒绝了授权此项操作，请在应用权限管理中手动授权后再进行尝试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .start();
    }

    protected abstract void doInGranted(PhotoFactory.OnResultListener listener);
}
