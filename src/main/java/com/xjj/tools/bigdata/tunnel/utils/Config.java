package com.xjj.tools.bigdata.tunnel.utils;

import java.io.*;
import java.util.Properties;

/**
 * Created by cjh on 18/9/19.
 */
public class Config {
    private static Config inst;
    private Properties prop;
    private Config(){
        try {
            File directory = new File("");//设定为当前文件夹
            String path = directory.getAbsolutePath();
            if(path.indexOf(File.separator+"bin")!=-1)
                path = path+File.separator+"..";
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path+File.separator+"conf"+File.separator+"config.properties"))); //方法1
            prop = new Properties();
            prop.load(new InputStreamReader(inputStream, "UTF-8")); //加载格式化后的流
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    public static Config getInstance(){
        if(inst==null){
            inst = new Config();
        }
        return inst;
    }
    public String getString(String key){
        String tmp = prop.getProperty(key);
        return tmp!=null?tmp:"";
    }
    public int getInteger(String key){
        String tmp = getString(key);
        if(Func.isEmpty(tmp)){
            return 0;
        }else{
            return Integer.parseInt(tmp);
        }
    }
}