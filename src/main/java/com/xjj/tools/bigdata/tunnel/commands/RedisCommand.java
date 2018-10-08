package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@CliCompent
public class RedisCommand extends BaseCommand{
    @AutoSetValue
    private String inputLine;
    @CliMethod(description = "Redis Key Value",checkSession = false)
    public boolean redis(String columns,String key,
                         String valueKey,
                         String order,
                         boolean desc,
                         int limit){
        boolean showAllColumn = true;
        if(limit==0){
            limit = 20;
        }
        List<String> fields = new ArrayList<>();
        if(!Func.isEmpty(columns)&&columns.indexOf(",")!=-1){
            String[] fds = columns.split(",");
            showAllColumn = false;
            for(String fd :fds){
                fields.add(fd);
            }
        }
        if(Func.isEmpty(key)){
            //return false;
            key = columns;
        }
        if(Func.isEmpty(key)||key.indexOf(",")!=-1){
            return false;
        }
        if(Func.isEmpty(valueKey))
            valueKey = "items";
        //all-ALL-COUNT
        String action = "http://202.100.241.122:9096/ykt/loadContentByKey?key="+key;
        PostParam pm = new PostParam();
        pm.addParam("key",key);
        JSONObject result = RESTfulAgent.getInstance().getObject(action);
        if(result.getInt("errorCode")==0){
            Object obj = result.get(valueKey);
            if(obj instanceof JSONArray) {
                JSONArray items = (JSONArray)obj;//result.getJSONArray(valueKey);
                if (items.length() > 0) {
                    ConsoleTable table = getHead(items.getJSONObject(0),showAllColumn,fields);
                    table.setMaxRowCount(limit);
                    //List<String> hds = getHeader(items.getJSONObject(0));
                    //List<List<String>> list = new ArrayList<>();
                    if(!Func.isEmpty(order)){
                        items = sortJsonArray(items,order,desc);
                    }
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        table.appendRow();
                        if(showAllColumn) {
                            Iterator<String> keys = item.keys();
                            //List<String> rows = new ArrayList<>();
                            while (keys.hasNext()) {
                                table.appendColum(item.get(keys.next()));
                                //rows.add(item.get(keys.next()).toString());
                            }
                        }else{
                            for(String fd :fields){
                                Object v = getObject(item,fd);
                                table.appendColum(v);
                            }
                        }
                        //list.add(rows);
                    }
                    yellow(table.toString());
                    //TextTable table = new TextTable(hds,list);
                    //yellow(table.printTable());
                }
            }else if(obj instanceof JSONObject){
                JSONObject item = (JSONObject)obj;
                Iterator<String> keys = item.keys();
                ConsoleTable table=new ConsoleTable(item.keySet().size(),false);
                table.appendRow();
                table.appendColum("Key").appendColum("value");
                while(keys.hasNext()){
                    String _key = keys.next();
                    table.appendRow();
                    table.appendColum(_key).appendColum(item.get(_key));
                }
                yellow(table.toString());
            }
        }else{
            red(result.getString("message"));
        }
        return true;
    }

    private List<String> getHeader(JSONObject item){
        List<String> hds = new ArrayList<>();
        Iterator<String> keys = item.keys();
        while (keys.hasNext()){
            hds.add(keys.next());
        }
        return hds;
    }
    private ConsoleTable getHead(JSONObject item,
                                 boolean showAll,
                                 List<String> fields){
        if(showAll) {
            Iterator<String> keys = item.keys();

            ConsoleTable table = new ConsoleTable(item.keySet().size(), false, 20);
            table.appendRow();
            while (keys.hasNext()) {
                String key = keys.next();

                table.appendColum(key);
                //table.appendColum(Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a(key));
            }
            return table;
        }else{
            ConsoleTable table = new ConsoleTable(fields.size(),false,-1);
            table.appendRow();
            for(String fd:fields){
                table.appendColum(fd);
            }
            return table;
        }
    }



}
