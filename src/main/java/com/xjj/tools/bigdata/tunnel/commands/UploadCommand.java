package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.Func;
import com.xjj.tools.bigdata.tunnel.utils.GlobalValue;
import com.xjj.tools.bigdata.tunnel.utils.PostParam;
import com.xjj.tools.bigdata.tunnel.utils.RESTfulAgent;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by cjh on 18/9/20.
 */
@CliCompent
public class UploadCommand extends BaseCommand {
    @CliMethod(key = "uploadCsvFile",description = "上传CSV格式文件到仓库中",checkSession = true)
    public boolean uploadCsvFile(String csvFile,
                                 String tableName,
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
            MultipartEntity part = new MultipartEntity();
            part.addPart("appid", new StringBody(tableName));
            part.addPart("overwrite",new StringBody(rewrite));
            part.addPart("columnSplitChar",new StringBody(columnSplitChar));
            part.addPart("rowSplitChar",new StringBody(rowSplitChar));
            part.addPart("hdfs",new StringBody(hdfs));
            part.addPart("datafile",new FileBody(file,"application/octet-stream","UTF-8"));
            String content = RESTfulAgent.getInstance().doMultipartPost(GlobalValue.DATA_UPLOAD_API,part);
            yellow(content);
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
    @CliMethod(description = "上传TXT文件到仓库中")
    public boolean uploadTxtFile(String txtFile,String tableName,String columnSplitChar,String rowSplitChar){
        return true;
    }

    @CliMethod(description = "上传Excel表格到仓库中")
    public boolean uploadExcelFile(String excelFile,String tableName){
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
    @CliMethod(description = "上传JSON数据")
    public boolean upsertJson(String json,String appid){
        if(Func.isEmpty(GlobalValue.appid)&&Func.isEmpty(appid)){
            return false;
        }
        if(Func.isEmpty(json)){
            return false;
        }
        JSONObject table =GlobalValue.tables.get(appid);
        if(table==null){
            red(String.format("<%s>不存在",appid));
            return true;
        }else{
            try{
                if(json.startsWith("{")) {
                    JSONObject obj = new JSONObject(json);
                }else if(json.startsWith("[")) {
                    JSONArray list = new JSONArray(json);
                }
            }catch(Exception ex){
                red("json数据结构不规范");
                return true;
            }
            JSONObject result = RESTfulAgent.getInstance().loadObject("",json);
            return true;
        }
    }
}
