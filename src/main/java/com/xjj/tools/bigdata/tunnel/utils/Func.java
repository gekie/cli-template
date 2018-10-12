package com.xjj.tools.bigdata.tunnel.utils;

import org.json.JSONArray;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cjh on 18/9/19.
 */
public class Func {
    public static String format(long timestamp){
        return format(timestamp,"yyyy-MM-dd HH:mm:ss");
    }
    public static String format(long timestap,String fmt){
        SimpleDateFormat df = new SimpleDateFormat(fmt);
        Date date = new Date(timestap);
        return df.format(date);
    }

    public static String getFileSize(double filesize_bytes){
        DecimalFormat mD = new DecimalFormat("####.##");
        StringBuffer sb = new StringBuffer();
        if (filesize_bytes<(1024.0)){return sb.append(mD.format(filesize_bytes)).append("B").toString();}
        if (filesize_bytes<(1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024.0))).append("KB").toString();}
        if (filesize_bytes<(1024*1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024*1024.0))).append("MB").toString();}
        if (filesize_bytes<(1024*1024*1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024*1024*1024.0))).append("GB").toString();}
        if (filesize_bytes<(1024*1024*1024*1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024*1024*1024*1024.0))).append("TB").toString();}
        return ""+filesize_bytes;
    }
    public static String getFileSize(long filesize_bytes){
        DecimalFormat mD = new DecimalFormat("####.##");
        StringBuffer sb = new StringBuffer();
        if (filesize_bytes<(1024.0)){return sb.append(mD.format(filesize_bytes)).append("B").toString();}
        if (filesize_bytes<(1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024.0))).append("KB").toString();}
        if (filesize_bytes<(1024*1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024*1024.0))).append("MB").toString();}
        if (filesize_bytes<(1024*1024*1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024*1024*1024.0))).append("GB").toString();}
        if (filesize_bytes<(1024*1024*1024*1024*1024.0)){return sb.append(mD.format(filesize_bytes/(1024*1024*1024*1024.0))).append("TB").toString();}
        return ""+filesize_bytes;
    }
    public static boolean isEmpty(String str){
        return str==null||str.trim().length()==0;
    }

    public static String readFile(String file){
        try{
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            byte[] data = outputStream.toByteArray();
            fileInputStream.close();
            return new String(data,"UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void saveToFile(String content,String filename){
        FileOutputStream fop = null;
        File file = null;
        try{
            file = new File(filename);
            fop = new FileOutputStream(file);
            if(!file.exists())
                file.createNewFile();
            fop.write(content.getBytes("UTF-8"));
            fop.flush();
            fop.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static JSONArray loadJSONFromFile(String filename){
        File file = new File(filename);
        JSONArray json = null;
        if (file.exists()) {
            try {
                json = new JSONArray(Func.readFile(filename));
            }catch(org.json.JSONException ex){
                json = new JSONArray();
            }
        }else
            json = new JSONArray();
        return json;
    }
    public static Object parseParameterValue(String type,String value){
        if (type.equals("int") || type.equals("java.lang.Integer")) {
            return Integer.parseInt(value);
        } else if (type.equals("long") || type.equals("java.lang.Long")) {
            return Long.parseLong(value);
        } else if (type.equals("float") || type.equals("java.lang.Float")) {
            return Float.parseFloat(value);
        } else if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
           return Boolean.parseBoolean(value);
        } else {
            return value;
        }
    }
    public static Object initNormalValue(String type){
        if (type.equals("int") || type.equals("java.lang.Integer")) {
            return new Integer(0);
        } else if (type.equals("long") || type.equals("java.lang.Long")) {
            return new Long(0);
        } else if (type.equals("float") || type.equals("java.lang.Float")) {
            return new Float(0.0);
        } else if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
            return new Boolean(false);
        } else {
            return null;
        }
    }
}
