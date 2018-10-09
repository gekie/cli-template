package com.xjj.tools.bigdata.tunnel.utils;

public interface OnDownLoadListener {
    public void onReadyDownload();
    public String onFileContentLength(long size,String filename);
    public void onDownloadProcess(long num,long totalSize);
    public void onDownloadSuccess(String result);
    public void onDownloadFail(String errorMessage);
}
