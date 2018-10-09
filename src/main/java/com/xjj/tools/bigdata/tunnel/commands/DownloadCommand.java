package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by cjh on 18/9/20.
 */
@CliCompent
public class DownloadCommand extends BaseCommand {
    @CliMethod(description = "从仓库中导出CSV格式数据，保存到本地文件")
    public boolean downloadCsvFile(
            String appid,
            String sql,
            String localCsvPath,
            String columnSplitChar){
        if(Func.isEmpty(appid)){
            red("请供仓库名称");
            return false;
        }
        if(Func.isEmpty(sql))
            sql = "";
        if(Func.isEmpty(columnSplitChar))
            columnSplitChar = ",";
        if(Func.isEmpty(localCsvPath))
            localCsvPath = "."+File.separator+"download"+File.separator;
        File file = new File(localCsvPath);
        if(!file.exists()){
            file.mkdirs();
        }
        final String path = localCsvPath;
        PostParam pm = new PostParam();
        pm.addParam("appid",appid);
        pm.addParam("sql",sql);
        pm.addParam("columnSplitChar",columnSplitChar);
        RESTfulAgent.getInstance().donwloadFile(GlobalValue.exportCSV_API, pm, new OnDownLoadListener() {
            @Override
            public void onReadyDownload() {
                yellow("正在准备下载");
            }

            @Override
            public String onFileContentLength(long size, String filename) {
                yellow("准备保存位置："+path+filename);
                return path+filename;
            }

            @Override
            public void onDownloadProcess(long num, long totalSize) {
                green(num+"/"+totalSize);
            }

            @Override
            public void onDownloadSuccess(String result) {
                yellow(result);
                try {
                    CommandUtils.getInstance().getConsoleReader().readLine();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onDownloadFail(String errorMessage) {
                red(errorMessage);
            }
        });
        return true;
    }

}
