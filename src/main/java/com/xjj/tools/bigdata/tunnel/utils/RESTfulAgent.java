package com.xjj.tools.bigdata.tunnel.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.List;

/**
 * Created by cjh on 18/9/19.
 */
public class RESTfulAgent {
    final String userAgent = "xjj-bigdata-api";
    private static RESTfulAgent inst;
    public static RESTfulAgent getInstance(){
        if(inst==null){
            inst = new RESTfulAgent();
        }
        return inst;
    }

    /**
     * 同步调用
     */
    public String doPost(String url, PostParam params) throws DoPostException {
        HttpURLConnection conn = null;
        URL _url = null;
        OutputStream out = null;
        InputStream in = null;
        String result = "";
        try {
            _url = new URL(url);
            conn = (HttpURLConnection) _url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setRequestProperty("Accept", "application/octet-stream, */*");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("UseZip", Boolean.toString(params.isZip()));
            if(GlobalValue.ticket!=null){
                conn.setRequestProperty("ticket", GlobalValue.ticket);
            }
            if(GlobalValue.sessionId!=null){
                conn.addRequestProperty("Cookie", GlobalValue.sessionId);
            }
            out = conn.getOutputStream();
            if (params != null)
                out.write(params.getParamBytes());
            out.flush();
            int code = conn.getResponseCode();
            String cookieval = conn.getHeaderField("set-cookie");
            if(cookieval != null){
                String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                GlobalValue.sessionId = sessionid;
            }
            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                in = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
                result = content.toString();
            } else {
                throw new DoPostException(conn.getResponseMessage() + " 错误代码：" + conn.getResponseCode());
            }
        } catch (OutOfMemoryError er) {
            throw new DoPostException("内在溢出");
        } catch (Error er) {
            er.printStackTrace();
            throw new DoPostException("出错了");
        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            throw new DoPostException("连接服务器超时");
        } catch (SocketException ex) {
            ex.printStackTrace();
            throw new DoPostException("无法连接服务器");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new DoPostException("连接服务器异常，请检查网络");
        } catch (IOException e) {
            e.printStackTrace();
            throw new DoPostException("网络数据读写异常，请检查网络");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DoPostException("无法连接服务器");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }


