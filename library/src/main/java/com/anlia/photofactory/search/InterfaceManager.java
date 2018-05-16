package com.anlia.photofactory.search;

import java.util.List;
import java.util.Map;

/**
 * Created by anlia on 2018/1/9.
 */

public interface InterfaceManager {
    /**
     * MVP模式接口
     */
    interface Model {
        void getData(Map<String, Object> map, ModelDataCallBack callBack);
    }
    interface View {
        void onFinish(List<Map<String, Object>> list);
    }
    interface Presenter{
        void getData(Map<String, Object> map);
    }

    /**
     * model数据回调
     */
    interface ModelDataCallBack {
        void getListDataSuccess(List<Map<String, Object>> list);
        void getDataFailed(String message);
    }

    /**
     * 搜索数据回调
     */
    interface SearchDataCallBack extends View{
        @Override
        void onFinish(List<Map<String, Object>> list);
    }

    /**
     * 加载中回调
     */
    interface LoadingCallBack{
        void showLoading();
        void hideLoading();
    }

    /**
     * 错误回调
     */
    interface ErrorCallBack{
        void dealError(String message);
    }
}
