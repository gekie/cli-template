package com.xjj.tools.bigdata.tunnel.utils;

/**
 * Created by cjh on 15/3/20.
 */
public interface OnLoadDataListener {
    void onDataReceiver(String dataContent);
    void onError(String errorMessage);
}
