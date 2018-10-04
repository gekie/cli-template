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
    private Object getObject(JSONObject item,String key){
        if(item.has(key)){
            return item.get(key);
        }
        if(item.has(key.toLowerCase())){
            return item.get(key.toLowerCase());
        }
        if(item.has(key.toUpperCase())){
            return item.get(key.toUpperCase());
        }
        return null;
    }
    private Object getObject(HashMap item,String key){
        if(item.containsKey(key)){
            return item.get(key);
        } else if (item.containsKey(key.toUpperCase())) {
            return item.get(key.toUpperCase());
        }else if(item.containsKey(key.toLowerCase())){
            return item.get(key.toLowerCase());
        }else
            return null;
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
    private boolean containsKey(HashMap map,String key){
        if(map.containsKey(key)){
            return true;
        }else if(map.containsKey(key.toLowerCase())){
            return true;
        }else if(map.containsKey(key.toUpperCase())){
            return true;
        }else
            return false;
    }
    private JSONArray sortJsonArray(JSONArray items, final String sortField, final boolean desc){
        List<Object> list = items.toList();
        Collections.sort(list, new Comparator() {
            public int compare(Object obj1,Object obj2){
                int retVal=0;
                HashMap a = (HashMap) obj1;
                HashMap b = (HashMap)obj2;
                if(containsKey(a,sortField)){
                    Object av = getObject(a,sortField);
                    Object bv = getObject(b,sortField);
                    String type = av.getClass().getName();
                    if(type.equals("java.lang.Integer")) {
                        Integer ai = new Integer(av.toString());
                        Integer bi = new Integer(bv.toString());
                        if (desc) {
                            if(bi>ai) return 1;
                            else if(bi==ai) return 0;
                            else
                                return -1;
                        } else {
                            if(ai>bi) return 1;
                            else if(ai==bi) return 0;
                            else
                                return -1;
                        }
                    }else if(type.equals("java.lang.Float")) {
                        if (desc) {
                            if ((Float) bv > (Float) av) return 1;
                            if ((Float) bv == (Float) av) return 0;
                            return -1;
                        } else {
                            if ((Float) av > (Float) bv) return 1;
                            if ((Float) av == (Float) bv) return 0;
                            return -1;
                        }
                    }else if(type.equals("java.lang.Double")){
                        if (desc) {
                            if ((Double) bv > (Double) av) return 1;
                            if ((Double) bv == (Double) av) return 0;
                            return -1;
                        } else {
                            if ((Double) av > (Double) bv) return 1;
                            if ((Double) av == (Double) bv) return 0;
                            return -1;
                        }
                    }else if(type.equals("java.lang.Long")){
                        if (desc) {
                            if ((Long) bv > (Long) av) return -1;
                            if ((Long) bv == (Long) av) return 0;
                            return -1;
                        } else {
                            if ((Long) av > (Long) bv) return 1;
                            if ((Long) av == (Long) bv) return 0;
                            return -1;
                        }
                    }else if(type.equals("java.lang.String")){
                        String af = av.toString().substring(0,1);
                        String bf = bv.toString().substring(0,1);
                        if(desc){
                            retVal=af.compareTo(bf);
                        }else{
                            retVal = bf.compareTo(af);
                        }
                    }
                }

                return retVal;
            }
        });
        JSONArray _items = new JSONArray(list);
        return _items;
    }
}
