package com.xjj.tools.bigdata.tunnel.utils;

/**
 * Created by cjh on 15/3/20.
 */
public interface OnDoPostMultiDataListener {
    void readyUpload();
    void transferred(long num);
    void uploadFail(int statusCode,String errorMessage);
    void uploadSuccess(String response);
}
