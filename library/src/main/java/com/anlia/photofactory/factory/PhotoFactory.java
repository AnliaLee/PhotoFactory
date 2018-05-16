package com.anlia.photofactory.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.LoaderManager;

import com.anlia.photofactory.search.InterfaceManager;
import com.anlia.photofactory.search.model.SearchPhotoModelImpl;
import com.anlia.photofactory.search.presenter.SearchPhotoPresenterImpl;
import com.anlia.photofactory.result.ResultData;
import com.anlia.photofactory.utils.SystemUtils;
import com.anlia.photofactory.utils.UriUtils;
import com.anlia.photofactory.worker.CameraWorker;
import com.anlia.photofactory.worker.CropWorker;
import com.anlia.photofactory.worker.GalleryWorker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anlia on 2017/12/18.
 */

public class PhotoFactory {
    private Activity mActivity;
    private Context mContext;
    private Uri mUri;

    private String mPhotoPath;
    private String mPhotoName;

    public static final int TYPE_PHOTO_AUTO_COMPRESS = 100;
    public static final int TYPE_PHOTO_UNTREATED = 101;
    public static final int TYPE_PHOTO_FROM_GALLERY = 102;
    public static final int TYPE_PHOTO_CROP = 103;
    public static final int TYPE_ERROR = -99;

    public PhotoFactory(Activity activity, Context context){
        this(activity,context,
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ "DCIM" +"/",
                "original_"+System.currentTimeMillis()+ ".png");
    }

    public PhotoFactory(Activity activity, Context context, String photoPath, String photoName){
        mActivity = activity;
        mContext = context;
        mPhotoPath = photoPath;
        mPhotoName = photoName;

        if(!SystemUtils.HasSdcard()){
            throw new NullPointerException("SD卡读取失败");
        }else {
            File dir= new File(mPhotoPath);
            if (!dir.exists()) {
                dir.mkdirs();// 创建照片的存储目录
            }
        }
    }

    public StartBuilder FactoryStart(){
        return new StartBuilder();
    }

    public class StartBuilder {
        public StartBuilder(){
            mUri = UriUtils.GetFileUri(mActivity,new File(mPhotoPath, mPhotoName));
        }

        public CameraWorker FromCamera(){
            return new CameraWorker(mActivity,mUri);
        }

        public GalleryWorker FromGallery(){
            return new GalleryWorker(mActivity,mUri);
        }

        public CropWorker FromCrop(){
            return new CropWorker(mActivity,mUri,mContext, mPhotoPath, mPhotoName);
        }
    }

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_CANCELED = 201;

    public ResultData FactoryFinish(int requestCode, int resultCode, Intent data){
        int dataCode;
        if(requestCode == TYPE_PHOTO_FROM_GALLERY){
            if(data == null){
                dataCode = CODE_CANCELED;
            }else {
                mUri = data.getData();
                dataCode = CODE_SUCCESS;
            }
        }else if(requestCode == TYPE_PHOTO_UNTREATED){
            File photo = new File(mPhotoPath, mPhotoName);
            if(!photo.exists()){
                dataCode = CODE_CANCELED;
            }else {
                ShowPhotoInGallery();
                dataCode = CODE_SUCCESS;
            }
        }else if(requestCode == TYPE_PHOTO_AUTO_COMPRESS){
            if(data == null){
                dataCode = CODE_CANCELED;
            }else {
                dataCode = CODE_SUCCESS;
            }
        }else if(requestCode == TYPE_PHOTO_CROP){
            if(data == null){
                dataCode = CODE_CANCELED;
            }else {
                dataCode = CODE_SUCCESS;
            }
        }else{
            dataCode = CODE_CANCELED;
        }
        return new ResultData(mActivity,mUri,mContext,requestCode,resultCode,data,dataCode);
    }

    /**
     * 在手机相册中显示刚拍摄的图片
     */
    private void ShowPhotoInGallery(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }

    public interface OnResultListener {
        void OnCancel();
        void OnSuccess(ResultData resultData);
    }


    /**
     * 初始化图片搜索Builder
     * @param loaderManager
     * @param applicationContext
     * @param projection 加载数据的映射（MediaStore.Images.Media.DATA等）
     * @return
     */
    public SearchBuilder FactorySearch(LoaderManager loaderManager,Context applicationContext,String[] projection){
        Map<String,Object> map = new HashMap<>();
        map.put("lm",loaderManager);
        map.put("ac",applicationContext);
        map.put("isQueryByFormat",false);
        map.put("selections",new String[]{""});
        map.put("projection",projection);
        return new SearchBuilder(map);
    }

    public class SearchBuilder{
        SearchPhotoPresenterImpl presenter;
        Map<String,Object> map;
        InterfaceManager.LoadingCallBack loadingCallBack;
        InterfaceManager.SearchDataCallBack dataCallBack;
        InterfaceManager.ErrorCallBack errorCallBack;

        public SearchBuilder(Map<String,Object> map){
            this.map = map;
        }

        /**
         * 设置查询条件（模糊匹配图片路径或名称）
         * @param selections
         * @return
         */
        public SearchBuilder setSelection(String[] selections){
            map.put("isQueryByFormat",false);
            map.put("selections",selections);
            return this;
        }

        /**
         * 设置查询条件（匹配指定图片格式）
         * @param selections
         * @return
         */
        public SearchBuilder setSelectionByFormat(String[] selections){
            map.put("isQueryByFormat",true);
            map.put("selections",selections);
            return this;
        }

        /**
         * 设置数据加载中的回调
         * ps:由于图片查询速度太快了，几乎看不到loading，聊胜于无吧233
         * @param callBack
         * @return
         */
        public SearchBuilder setLoadingEvent(InterfaceManager.LoadingCallBack callBack){
            loadingCallBack = callBack;
            return this;
        }

        /**
         * 设置报错信息的回调
         * @param callBack
         * @return
         */
        public SearchBuilder setErrorEvent(InterfaceManager.ErrorCallBack callBack){
            errorCallBack = callBack;
            return this;
        }

        /**
         * 执行搜索
         * @param callBack
         */
        public void execute(InterfaceManager.SearchDataCallBack callBack){
            dataCallBack = callBack;

            if(loadingCallBack ==null && errorCallBack == null){
                presenter = new SearchPhotoPresenterImpl(new SearchPhotoModelImpl(),callBack);
            }else if(loadingCallBack ==null){
                presenter = new SearchPhotoPresenterImpl(new SearchPhotoModelImpl(),callBack,errorCallBack);
            }else if(errorCallBack == null){
                presenter = new SearchPhotoPresenterImpl(new SearchPhotoModelImpl(),callBack,loadingCallBack);
            }else {
                presenter = new SearchPhotoPresenterImpl(new SearchPhotoModelImpl(),callBack,loadingCallBack,errorCallBack);
            }
            presenter.getData(map);
        }
    }
}
