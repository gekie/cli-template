package com.xjj.tools.bigdata.tunnel.utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by cjh on 18/9/19.
 */
public class GlobalValue {
    public static String userName= "nologin";
    public static String ticket;
    public static String account ;
    public static String appid;
    public static String dbSkey;
    public static String sessionId;
    public static HashMap<String,JSONObject> tables;
    public static String endPoint = Config.getInstance().getString("end_point");
    public static String MY_TABLE_API=endPoint+"bigdata/loadMyAppConfig";
    public static boolean isLogin(){
        return ticket!=null&&ticket.trim().length()>0;
    }
    public static void reset(){
        ticket = "";
        userName = "nologin";
        appid = "";
        account = "";
        sessionId = "";
        dbSkey = "";
        if(tables!=null)
            tables.clear();
    }
}
