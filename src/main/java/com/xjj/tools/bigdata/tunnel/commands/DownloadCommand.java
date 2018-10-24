package com.xjj.tools.bigdata.tunnel.commands;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.xjj.tools.bigdata.tunnel.utils.*;
import org.jline.reader.LineReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

/**
 * Created by cjh on 18/9/20.
 */
@CliCompent
public class DownloadCommand extends BaseCommand {
    @AutoSetValue
    protected LineReader reader;
    @CliMethod(description = "从仓库中导出CSV格式数据，保存到本地文件",calcRequestTime = false)
    public boolean download(
            final String appid,
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
            localCsvPath = Config.getInstance().getBasePath()+"download"+File.separator;
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
            String localFile;
            @Override
            public void onReadyDownload() {
                reader.printAbove("正在准备下载，等到服务端响应请求");
            }

            @Override
            public String onFileContentLength(long size, String filename) {
                reader.printAbove("准备保存位置："+path+filename);
                reader.printAbove("数据大小："+Func.getFileSize(size));
                localFile =path+filename;
                return localFile;
            }

            @Override
            public void onDownloadProcess(long num, long totalSize) {
                printProgress(num,totalSize);
            }

            @Override
            public void onDownloadSuccess(String result) {
                print("\n");
                saveDownloadLog(appid,localFile,result,true);
                reader.printAbove(result);
            }

            @Override
            public void onDownloadFail(String errorMessage) {
                print("\n");
                red(errorMessage);
                saveDownloadLog(appid,localFile,errorMessage,false);
                reader.printAbove("");
            }
        });
        return true;
    }

    private void saveDownloadLog(String appid,String localFile,String message,boolean success){
        try {
            String logFile = Config.getInstance().getBasePath() + "download" + File.separator + "log";
            JSONArray json = Func.loadJSONFromFile(logFile);
            JSONObject app = GlobalValue.tables.get(appid);
            JSONObject item = new JSONObject();
            item.put("appid", appid);
            item.put("file", localFile);
            item.put("name", app.getString("base:name"));
            item.put("success", success);
            item.put("message", message);
            item.put("createtime", System.currentTimeMillis());
            json.put(item);
            Func.saveToFile(json.toString(), logFile);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @CliMethod(group = "show",description = "查看下载的数据",calcRequestTime = false,checkSession = false)
    public boolean downlog(){
        String logFile = Config.getInstance().getBasePath()+"download"+File.separator+"log";
        File file = new File(logFile);
        if(file.exists()){
            JSONArray items = new JSONArray(Func.readFile(logFile));
            ConsoleTable table = new ConsoleTable(6,false,-1);
            table.appendRow("AppId;仓库名称;下载时间;数据文件;是否成功;消息");
            items = sortJsonArray(items,"createtime",true);
            String basePath = Config.getInstance().getBasePath();
            for(int i=0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                String path = item.getString("file");
                path = path.replace(basePath,"./");
                table.appendRow(new Object[]{
                        item.get("appid"),
                        item.get("name"),
                        Func.format(item.getLong("createtime")),
                        path,
                        item.getBoolean("success")?"成功":"失败",
                        item.get("message")
                });
            }
            yellow(table.toString());
        }else{
            yellow("没有下载记录");
        }
        return true;
    }
    @CliMethod(group = "show",description = "查看本地CSV文件",calcRequestTime = false,checkSession = false)
    public boolean csv(String parent,String file,String split,int limit){
        if(Func.isEmpty(file)){
            return false;
        }
        if(file.startsWith("./"))
            file = Config.getInstance().getBasePath()+file;
        File cfile = new File(file);
        if(cfile.exists()) {
            char splitchar = ',';
            if(!Func.isEmpty(split))
                splitchar = split.charAt(0);
            if(limit==0)
                limit = -1;
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(cfile));
                CSVReader csvReader = new CSVReader(
                        new InputStreamReader(in, "UTF-8"),
                        splitchar,
                        CSVParser.DEFAULT_QUOTE_CHARACTER,
                        CSVParser.DEFAULT_ESCAPE_CHARACTER,
                        0);
                String[] items = null;
                ConsoleTable table = null;
                int headCount = 0;
                int count = 0;
                while((items=csvReader.readNext())!=null){
                    if(table==null) {
                        headCount= items.length;
                        table = new ConsoleTable(headCount, false, limit);
                    }
                    if(items.length<=headCount) {
                        table.appendRow(items);
                        count++;
                    }
                    if(limit>0&&count>=limit){
                        break;
                    }
                }
                yellow(table.toString());
                csvReader.close();
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            red(file+"文件不存在");
        }
        return true;
    }
    @CliMethod(group = "export",key="appid",description = "导出数据仓库配置信息")
    public boolean exportAppconfig(String _,String prefix,String csv){
        if(Func.isEmpty(prefix)){
            return false;
        }
        if(Func.isEmpty(csv))
            csv = Config.getInstance().getBasePath()+"download"+File.separator;
        File file = new File(csv);
        if(!file.exists()){
            file.mkdirs();
        }
        csv = csv+prefix+"_app_config.csv";
        String sql = "select * from \"app_config1\" where \"appid\" like '"+prefix+"%' order by \"name\"";
        PostParam pm = new PostParam();
        pm.addParam("sql",sql);
        pm.addParam("appid", GlobalValue.appid);
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.QUERY_SQL_API,pm);
        int errorCode = result.getInt("errorCode");
        if(errorCode==0){
            JSONArray items = result.getJSONArray("items");
            if(items.length()==0){
                println("没有数据.");
            }else{
                int count = items.length();
                StringBuffer buf = new StringBuffer();
                buf.append("ROW,name,appid,onlineprovider,provider\n");
                for(int i = 0;i<count;i++) {
                    JSONObject item = items.getJSONObject(i);
                    buf.append(item.getString("ROW")+",");
                    buf.append(item.getString("name")+",");
                    buf.append(item.getString("appid")+",");
                    buf.append(item.getString("onlineprovider")+",");
                    buf.append(item.getString("provider"));
                    buf.append("\n");
                }
                Func.saveToFile(buf.toString(),csv);
                yellow("一共导出"+count+"个AppId配置");
            }
        }else{
            red(result.getString("message"));
        }
        return true;
    }
}
