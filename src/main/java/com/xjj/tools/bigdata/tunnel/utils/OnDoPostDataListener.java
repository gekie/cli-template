package com.xjj.tools.bigdata.tunnel.utils;

/**
 * Created by cjh on 15/3/20.
 */
public interface OnDoPostDataListener {
    public void onDataReceiver(String result) ;
    public void onError(String errorMessage);
    public void onShowMessage(String message, int value);
}
