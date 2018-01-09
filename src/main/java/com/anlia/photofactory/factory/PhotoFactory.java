package com.anlia.photofactory.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;

import com.anlia.photofactory.mvp.InterfaceManager;
import com.anlia.photofactory.mvp.model.SearchPhotoModelImpl;
import com.anlia.photofactory.mvp.presenter.SearchPhotoPresenterImpl;
import com.anlia.photofactory.utils.CompressUtils;
import com.anlia.photofactory.utils.SystemUtils;
import com.anlia.photofactory.utils.UriUtils;

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
    private Intent intent;

    private String photoPath;
    private String photoName;

    public int REQUEST_CODE;
    public static final int TYPE_PHOTO_AUTO_COMPRESS = 100;
    public static final int TYPE_PHOTO_UNTREATED = 101;
    public static final int TYPE_PHOTO_FROM_GALLERY = 102;

    public PhotoFactory(Activity activity, Context context){
        mActivity = activity;
        mContext = context;
    }

    public StartBuilder FactoryStart(){
        REQUEST_CODE = -99;
        return new StartBuilder();
    }

    public class StartBuilder {
        public StartBuilder(){
        }

        public StartBuilder SetStartType(int type){
            REQUEST_CODE = type;
            return this;
        }

        public void Start(){
            if(REQUEST_CODE == -99){
                throw new NullPointerException("需要使用SetStartType设置获取相片的途径");
            }

            if(REQUEST_CODE == TYPE_PHOTO_AUTO_COMPRESS){
                TakePhotoAutoCompress();
            }else if(REQUEST_CODE == TYPE_PHOTO_UNTREATED){
                if(!SystemUtils.HasSdcard()){
                    throw new NullPointerException("SD卡读取失败");
                }else {
                    photoName = "original_"+System.currentTimeMillis()+ ".jpg";
                    photoPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ SystemUtils.getAppName(mContext)+"/";
                    File dir= new File(photoPath);
                    if (!dir.exists()) {
                        dir.mkdirs();// 创建照片的存储目录
                    }
                }
                TakePhotoUnTreated();
            }else if(REQUEST_CODE == TYPE_PHOTO_FROM_GALLERY){
                ChooseImageFromGallery();
            }
        }

        /**
         * 照相后返回系统自动压缩过的相片
         */
        private void TakePhotoAutoCompress(){
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mActivity.startActivityForResult(intent, REQUEST_CODE);
        }

        /**
         * 照相后返回高清原图相片
         */
        private void TakePhotoUnTreated(){
            mUri = UriUtils.GetFileUri(mActivity,new File(photoPath,photoName));
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            mActivity.startActivityForResult(intent,REQUEST_CODE);
        }

        /**
         * 从本地相册中选取图片
         */
        private void ChooseImageFromGallery() {
            intent = new Intent();
            intent.setType("image/*");// 设置文件类型
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            mActivity.startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public static final int CODE_HAS_DATA = 200;
    public static final int CODE_TAKE_PHOTO_CANCELED = 201;
    public static final int CODE_GALLERY_CANCELED = 202;

    public FinishBuilder FactoryFinish(int requestCode, int resultCode, Intent data){
        if(requestCode == TYPE_PHOTO_FROM_GALLERY){
            if(data == null){
                return new FinishBuilder(requestCode,resultCode,data, CODE_GALLERY_CANCELED);
            }else {
                mUri = data.getData();
                return new FinishBuilder(requestCode,resultCode,data, CODE_HAS_DATA);
            }
        }else if(requestCode == TYPE_PHOTO_UNTREATED){
            File photo = new File(photoPath,photoName);
            if(!photo.exists()){
                return new FinishBuilder(requestCode,resultCode,data, CODE_TAKE_PHOTO_CANCELED);
            }else {
                ShowPhotoInGallery();
                return new FinishBuilder(requestCode,resultCode,data, CODE_HAS_DATA);
            }
        }else if(requestCode == TYPE_PHOTO_AUTO_COMPRESS){
            if(data == null){
                return new FinishBuilder(requestCode,resultCode,data, CODE_TAKE_PHOTO_CANCELED);
            }else {
                return new FinishBuilder(requestCode,resultCode,data, CODE_HAS_DATA);
            }
        }else {
            return new FinishBuilder(requestCode,resultCode,data, CODE_TAKE_PHOTO_CANCELED);
        }
    }

    public class FinishBuilder {
        private int requestCode;
        private int resultCode;
        private Intent data;
        private Bitmap bitmap = null;
        private boolean isCompress = false;
        private int cancelCode;
        private OnResultListener mOnResultListener;

        public FinishBuilder(int requestCode, int resultCode, Intent data, int cancelCode){
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
            this.cancelCode = cancelCode;
        }

        public void setOnResultListener(OnResultListener mOnResultListener){
            this.mOnResultListener = mOnResultListener;
            if(cancelCode == CODE_HAS_DATA){
                this.mOnResultListener.HasData(this);
            }else if(cancelCode == CODE_TAKE_PHOTO_CANCELED){
                this.mOnResultListener.TakePhotoCancel();
            }else if(cancelCode == CODE_GALLERY_CANCELED){
                this.mOnResultListener.GalleryPhotoCancel();
            }
        }

        /**
         * 按目标宽高缩放
         * @param w
         * @param h
         * @return
         */
        public FinishBuilder addScaleCompress(int w, int h){
            if(isCompress){
                bitmap = CompressUtils.ScaleCompressFormBitmap(bitmap,w,h);
            }else {
                try {
                    isCompress = true;
                    bitmap = CompressUtils.ScaleCompressFormUri(mActivity,mUri,h,w);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return this;
        }

        /**
         * 等比例缩放
         * @param scale 压缩比
         * @return
         */
        public FinishBuilder addScaleCompress(int scale){
            if(isCompress){
                bitmap = CompressUtils.ScaleCompressFormBitmap(bitmap,scale);
            }else {
                try {
                    isCompress = true;
                    bitmap = CompressUtils.ScaleCompressFormUri(mActivity,mUri,scale);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return this;
        }

        /**
         * 质量压缩
         * @param targetSize 目标大小
         * @return
         */
        public FinishBuilder addQualityCompress(int targetSize){
            if(isCompress){
                bitmap = CompressUtils.QualityCompressFromBitmap(bitmap,targetSize);
            }else {
                try {
                    isCompress = true;
                    bitmap = CompressUtils.QualityCompressFromUri(mActivity,mUri,targetSize);
                }catch (Exception e){

                }
            }
            return this;
        }

        public Bitmap GetBitmap(){
            if(cancelCode != CODE_HAS_DATA){
                return null;
            }
            switch (requestCode){
                case TYPE_PHOTO_AUTO_COMPRESS:
                    try{
                        bitmap = data.getParcelableExtra("data");
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    break;
                case TYPE_PHOTO_UNTREATED:
                case TYPE_PHOTO_FROM_GALLERY:
                    if(!isCompress){
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), mUri);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    if(REQUEST_CODE == TYPE_PHOTO_UNTREATED){
                        if (mUri!=null){
                            mContext.getContentResolver().delete(mUri, null, null);
                        }
                    }
                    break;

            }
            return bitmap;
        }

        public Uri GetUri(){
            if(cancelCode != CODE_HAS_DATA){
                return null;
            }
            switch (requestCode){
                case TYPE_PHOTO_AUTO_COMPRESS:
                    try{
                        bitmap = data.getParcelableExtra("data");
                        mUri = Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bitmap, null,null));
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    break;
                case TYPE_PHOTO_UNTREATED:
                case TYPE_PHOTO_FROM_GALLERY:
                    if(isCompress){
                        mUri = Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bitmap, null,null));
                    }
                    break;
            }
            return mUri;
        }
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
        void TakePhotoCancel();
        void GalleryPhotoCancel();
        void HasData(FinishBuilder resultData);
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
