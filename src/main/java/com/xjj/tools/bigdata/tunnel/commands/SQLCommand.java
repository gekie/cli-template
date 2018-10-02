package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2018/9/19.
 */
@CliCompent
public class SQLCommand extends BaseCommand{
    //@CliMethod(description = "执行SQL入口",show = false)
    public boolean selectSQL(String sql){
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
        //JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.QUERY_SQL_API,pm);
        String api = "http://202.100.241.122:9096/xxd/queryBySQL";
        JSONObject result = RESTfulAgent.getInstance().loadObject(api,pm);
        int errorCode = result.getInt("errorCode");
        if(errorCode==0){
            JSONArray items = result.getJSONArray("items");
            if(items.length()==0){
                println("没有数据.");
            }
            boolean printHead = false;
            List<JSONObject> heads = getFields(sql);
            if(heads==null) heads = new ArrayList<>();
            for(int i = 0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                Iterator<String> keys = item.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    String value = " "+item.get(key).toString()+" ";
                    value = value.replaceAll("\r","").replaceAll("\n","");
                    int fieldLen = getStringLen(key);
                    if(!printHead) {
                        JSONObject h = getHeader(key,heads);
                        if(h==null){
                            h = new JSONObject();
                            h.put("field",key);
                            heads.add(h);
                        }
                        int maxLen = getStringLen(value);
                        if(maxLen<fieldLen)
                            maxLen =fieldLen;
                        if(maxLen>100)
                            maxLen = 100;
                        h.put("maxLen",maxLen);

                    }else{
                        JSONObject h = getHeader(key,heads);
                        int _maxLen = getStringLen(value);
                        if(_maxLen<fieldLen)
                            _maxLen = fieldLen;
                        if(_maxLen>100)
                            _maxLen=100;
                        int maxLen = h.getInt("maxLen");
                        if(maxLen<_maxLen){
                            h.put("maxLen",_maxLen);
                        }

                    }
                }
                printHead = true;
            }
            for(JSONObject obj:heads){
                //print(obj.getString("field")+"("+obj.getInt("maxLen")+")\t│", Ansi.Color.YELLOW);
                int maxLen = obj.getInt("maxLen");
                if(maxLen<10)
                    maxLen = 10;
                String field = obj.getString("field");
                print(field, Ansi.Color.YELLOW);
                String blank = getBlankString(maxLen-getStringLen(field));
                print(blank+"│", Ansi.Color.YELLOW);
            }
            print("\r\n");
            for(JSONObject obj:heads){
                //print(obj.getString("field")+"("+obj.getInt("maxLen")+")\t│", Ansi.Color.YELLOW);
                int maxLen = obj.getInt("maxLen");
                if(maxLen<10)
                    maxLen = 10;
                String blank = getBlankString(maxLen,"─");
                print(blank+"│", Ansi.Color.YELLOW);
            }
            print("\r\n");
            //select "id","title" from "xjjoa2" limit 10;
            //select "id","title","comefrom","implService","starttime" from "xjjoa2" limit 12;
            for(int i=0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                for(JSONObject obj:heads){
                    int maxLen = obj.getInt("maxLen");
                    if(maxLen<10)
                        maxLen = 16;
                    String value = " "+item.get(obj.getString("field")).toString()+ " ";
                    value = value.replaceAll("\r","").replaceAll("\n","");

                    int _maxLen =value.length();// getStringLen(value);
                    if(_maxLen>50){
                        _maxLen = 50;
                        value = value.substring(0,47)+"...";
                    }
                    print(value);
                    int len = maxLen-_maxLen;
                    String blank = getBlankString(len);
                    print(blank+"┼", Ansi.Color.YELLOW);
                }
                print("\r\n");
            }
            for(JSONObject obj:heads){
                //print(obj.getString("field")+"("+obj.getInt("maxLen")+")\t│", Ansi.Color.YELLOW);
                int maxLen = obj.getInt("maxLen");
                if(maxLen<10)
                    maxLen = 10;
                String blank = getBlankString(maxLen,"─");
                print(blank+"┴", Ansi.Color.YELLOW);
            }
            print("\r\n");
            println("fetch "+items.length()+" rows.");
        }else{
            red(result.getString("message"));
        }
        return true;
    }
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
        //JSONObject result = RESTfulAgent.getInstance().loadObject(GlobalValue.QUERY_SQL_API,pm);
        String api = "http://202.100.241.122:9096/xxd/queryBySQL";
        JSONObject result = RESTfulAgent.getInstance().loadObject(api,pm);
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
                            table.appendColum(item.get(key));
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
        if(GlobalValue.tables.get(tableName)!=null){
            red(tableName+"已经存在");
            return false;
        }
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
