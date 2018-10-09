package com.xjj.tools.bigdata.tunnel.utils;

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
}
