package com.anlia.photofactory.permission;

import android.app.Activity;
import android.content.Context;

import java.util.List;

/**
 * Created by anlia on 2018/9/21.
 */

public class PermissionAlwaysDenied {
    public volatile static PermissionAlwaysDenied sPermissionAlwaysDenied = null;
    private Action mAction;


    public static PermissionAlwaysDenied getInstance() {
        if (sPermissionAlwaysDenied == null) {
            synchronized (PermissionAlwaysDenied.class) {
                sPermissionAlwaysDenied = new PermissionAlwaysDenied();
            }
        }
        return sPermissionAlwaysDenied;
    }

    public void setAction(Action action) {
        this.mAction = action;
    }

    public Action getAction() {
        return this.mAction;
    }

    public interface Action {
        void onAction(Context context, List<String> permissions, Executor executor);
    }

    public interface Executor {
        void toSetting();
    }
}
