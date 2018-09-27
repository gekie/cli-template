package com.xjj.tools.bigdata.tunnel.utils;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Created by cjh on 15/3/20.
 */
public class PostParam implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    HashMap<String, Object> param;
    private boolean zip;

    public PostParam(boolean zip) {
        this();
        this.zip = zip;
    }

    public PostParam() {
        param = new HashMap<String, Object>();
    }

    public void addParam(String name, String value) {
        if (value == null)
            param.put(name, "");
        else
            param.put(name, value);
    }

    public void addParam(String name, int value) {
        param.put(name, value);
    }

    public void addParam(String name, long value) {
        param.put(name, value);
    }

    public void addParam(String name, ArrayList value) {
        param.put(name, value);
    }

    public byte[] getParamBytes() {
        return getParamBytes("UTF-8");
    }

    public byte[] getParamBytes(String charset) {
        byte[] bytes = null;
        try {
            Iterator iter = param.entrySet().iterator();
            StringBuffer buf = new StringBuffer();
            while (iter.hasNext()) {
                Entry<String, String> entry = (Entry<String, String>) iter.next();
                String key = entry.getKey();
                Object _value = entry.getValue();

                if (_value instanceof String) {
                    String value = _value == null ? "" : (String) _value;
                    buf.append(URLEncoder.encode(key, charset));
                    buf.append("=");
                    buf.append(URLEncoder.encode(value, charset));
                    buf.append("&");
                } else if (_value instanceof Integer) {
                    buf.append(URLEncoder.encode(key, charset));
                    buf.append("=");
                    buf.append(_value);
                    buf.append("&");

                } else if (_value instanceof Long) {
                    buf.append(URLEncoder.encode(key, charset));
                    buf.append("=");
                    buf.append(_value);
                    buf.append("&");
                } else if(_value instanceof ArrayList){
                    ArrayList list = (ArrayList)_value;
                    for(int i = 0;i<list.size();i++){
                        String v = (String)list.get(i);
                        buf.append(URLEncoder.encode(key, charset));
                        buf.append("=");
                        buf.append(v);
                        buf.append("&");
                    }
                }
            }
            //buf.append("rand="+System.currentTimeMillis());
            String url = buf.toString();
            bytes = url.getBytes(charset);
        } catch (Exception ex) {
            ex.printStackTrace();
            bytes = new byte[0];
        }
        return bytes;
    }

    public boolean isZip() {
        return zip;
    }

    public HashMap<String, Object> getParam(){
        return param;
    }
}
