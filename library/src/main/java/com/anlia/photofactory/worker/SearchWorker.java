package com.anlia.photofactory.worker;

import com.anlia.photofactory.search.InterfaceManager;
import com.anlia.photofactory.search.model.SearchPhotoModelImpl;
import com.anlia.photofactory.search.presenter.SearchPhotoPresenterImpl;

import java.util.Map;

/**
 * Created by anlia on 2018/6/25.
 */

public class SearchWorker {
    SearchPhotoPresenterImpl presenter;
    Map<String,Object> map;
    InterfaceManager.LoadingCallBack loadingCallBack;
    InterfaceManager.SearchDataCallBack dataCallBack;
    InterfaceManager.ErrorCallBack errorCallBack;

    public SearchWorker(Map<String,Object> map){
        this.map = map;
    }

    /**
     * 设置查询条件（模糊匹配图片路径或名称）
     * @param selections
     * @return
     */
    public SearchWorker setSelection(String[] selections){
        map.put("isQueryByFormat",false);
        map.put("selections",selections);
        return this;
    }

    /**
     * 设置查询条件（匹配指定图片格式）
     * @param selections
     * @return
     */
    public SearchWorker setSelectionByFormat(String[] selections){
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
    public SearchWorker setLoadingEvent(InterfaceManager.LoadingCallBack callBack){
        loadingCallBack = callBack;
        return this;
    }

    /**
     * 设置报错信息的回调
     * @param callBack
     * @return
     */
    public SearchWorker setErrorEvent(InterfaceManager.ErrorCallBack callBack){
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