    /**
     * 同步调用
     */
    public String doGet(String url) throws DoPostException {
        HttpURLConnection conn = null;
        URL _url = null;
        OutputStream out = null;
        InputStream in = null;
        String result = "";
        try {
            CookieManager manager = new CookieManager();
            _url = new URL(url);
            conn = (HttpURLConnection) _url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setRequestProperty("Accept", "application/octet-stream, */*");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", userAgent);
            if(GlobalValue.ticket!=null){
                conn.setRequestProperty("ticket", GlobalValue.ticket);
            }
            if(GlobalValue.sessionId!=null){
                conn.addRequestProperty("Cookie", GlobalValue.sessionId);
            }
            int code = conn.getResponseCode();
            String cookieval = conn.getHeaderField("set-cookie");
            if(cookieval != null){
                String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                GlobalValue.sessionId = sessionid;
            }

            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                in = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
                result = content.toString();
            } else {
                throw new DoPostException(conn.getResponseMessage() + " 错误代码：" + conn.getResponseCode());
            }
        } catch (OutOfMemoryError er) {
            throw new DoPostException("内在溢出");
        } catch (Error er) {
            er.printStackTrace();
            throw new DoPostException("出错了");
        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            throw new DoPostException("连接服务器超时");
        } catch (SocketException ex) {
            ex.printStackTrace();
            throw new DoPostException("无法连接服务器");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new DoPostException("连接服务器异常，请检查网络");
        } catch (IOException e) {
            e.printStackTrace();
            throw new DoPostException("网络数据读写异常，请检查网络");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DoPostException("无法连接服务器");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

    /**
     * 异步Post请求，返回json
     *
     * @param url          Post请求地址
     * @param listener回调接口
     */
    public void doPost(final String url, final String ticket ,final PostParam params, final OnDoPostDataListener listener) {
        MessageExecutorService.getInstance().execute(new Runnable() {
            //protected String doInBackground(String... pms) {
            public void run() {
                HttpURLConnection conn = null;
                URL _url = null;
                OutputStream out = null;
                InputStream in = null;
                String result = "";
                try {
                    listener.onShowMessage("正在准备连接服务...", 2);
                    _url = new URL(url);
                    conn = (HttpURLConnection) _url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setRequestMethod("POST");
                    //conn.setRequestProperty("Connection", "Keep-Alive");
                    //conn.setRequestProperty("Accept", "application/octet-stream, */*");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("User-Agent", userAgent);
                    conn.setRequestProperty("UseZip", Boolean.toString(params.isZip()));
                    if(ticket!=null) {
                        conn.setRequestProperty("ticket", ticket);
                        conn.setRequestProperty("accessToken", ticket);
                    }else{
                        if(GlobalValue.ticket!=null){
                            conn.setRequestProperty("ticket", GlobalValue.ticket);
                        }
                    }
                    if(GlobalValue.sessionId!=null){
                        conn.addRequestProperty("Cookie", GlobalValue.sessionId);
                    }
                    conn.setUseCaches(false);
                    listener.onShowMessage("正在连接服务...", 10);
                    out = conn.getOutputStream();
                    listener.onShowMessage("正在发送请求参数...", 50);
                    if (params != null)
                        out.write(params.getParamBytes());
                    out.flush();
                    listener.onShowMessage("正在发送请求参数...", 80);
                    int code = conn.getResponseCode();
                    String cookieval = conn.getHeaderField("set-cookie");
                    if(cookieval != null){
                        String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                        GlobalValue.sessionId = sessionid;
                    }
                    if (code == HttpURLConnection.HTTP_OK) {
                        StringBuilder content = new StringBuilder();
                        in = conn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            content.append(line);
                        }
                        result = content.toString();
                        //listener.onDataReceiver(result);
                        listener.onDataReceiver(result);
                        listener.onShowMessage("接收数据完成", 100);
                    } else {
                        listener.onShowMessage("", 100);
                        listener.onError(conn.getResponseMessage() + " 错误代码：" + conn.getResponseCode());
                    }
                } catch (OutOfMemoryError er) {
                    er.printStackTrace();
                    listener.onError("内存溢出");
                } catch (Error er) {
                    er.printStackTrace();
                    listener.onError("出错了");
                } catch (SocketTimeoutException ex) {
                    ex.printStackTrace();
                    listener.onError("连接服务器超时");
                } catch (SocketException ex) {
                    ex.printStackTrace();
                    listener.onError("无法连接服务器");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    listener.onError("连接服务器异常，请检查网络");
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError("网络数据读写异常，请检查网络");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listener.onError("无法连接服务器");
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }

                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }
    public void get(String url, OnLoadDataListener listener) {
        get(url, "UTF-8", listener);
    }

    public void get(String url, boolean zip, OnLoadDataListener listener) {
        get(url, zip, "UTF-8", listener);
    }
    public void get(final String url, final String charset, final OnLoadDataListener listener) {
        get(url, false, charset, listener);
    }
    public void get(final String url, final boolean zip, final String charset, final OnLoadDataListener listener) {
        if (url == null)
            return;
        MessageExecutorService.getInstance().execute(new Runnable() {
            public void run() {
                HttpURLConnection conn = null;
                URL _url = null;
                try {
                    _url = new URL(url);
                    conn = (HttpURLConnection) _url.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Accept", "*/*");
                    conn.setRequestProperty("user-agent", userAgent);
                    if(GlobalValue.ticket!=null){
                        conn.setRequestProperty("ticket", GlobalValue.ticket);
                    }
                    if(GlobalValue.sessionId!=null){
                        conn.addRequestProperty("Cookie", GlobalValue.sessionId);
                    }
                    conn.connect();
                    String cookieval = conn.getHeaderField("set-cookie");
                    if(cookieval != null){
                        String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                        GlobalValue.sessionId = sessionid;
                    }
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStreamReader in = new InputStreamReader(
                                conn.getInputStream(), charset);
                        StringBuffer buf = new StringBuffer();

                        char[] content = new char[2048];
                        int num = 0;
                        while (num > -1) {
                            num = in.read(content);
                            if (num <= 0)
                                break;
                            buf.append(new String(content, 0, num));
                        }
                        in.read(content);
                        in.close();
                        listener.onDataReceiver(buf.toString());
                    } else {
                        String message = "读取数据失败:" + conn.getResponseMessage() + " 错误代码：" + conn.getResponseCode();
                        listener.onError(message);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    listener.onError(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e.getMessage());
                } finally {
                    if (conn != null)
                        conn.disconnect();
                }
            }
        });
    }

    public JSONObject postContent(String request, String output) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            //建立连接
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            if(GlobalValue.ticket!=null){
                connection.setRequestProperty("ticket", GlobalValue.ticket);
            }
            if(GlobalValue.sessionId!=null){
                connection.addRequestProperty("Cookie", GlobalValue.sessionId);
            }
            if (output != null) {
                OutputStream out = connection.getOutputStream();
                out.write(output.getBytes("UTF-8"));
                out.close();
            }
            //流处理
            InputStream input = connection.getInputStream();
            InputStreamReader inputReader = new InputStreamReader(input, "UTF-8");
            BufferedReader reader = new BufferedReader(inputReader);
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String cookieval = connection.getHeaderField("set-cookie");
            if(cookieval != null){
                String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                GlobalValue.sessionId = sessionid;
            }
            //关闭连接、释放资源
            reader.close();
            inputReader.close();
            input.close();
            input = null;
            connection.disconnect();
            String txt = buffer.toString();
            //System.out.println("postContent:" + txt);
            jsonObject = new JSONObject(txt);
        } catch (Exception e) {
        }
        return jsonObject;
    }
    public String doMultipartPost(final String url, final MultipartEntity part) {
        String serverResponse = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(part);
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            int code = response.getStatusLine().getStatusCode();
            if (code == HttpURLConnection.HTTP_OK) {
                serverResponse = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("error in doMultipartPost:"+ex.getMessage());
        }
        return serverResponse;
    }
    public void doPost(final String url, final String jsonParam, final OnDoPostDataListener listener) {
        Thread postThread = new Thread(new Runnable() {
            //protected String doInBackground(String... pms) {
            public void run() {
                HttpURLConnection conn = null;
                URL _url = null;
                OutputStreamWriter out = null;
                InputStream in = null;
                String result = "";
                try {
                    listener.onShowMessage("正在准备连接服务...", 2);
                    _url = new URL(url);
                    conn = (HttpURLConnection) _url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
                    conn.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
                    conn.setRequestProperty("User-Agent", userAgent);
                    if(GlobalValue.ticket!=null){
                        conn.setRequestProperty("ticket", GlobalValue.ticket);
                    }
                    if(GlobalValue.sessionId!=null){
                        conn.addRequestProperty("Cookie", GlobalValue.sessionId);
                    }
                    conn.setUseCaches(false);
                    listener.onShowMessage("正在连接服务...", 10);
                    out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8"); // utf-8编码
                    listener.onShowMessage("正在发送请求参数...", 50);
                    out.append(jsonParam);
                    out.flush();
                    listener.onShowMessage("正在发送请求参数...", 80);
                    int code = conn.getResponseCode();
                    String cookieval = conn.getHeaderField("set-cookie");
                    if(cookieval != null){
                        String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                        GlobalValue.sessionId = sessionid;
                    }
                    if (code == HttpURLConnection.HTTP_OK) {
                        StringBuilder content = new StringBuilder();
                        in = conn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            content.append(line);
                        }
                        result = content.toString();
                        //listener.onDataReceiver(result);
                        listener.onDataReceiver(result);
                        listener.onShowMessage("接收数据完成", 100);
                    } else {
                        listener.onShowMessage("", 100);
                        listener.onError(conn.getResponseMessage() + " 错误代码：" + conn.getResponseCode());
                    }
                } catch (OutOfMemoryError er) {
                    er.printStackTrace();
                    listener.onError("内存溢出");
                } catch (Error er) {
                    er.printStackTrace();
                    listener.onError("出错了");
                } catch (SocketTimeoutException ex) {
                    ex.printStackTrace();
                    listener.onError("连接服务器超时");
                } catch (SocketException ex) {
                    ex.printStackTrace();
                    listener.onError("无法连接服务器");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    listener.onError("连接服务器异常，请检查网络");
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError("网络数据读写异常，请检查网络");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listener.onError("无法连接服务器");
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }

                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
        postThread.start();
    }
    public JSONObject loadObject(String url){
        return loadObject(url,new PostParam());
    }
    public JSONObject getObject(String url){
        try{
            String content = doGet(url);
            JSONObject obj = new JSONObject(content);
            return obj;
        }catch(DoPostException ex){
            JSONObject obj = new JSONObject();
            obj.put("errorCode",-1);
            obj.put("message",ex.getMessage());
            return obj;
        }
    }
    public JSONObject loadObject(String url,String jsonParam){
        try{
            String content = doPost(url,jsonParam);
            JSONObject obj = new JSONObject(content);
            return obj;
        }catch(DoPostException ex){
            JSONObject obj = new JSONObject();
            obj.put("errorCode",-1);
            obj.put("message",ex.getMessage());
            return obj;
        }
    }
    public JSONObject loadObject(String url,PostParam pm){
        try{
            String content = doPost(url,pm);
            JSONObject obj = new JSONObject(content);
            return obj;
        }catch(DoPostException ex){
            JSONObject obj = new JSONObject();
            obj.put("errorCode",-1);
            obj.put("message",ex.getMessage());
            return obj;
        }
    }
    /**
     * 同步调用
     */
    public String doPost(String url, String jsonParam) throws DoPostException {
        return doPostForHeader(url,jsonParam,null);
    }
    public String doPostForHeader(String url, String jsonParam,PostParam header) throws DoPostException {
        HttpURLConnection conn = null;
        URL _url = null;
        OutputStreamWriter out = null;
        InputStream in = null;
        String result = "";
        try {
            _url = new URL(url);
            conn = (HttpURLConnection) _url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            conn.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            conn.setRequestProperty("User-Agent", userAgent);
            if(GlobalValue.ticket!=null){
                conn.setRequestProperty("ticket", GlobalValue.ticket);
            }
            if(GlobalValue.sessionId!=null){
                conn.addRequestProperty("Cookie", GlobalValue.sessionId);
            }
            if(header!=null){

            }
            conn.setUseCaches(false);
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(jsonParam);
            out.flush();
            int code = conn.getResponseCode();
            String cookieval = conn.getHeaderField("set-cookie");
            if(cookieval != null){
                String sessionid = cookieval.substring(0, cookieval.indexOf(";"));
                GlobalValue.sessionId = sessionid;
            }
            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                in = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
                result = content.toString();
            } else {
                throw new DoPostException(conn.getResponseMessage() + " 错误代码：" + conn.getResponseCode());
            }
        } catch (OutOfMemoryError er) {
            throw new DoPostException("内在溢出");
        } catch (Error er) {
            er.printStackTrace();
            throw new DoPostException("出错了");
        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            throw new DoPostException("连接服务器超时");
        } catch (SocketException ex) {
            ex.printStackTrace();
            throw new DoPostException("无法连接服务器");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new DoPostException("连接服务器异常，请检查网络");
        } catch (IOException e) {
            e.printStackTrace();
            throw new DoPostException("网络数据读写异常，请检查网络");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DoPostException("无法连接服务器");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }
}
