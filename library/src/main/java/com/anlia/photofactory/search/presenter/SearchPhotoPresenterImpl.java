package com.anlia.photofactory.search.presenter;

import android.os.Handler;

import com.anlia.photofactory.search.InterfaceManager;

import java.util.List;
import java.util.Map;

/**
 * Created by anlia on 2017/7/23.
 */

public class SearchPhotoPresenterImpl implements InterfaceManager.Presenter{
    InterfaceManager.Model model;
    InterfaceManager.View view;
    InterfaceManager.LoadingCallBack loadingCallBack;
    InterfaceManager.ErrorCallBack errorCallBack;

    private Handler mHandler = new Handler();

    public SearchPhotoPresenterImpl(InterfaceManager.Model model, InterfaceManager.View view){
        this.view = view;
        this.model = model;
    }

    public SearchPhotoPresenterImpl(InterfaceManager.Model model, InterfaceManager.View view, InterfaceManager.LoadingCallBack loadingCallBack){
        this.view = view;
        this.model = model;
        this.loadingCallBack = loadingCallBack;
    }

    public SearchPhotoPresenterImpl(InterfaceManager.Model model, InterfaceManager.View view, InterfaceManager.ErrorCallBack errorCallBack){
        this.view = view;
        this.model = model;
        this.errorCallBack = errorCallBack;
    }

    public SearchPhotoPresenterImpl(InterfaceManager.Model model, InterfaceManager.View view,
                                    InterfaceManager.LoadingCallBack loadingCallBack, InterfaceManager.ErrorCallBack errorCallBack){
        this.view = view;
        this.model = model;
        this.loadingCallBack = loadingCallBack;
        this.errorCallBack = errorCallBack;
    }

    @Override
    public void getData(Map<String, Object> map) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(loadingCallBack !=null){
                    loadingCallBack.showLoading();
                }
            }
        });
        model.getData(map, new InterfaceManager.ModelDataCallBack() {
            @Override
            public void getListDataSuccess(final List<Map<String, Object>> list) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.onFinish(list);
                        if(loadingCallBack !=null){
                            loadingCallBack.hideLoading();
                        }
                    }
                });
            }

            @Override
            public void getDataFailed(final String message) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(errorCallBack !=null){
                            errorCallBack.dealError(message);
                        }

                        if(loadingCallBack !=null){
                            loadingCallBack.hideLoading();
                        }
                    }
                });
            }
        });
    }
}
