package com.anlia.photofactory.search.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.anlia.photofactory.search.InterfaceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anlia on 2018/1/8.
 */

public class SearchPhotoModelImpl implements InterfaceManager.Model {
    /**
     * Loader的唯一ID号
     */
    private final static int IMAGE_LOADER_ID = 1000;

    @Override
    public void getData(Map<String, Object> map, final InterfaceManager.ModelDataCallBack callBack) {
        LoaderManager loaderManager = (LoaderManager) map.get("lm");
        final Context applicationContext = (Context) map.get("ac");
        final boolean isQueryByFormat = (boolean) map.get("isQueryByFormat");
        final String[] selections = (String[]) map.get("selections");
        final String [] projection = (String[]) map.get("projection");

        //初始化指定id的Loader
        loaderManager.initLoader(IMAGE_LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                //构造筛选语句
                String selection = "";
                for (int i = 0; i < selections.length; i++) {
                    if (i != 0) {
                        selection = selection + " OR ";
                    }

                    if(isQueryByFormat){
                        selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + selections[i] + "'";
                    }else {
                        selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + selections[i] + "%'";
                    }
                }
                Log.e("Tag",selection);
                //按图片修改时间递增顺序对结果进行排序;待会从后往前移动游标就可实现时间递减
//                String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED;//根据修改时间递增
                String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED;//根据添加时间递增

                CursorLoader imageCursorLoader = new CursorLoader(applicationContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, selection, null, sortOrder);
                return imageCursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data == null){
                    callBack.getDataFailed("查询失败！");
                    return;
                }

                List<Map<String,Object>> list = new ArrayList<>();
                Map<String,Object> dataMap;
                //游标从最后开始往前递减，以此实现时间递减顺序（最近访问的文件，优先显示）
                if (data.moveToLast()) {
                    do {
                        //输出文件的完整路径
                        dataMap = new HashMap<>();
                        for(int i=0;i<projection.length;i++){
                            dataMap.put(projection[i],data.getString(i));
                        }
//                        dataMap.put("path",data.getString(0));
                        list.add(dataMap);
                    } while (data.moveToPrevious());
                }
                callBack.getListDataSuccess(list);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }
}
