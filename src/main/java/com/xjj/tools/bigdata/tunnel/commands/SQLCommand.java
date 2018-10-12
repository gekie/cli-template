package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin on 2018/9/19.
 */
@CliCompent
public class SQLCommand extends BaseCommand{
    @CliMethod(description = "执行SQL入口",show=false,checkSession = false)
    public boolean querySQL(String sql){
        if(Func.isEmpty(sql)){
            return false;
        }
        /*
        if(Func.isEmpty(GlobalValue.appid)){
            red("您还没进入项目空间，请使用 use <appid>进入项目空间");
            return false;
        }*/
        sql = sql.replaceAll(";","");
        PostParam pm = new PostParam();
        pm.addParam("sql",sql);
        pm.addParam("appid", GlobalValue.appid);
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.QUERY_SQL_API,pm);
        int errorCode = result.getInt("errorCode");
        if(errorCode==0){
            JSONArray items = result.getJSONArray("items");
            if(items.length()==0){
                println("没有数据.");
            }
            boolean printHead = false;
            List<JSONObject> heads = getFields(sql);
            if(items.length()>0){
                ConsoleTable table = getConsoleTable(heads,items.getJSONObject(0));
                int count = items.length();
                for(int i = 0;i<items.length();i++){
                    JSONObject item = items.getJSONObject(i);
                    table.appendRow();
                    if(heads==null) {
                        Iterator<String> keys = item.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            table.appendColum(item.get(key));
                        }
                    }else{
                        for(JSONObject obj:heads){
                            String key = obj.getString("field").toUpperCase();
                            table.appendColum(get(key,item));
                        }
                    }
                }
                yellow(table.toString());
            }else{
                red("没有记录");
            }

        }else{
            red(result.getString("message"));
        }
        return true;
    }
    private Object get(String key,JSONObject obj){
        if(obj.has(key)){
            return obj.get(key);
        }else if(obj.has(key.toUpperCase())){
            return obj.get(key.toUpperCase());
        }else if(obj.has(key.toLowerCase())){
            return obj.get(key.toLowerCase());
        }else{
            return null;
        }
    }
    private ConsoleTable getConsoleTable(List<JSONObject> heads,JSONObject item){
        ConsoleTable table = null;
        if(heads!=null){
            table = new ConsoleTable(heads.size(),false,10);
            table.appendRow();
            for(JSONObject obj:heads){
                String field = obj.getString("field");
                table.appendColum(field);
            }
        }else{
            table = new ConsoleTable(item.keySet().size(),false,10);
            table.appendRow();
            Iterator<String> keys = item.keys();
            while(keys.hasNext()){
                table.appendColum(keys.next());
            }
        }
        return table;
    }
    @CliMethod(description = "执行SQL入口",show = false)
    public boolean createSQL(String sql){
        String _sql= sql.toLowerCase();
        if(!_sql.startsWith("create table")||_sql.indexOf("(")==-1||_sql.indexOf(")")==-1){
            red("建表SQL语法不规范");
            return false;
        }
        String tableName = getTableName(sql);
        /*if(GlobalValue.tables.get(tableName)!=null){
            red(tableName+"已经存在");
            return false;
        }*/
        //create table a_test2(id varchar primary key,a varchar,b varchar);
        sql = sql.replaceAll(";","");
        println(sql);
        PostParam pm = new PostParam();
        pm.addParam("sql",sql);
        pm.addParam("appid", GlobalValue.appid);
        JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.EXECUTE_SQL_API,pm);
        println(result);
        return true;
    }
    private String getBlankString(int len){
        return getBlankString(len," ");
    }
    private String getBlankString(int len,String s){
        String str = "";
        for(int j=0;j<len;j++){
            str+=s;
        }
        return str;
    }
    private JSONObject getHeader(String field,List<JSONObject> list){
        for(JSONObject obj:list){
            if(obj.getString("field").equals(field)){
                return obj;
            }
        }
        return null;
    }
    private String getGB2312(String str){
        try {
            String a = new String(str.getBytes("gb2312"), "iso-8859-1");
            return a;
        }catch (Exception ex){
            return str;
        }
    }
    private int getStringLen(String str){
        try {
            String a = new String(str.getBytes("GBK"), "iso-8859-1");
            return a.length();
        }catch (Exception ex){
            return str.length();
        }
    }
    private List<JSONObject> getFields(String sql){
        String _sql = sql.toUpperCase();
        int fromIndex = _sql.indexOf(" FROM");
        if(fromIndex!=-1){
            sql = sql.substring(7,fromIndex);
            //sql = sql.replaceAll(" ","");
            if(sql.indexOf("*")!=-1){
                return null;
            }
            String[] fields = sql.split(",");
            ArrayList<JSONObject> heads = new ArrayList<>();
            for(int i = 0;i<fields.length;i++){
                String f = fields[i];
                String bf=f.toUpperCase();
                int bfi = bf.indexOf(" AS");
                if(bf.indexOf(" AS")!=-1){
                    f =bf.substring(bfi+3);
                }
                f = f.replaceAll("\"","").replace(" ","");
                JSONObject h = new JSONObject();
                h.put("field",f);
                h.put("maxLen",f.length());
                heads.add(h);
            }
            return heads;
        }else{
            return null;
        }
    }
    private String getTableName(String sql){
        String _sql = sql.toLowerCase();
        String tableName = sql.substring(_sql.indexOf("table")+6,sql.indexOf("("));
        return tableName.replaceAll(" ","");
    }

    @CliMethod(group = "show",key="table",description = "列出所有SQL表",checkSession = false)
    public boolean showtable(){
        println("show table");
        return true;
    }

    @CliMethod(description = "查看键组")
    public boolean describe(String appid){
        String _appid = appid.replaceAll("\"","");
        if(GlobalValue.tables.get(_appid)==null){
            red(appid+"不存在");
            return true;
        }else {
            String sql = "select * from "+appid+" limit 1";
            PostParam pm = new PostParam();
            pm.addParam("sql", sql);
            pm.addParam("appid", appid);
            JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.QUERY_SQL_API, pm);
            if(result.getInt("errorCode")==0){
                JSONArray items = result.getJSONArray("items");
                if(items.length()>0) {
                    ConsoleTable table = new ConsoleTable(2, false, -1);
                    table.appendRow("FIELD_NAME;TYPE_NAME");
                    JSONObject item = items.getJSONObject(0);
                    Iterator<String> keys = item.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        table.appendRow(new Object[]{key,"varchar"});
                    }
                    yellow(table.toString());
                }else{
                    red("没有记录");
                }
            }else{
                red(result.getString("message"));
            }
            return true;
        }
    }
    public static void main(String[] args){
        String sql = "select count(\"ROW\") as total from \"xjj_aiml_pattern\";";
        String _sql = sql.toUpperCase();
        int fromIndex = _sql.indexOf(" FROM");
        if(fromIndex!=-1){
            sql = sql.substring(7,fromIndex);
            //sql = sql.replaceAll(" ","");
            System.out.println(sql);
            if(sql.indexOf("*")==-1) {
                System.out.println(sql);
                String[] fields = sql.split(",");
                for (int i = 0; i < fields.length; i++) {
                    String f = fields[i];
                    String bf=f.toLowerCase();
                    int bfi = bf.indexOf(" as");
                    if(bf.indexOf(" as")!=-1){
                        f =f.substring(bfi+3);
                    }
                    f = f.replaceAll("\"", "").replace(" ", "");
                    System.out.println(f);
                }
            }
        }
    }

}
