package com.xjj.tools.bigdata.tunnel.utils;

/**
 * Created by cjh on 15/3/20.
 */
public interface OnLoadDataListener {
    public void onDataReceiver(String dataContent);
    public void onError(String errorMessage);
}
