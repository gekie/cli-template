package com.xjj.tools.bigdata.tunnel.commands;

/**
 * Created by cjh on 18/9/20.
 */
@CliCompent
public class DownloadCommand extends BaseCommand {
    @CliMethod(description = "从仓库中导出CSV格式数据，保存到本地文件")
    public boolean downloadCsvFile(String tableName,String localCsvPath,String columnSplitChar,String rowSplitChar){
        return true;
    }
    @CliMethod(description = "从仓库中导出Excel格式数据，保存到本地文件")
    public boolean downloadExcelFile(String tableName,String localExcelPath){
        return true;
    }
    @CliMethod(description = "从仓库中导出CSV格式数据，保存到本地文件")
    public boolean downloadTxtFile(String tableName,String localTxtPath,String columnSplitChar,String rowSplitChar){
        return true;
    }
}
