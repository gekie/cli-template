package com.xjj.tools.bigdata.tunnel.commands;

/**
 * Created by cjh on 18/9/20.
 */
@CliCompent
public class UploadCommand extends BaseCommand {
    @CliMethod(key = "uploadCsvFile",description = "上传CSV格式文件到仓库中",checkSession = true)
    public boolean uploadCsvFile(String csvFile,String tableName,String columnSplitChar,String rowSplitChar){
        return true;
    }

    @CliMethod(description = "上传TXT文件到仓库中")
    public boolean uploadTxtFile(String txtFile,String tableName,String columnSplitChar,String rowSplitChar){
        return true;
    }

    @CliMethod(description = "上传Excel表格到仓库中")
    public boolean uploadExcelFile(String excelFile,String tableName){
        return true;
    }

}
