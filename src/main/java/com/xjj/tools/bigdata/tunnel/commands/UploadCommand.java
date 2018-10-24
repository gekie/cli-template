package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.jline.reader.LineReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by cjh on 18/9/20.
 */
@CliCompent
public class UploadCommand extends BaseCommand {
    @AutoSetValue
    protected LineReader reader;
    @CliMethod(key = "updata",description = "上传CSV格式文件到仓库中",checkSession = true)
    public boolean uploadCsvFile(final String csvFile,
                                 final String tableName,
                                 String columnSplitChar,
                                 String rowSplitChar,
                                 String rewrite,
                                 String hdfs){
        if(Func.isEmpty(csvFile)){
            red("请提供待上传数据文件路径");
            return false;
        }
        File file = new File(csvFile);
        if(!file.exists()){
            red(csvFile+"不存在");
            return true;
        }
        if(Func.isEmpty(tableName)){
            red("请供仓库名称");
            return false;
        }
        if(Func.isEmpty(rewrite))
            rewrite = "true";
        if(Func.isEmpty(hdfs))
            hdfs = "false";
        if(Func.isEmpty(columnSplitChar))
            columnSplitChar = ",";
        if(Func.isEmpty(rowSplitChar))
            rowSplitChar = "\n";

        try {
            CustomMultipartEntity part = new CustomMultipartEntity();
            part.addPart("appid", new StringBody(tableName));
            part.addPart("overwrite",new StringBody(rewrite));
            part.addPart("columnSplitChar",new StringBody(columnSplitChar));
            part.addPart("rowSplitChar",new StringBody(rowSplitChar));
            part.addPart("hdfs",new StringBody(hdfs));
            part.addPart("datafile",new FileBody(file,"application/octet-stream","UTF-8"));
            //String content = RESTfulAgent.getInstance().doMultipartPost(GlobalValue.DATA_UPLOAD_API,part);
            //yellow(content);
            final long fsize = file.length();
            yellow("正准备上传数据，数据大小："+Func.getFileSize(fsize));
            final String _hdfs = hdfs;
            final String _rewrite = rewrite;
            final String _columnSplitChar = columnSplitChar;
            RESTfulAgent.getInstance().doMultipartPost(GlobalValue.DATA_UPLOAD_API,
                    part,
                    new OnDoPostMultiDataListener() {
                        public void readyUpload(){
                            resetPrint();
                            yellow("正在发送数据");
                        }
                        @Override
                        public void transferred(long num) {
                            printProgress(num,fsize);
                        }

                        @Override
                        public void uploadFail(int statusCode, String errorMessage) {
                            resetPrint();
                            red("数据上传失败，错误代码："+statusCode+"，原因："+errorMessage);
                            reader.printAbove("");
                            saveUploadLog(tableName,
                                    csvFile,
                                    _hdfs,_rewrite,_columnSplitChar,
                                    "错误代码："+statusCode+"，原因："+errorMessage,
                                    false);
                        }

                        @Override
                        public void uploadSuccess(String response) {
                            resetPrint();
                            try {
                                JSONObject result = new JSONObject(response);
                                if(result.getInt("errorCode")==0){
                                    yellow(result.getString("message"));
                                    saveUploadLog(tableName,
                                            csvFile,
                                            _hdfs,_rewrite,_columnSplitChar,
                                            result.getString("message"),
                                            true);
                                }else{
                                    red(result.getString("message"));
                                    saveUploadLog(tableName,
                                            csvFile,
                                            _hdfs,_rewrite,_columnSplitChar,
                                            result.getString("message"),
                                            false);
                                }
                            }catch(Exception ex){
                                red(ex.getMessage());
                            }
                            reader.printAbove("");
                        }
                    }
            );
        }catch(Exception ex){
            red(ex.getMessage());
        }
        return true;
    }
    @CliMethod(group = "show",description = "查看数据上传日志",calcRequestTime = false)
    public boolean upload(String _,String appid){
        if(Func.isEmpty(appid)){
            return  false;
        }
        PostParam pm = new PostParam();
        pm.addParam("appid",appid);
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.SHOW_UPLOAD_LOG,pm);
        Object obj = result.get("message");
        if(obj instanceof JSONObject)
            yellow(result.getString("message"));
        else if(obj instanceof JSONArray){
            JSONArray arr = (JSONArray)obj;
            for(int i = 0;i<arr.length();i++){
                yellow(arr.getString(i).replaceAll("\n",""));
            }
        }
        return true;
    }

    @CliMethod(description = "创建新仓库")
    public boolean createRepository(String appid,String name){
        if(Func.isEmpty(appid)){
            red("createRepository参数不完整，参考：createRepository appid:<appid> name:<name>");
            return true;
        }
        if(Func.isEmpty(name)){
            red("createRepository参数不完整，参考：createRepository appid:<appid> name:<name>");
            return true;
        }
        String _appid = appid;
        String _name = name;
        String paramNames = "appid;name;";
        if(appid.indexOf(":")!=-1){
            String[] appids = appid.split(":");
            if(paramNames.indexOf(appids[0])!=-1){
                if(appids[0].equals("appid")){
                    _appid = appids[1];
                }else if(appids[0].equals("name")){
                    _name = appids[1];
                }
            }else{
                red(appids[0]+"不是有效的参数");
                return true;
            }
        }
        if(name.indexOf(":")!=-1){
            String[] appids = name.split(":");
            if(paramNames.indexOf(appids[0])!=-1){
                if(appids[0].equals("appid")){
                    _appid = appids[1];
                }else if(appids[0].equals("name")){
                    _name = appids[1];
                }
            }else{
                red(appids[0]+"不是有效的参数");
                return true;
            }
        }
        if(GlobalValue.tables.get(_appid)!=null){
            red(appid+"已经存在");
            return true;
        }
        String online = "none";
        PostParam pm = new PostParam();
        JSONObject vo = new JSONObject();
        vo.put("appid",_appid);
        vo.put("rowKey","");
        vo.put("name",_name);
        vo.put("provider",GlobalValue.userName);
        vo.put("onlineprovider",online);
        vo.put("newapp","true");
        pm.addParam("model",vo.toString());
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.Create_Repository_API,pm);
        if(result.getInt("errorCode")==0){
            JSONObject item = result.getJSONObject("item");
            yellow("成功创建数据仓库："+_name+",AppID："+_appid+",SecrectKey："+result.getString("rowKey"));
            GlobalValue.appid = _appid;
            GlobalValue.tables.put(_appid,item);
            GlobalValue.dbSkey = result.getString("rowKey");
        }else{
            red("创建数据仓库<"+_appid+">失败："+result.getString("message"));
        }
        return true;
    }
    @CliMethod(description = "删除仓库")
    public boolean dropRepository(String appid){
        if(Func.isEmpty(appid)){
            return false;
        }
        JSONObject table = GlobalValue.tables.get(appid);
        if(table==null){
            red("<"+appid+">不存在");
            return true;
        }
        PostParam pm = new PostParam();
        pm.addParam("rowkey",table.getString("rowKey"));
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.Drop_Repository_API,pm);
        if(result.getInt("errorCode")==0){
            yellow(String.format("<%s>已成功删除",appid));
            GlobalValue.tables.remove(appid);
        }else{
            red(String.format("<%s>删除失败", appid));
        }
        return true;
    }
    private void saveUploadLog(String appid,String localFile,String hdfs,String rewrite,String columnSplitChar,String message,boolean success){
        try {
            String logFile = Config.getInstance().getBasePath() + "upload" + File.separator;
            File file = new File(logFile);
            if(!file.exists())
                file.mkdirs();
            logFile+="log";
            JSONArray json =Func.loadJSONFromFile(logFile);
            JSONObject app = GlobalValue.tables.get(appid);
            JSONObject item = new JSONObject();
            item.put("appid", appid);
            item.put("file", localFile);
            item.put("name", app.getString("base:name"));
            item.put("hdfs", hdfs);
            item.put("rewrite", rewrite);
            item.put("columnSplitChar", columnSplitChar);
            item.put("success", success);
            item.put("message", message);
            item.put("createtime", System.currentTimeMillis());
            json.put(item);
            Func.saveToFile(json.toString(), logFile);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @CliMethod(group = "show",key="uplog", description = "查看下载的数据",calcRequestTime = false,checkSession = false)
    public boolean upHistory(){
        String logFile = Config.getInstance().getBasePath()+"upload"+File.separator+"log";
        File file = new File(logFile);
        if(file.exists()){
            JSONArray items = new JSONArray(Func.readFile(logFile));
            ConsoleTable table = new ConsoleTable(9,false,-1);
            table.appendRow("AppId;仓库名称;上传时间;数据文件;覆盖;列分隔符;存入Hadoop;是否成功;消息");
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
                        item.get("rewrite"),
                        item.get("columnSplitChar"),
                        item.get("hdfs"),
                        item.getBoolean("success")?"成功":"失败",
                        item.get("message")
                });
            }
            yellow(table.toString());
        }else{
            yellow("没有上传记录");
        }
        return true;
    }

    @CliMethod(group="imp",key="app",description = "导入APP Config")
    public boolean importAppConfig(String _,String csv){
        if(Func.isEmpty(csv)){
            return false;
        }
        File file = new File(csv);
        if(!file.exists()){
            red(csv+"文件不存在");
        }else{
            String content = Func.readFile(csv);
            String[] lines = content.split("\n");
            for(int i =1;i<lines.length;i++){
                if(lines[i].length()>0) {
                    String[] ds = lines[i].split(",");
                    String row = ds[0];
                    String name = ds[1];
                    String appid = ds[2];
                    String onlineprovider = ds[3];
                    String provider = ds[4];
                    importNewAppid(row,name,appid,onlineprovider,provider);
                }
            }
        }
        return true;
    }

    private void importNewAppid(String row,String name,String appid,String onlineprovider,String provider){
        PostParam pm = new PostParam();
        JSONObject vo = new JSONObject();
        vo.put("appid",appid);
        vo.put("rowKey",row);
        vo.put("name",name);
        vo.put("provider",provider);
        vo.put("onlineprovider",onlineprovider);
        vo.put("newapp","true");
        pm.addParam("model",vo.toString());
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.Create_Repository_API,pm);
        if(result.getInt("errorCode")==0){
            yellow("成功创建数据仓库："+name+",AppID："+appid+",SecrectKey："+result.getString("rowKey"));
        }else{
            red("创建数据仓库<"+appid+">失败："+result.getString("message"));
        }
    }
}
