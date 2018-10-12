package com.xjj.tools.bigdata.tunnel.utils;

public interface OnDownLoadListener {
    void onReadyDownload();
    String onFileContentLength(long size,String filename);
    void onDownloadProcess(long num,long totalSize);
    void onDownloadSuccess(String result);
    void onDownloadFail(String errorMessage);
}
