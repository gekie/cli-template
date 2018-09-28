package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.Func;
import com.xjj.tools.bigdata.tunnel.utils.GlobalValue;
import com.xjj.tools.bigdata.tunnel.utils.PostParam;
import com.xjj.tools.bigdata.tunnel.utils.RESTfulAgent;
import org.json.JSONArray;
import org.json.JSONObject;

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
        }
    }
}
