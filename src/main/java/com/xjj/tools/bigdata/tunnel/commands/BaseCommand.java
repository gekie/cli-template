package com.xjj.tools.bigdata.tunnel.commands;
import com.xjj.tools.bigdata.tunnel.utils.CommandUtils;
import com.xjj.tools.bigdata.tunnel.utils.GlobalValue;
import org.fusesource.jansi.Ansi;
import org.jline.reader.LineReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cjh on 18/9/20.
 */
public class BaseCommand {
    protected void print(Object text,Ansi.Color fgColor){
        print(text,0,null,fgColor);
    }
    protected void resetPrint(){
        println(Ansi.ansi().reset());
    }
    protected void println(Object text,Ansi.Color fgColor){
        println(text,0,null,fgColor);
    }
    protected Ansi getAnsi(int column,Ansi.Color bgColor,Ansi.Color fgColor){
        Ansi ansi =  Ansi.ansi();
        if(bgColor!=null)
            ansi.bg(bgColor);
        if(fgColor!=null)
            ansi.fg(fgColor);
        if(column!=0)
            ansi.cursorToColumn(column);
        return ansi;
    }
    protected void print(Object text,int column){
        Ansi ansi = getAnsi(column,null,null);
        print(ansi.a(text));
    }
    protected void print(Object text,int column,Ansi.Color bgColor,Ansi.Color fgColor){
        print(getAnsi(column,bgColor,fgColor).a(text).reset());
    }
    protected void print(Object text,int column,Ansi.Color fgColor){
        print(getAnsi(column,null,fgColor).a(text).reset());
    }
    protected void println(Object text,int column,Ansi.Color bgColor,Ansi.Color fgColor){
        print(text,column,bgColor,fgColor);
        System.out.println();
    }
    protected void print(Object text){
        System.out.print(text);
    }
    protected void println(Object text){
        System.out.println(text);
    }
    protected void err(Object text){
        System.err.println(text);
    }
    protected void green(Object text){
        //println(text, Ansi.Color.GREEN);
        println(text,Ansi.Color.GREEN);
    }
    protected void yellow(Object text){
        println(text,Ansi.Color.YELLOW);
    }
    protected void red(Object text){
        println(text, Ansi.Color.RED);
    }
    protected boolean containsKey(HashMap map,String key){
        if(map.containsKey(key)){
            return true;
        }else if(map.containsKey(key.toLowerCase())){
            return true;
        }else if(map.containsKey(key.toUpperCase())){
            return true;
        }else
            return false;
    }
    protected Object getObject(JSONObject item, String key){
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
    protected Object getObject(HashMap item,String key){
        if(item.containsKey(key)){
            return item.get(key);
        } else if (item.containsKey(key.toUpperCase())) {
            return item.get(key.toUpperCase());
        }else if(item.containsKey(key.toLowerCase())){
            return item.get(key.toLowerCase());
        }else
            return null;
    }
    protected JSONArray sortJsonArray(JSONArray items, final String sortField, final boolean desc){
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
                        //String af = av.toString().substring(0,1);
                        String af = av.toString();
                        //String bf = bv.toString().substring(0,1);
                        String bf = bv.toString();
                        if(desc){
                            retVal=bf.compareTo(af);
                        }else{
                            retVal = af.compareTo(bf);
                        }
                    }
                }

                return retVal;
            }
        });
        JSONArray _items = new JSONArray(list);
        return _items;
    }

    protected void eraseLine(){
        println(Ansi.ansi().cursorToColumn(1).eraseLine());
    }
    protected void printProgress(long num,long max){
        if(!CommandUtils.getInstance().isHideCursor()) {
            CommandUtils.getInstance().hideCursor();
            eraseLine();
        }
        print("[", 1, Ansi.Color.WHITE);
        String p = Integer.toString(Math.round((num / (float) max) * 100)) + "%";
        print(p, 2,  Ansi.Color.GREEN);
        print("/", Ansi.Color.WHITE);
        print("100%", Ansi.Color.YELLOW);
        print(num,20);
        print("/", Ansi.Color.WHITE);
        print(max+" Bytes", Ansi.Color.YELLOW);
        print("]", Ansi.Color.WHITE);
    }
}
