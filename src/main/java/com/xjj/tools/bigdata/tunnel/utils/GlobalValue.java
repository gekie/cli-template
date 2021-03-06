package com.xjj.tools.bigdata.tunnel.utils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by cjh on 18/9/19.
 */
public class GlobalValue {
    public static String userName= "nologin";
    public static String ticket;
    public static String account ;
    public static String appid="";
    public static String dbSkey;
    public static String sessionId;
    public static HashMap<String,JSONObject> tables;
    public static String COMMAND_HISTORY_FILE = "";
    public static String loginURL = Config.getInstance().getString("login_url");
    public static String endPoint = Config.getInstance().getString("end_point");
    public static String MY_TABLE_API;
    public static String EXECUTE_SQL_API;
    public static String QUERY_SQL_API;
    public static String Create_Repository_API;
    public static String Drop_Repository_API;
    public static String DATA_UPLOAD_API;
    public static String SHOW_UPLOAD_LOG;
    public static String exportCSV_API;

    public static int printMaxRow = Config.getInstance().getInteger("print_max_row");
    public static boolean isLogin(){
        return ticket!=null&&ticket.trim().length()>0;
    }
    public static void init(){
        resetApi();
        File directory = new File("");//设定为当前文件夹
        String path = directory.getAbsolutePath();
        if(path.indexOf(File.separator+"bin")!=-1)
            path = path+File.separator+"..";
        COMMAND_HISTORY_FILE = path+File.separator+"history.log";
        File f = new File(COMMAND_HISTORY_FILE);
        if(!f.exists()){
            try {
                f.createNewFile();
            }catch (IOException ex){

            }
        }
    }
    public static void resetApi(){
        MY_TABLE_API=endPoint+"bigdata/loadMyAppConfig";
        EXECUTE_SQL_API=endPoint+"bigdata/excuteSQL";
        QUERY_SQL_API=endPoint+"bigdata/querySQL";
        Create_Repository_API=endPoint+"bigdata/saveConfig";
        Drop_Repository_API=endPoint+"bigdata/removeConfig";
        DATA_UPLOAD_API=endPoint+"bigdata/ReceiveDataFile";
        SHOW_UPLOAD_LOG=endPoint+"bigdata/showImportDataLog";
        exportCSV_API=endPoint+"bigdata/exportCSV";
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
