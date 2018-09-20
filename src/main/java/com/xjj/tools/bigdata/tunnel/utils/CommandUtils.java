package com.xjj.tools.bigdata.tunnel.utils;

import com.xjj.tools.bigdata.tunnel.commands.*;
import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created by cjh on 18/9/20.
 */
public class CommandUtils {
    private Map<String,ExecutorBean> mapp = new TreeMap<String,ExecutorBean>();
    private static CommandUtils instance;
    private String inputLine;
    private CommandUtils(){

    }
    public static CommandUtils getInstance(){
        if(instance==null)
            instance = new CommandUtils();
        return instance;
    }

    public Map<String,ExecutorBean> getCommandMaps(){
        return mapp;
    }
    public void initCommandClass(){
        try {
            Reflections reflections = new Reflections(Config.getInstance().getString("shell_commands_package"));
            Set<Class<?>> classList = reflections.getTypesAnnotatedWith(CliCompent.class);
            for (Class clazz : classList) {
                //System.out.println(clazz.getName());
                Method[] methods = clazz.getDeclaredMethods();
                Field[] fields = clazz.getDeclaredFields();
                Object instance = clazz.newInstance();
                List<Field> autoSetFields = new ArrayList<Field>();
                for (Field fd : fields) {
                    AutoSetValue autoSetValue = fd.getAnnotation(AutoSetValue.class);
                    if (autoSetValue != null) {
                        autoSetFields.add(fd);
                    }
                }

                for (Method method : methods) {
                    CliMethod cliMethod = method.getAnnotation(CliMethod.class);
                    CheckSession checkSession = method.getAnnotation(CheckSession.class);
                    String key = method.getName();
                    if (cliMethod != null) {
                        String _key = cliMethod.key();
                        if (_key != null && _key.trim().length() > 0)
                            key = _key;
                        //System.out.println("    " + key + ":" + shellMethod.description());
                        ExecutorBean bean = new ExecutorBean();
                        bean.setCliMethod(cliMethod);
                        bean.setObject(instance);
                        bean.setMethod(method);
                        bean.setAutoSetFields(autoSetFields);
                        if(checkSession!=null){
                            bean.setCheckSession(checkSession);
                        }
                        mapp.put(key, bean);
                    }

                }

            }
        }catch (Exception ex){

        }
    }
    public boolean isSQLCommand(String input){
        if(Func.isEmpty(input)) return false;

        String[] pm= input.toUpperCase().split(" ");
        String cmd = pm[0];
        String sqlStart = "SELECT;CREATE;DELETE;UPSERT;INSERT;UPDATE;DROP;";
        return sqlStart.indexOf(cmd+";")!=-1;
    }
    public static String getShellPrompt(boolean moreLine){
        String prompt = "[@|green xjj-bigdata|@@@|yellow "+GlobalValue.userName+"|@:";
        if(!moreLine)
            return Ansi.ansi().eraseLine().render(prompt+">").toString();
        else{
            prompt= prompt.replaceAll("."," ");
            return Ansi.ansi().eraseLine().render(prompt+">").toString();
        }
    }
    public void listenInput(String[] args){
        try {
            ConsoleReader reader = new ConsoleReader();
            String line = null;
            boolean moreLine = false;
            boolean isSQLLine = false;
            StringBuilder stringBuf = new StringBuilder();
            do {
                line = reader.readLine(getShellPrompt(moreLine));
                if(Func.isEmpty(line)) continue;
                if(!isSQLLine&&isSQLCommand(line))
                    isSQLLine = true;
                if(isSQLLine) {
                    stringBuf.append(line+" ");
                    // 只有;结束，才执行
                    if (line.trim().endsWith(";")) {
                        String commandStr = stringBuf.toString();
                        inputLine = commandStr;
                        System.out.println(commandStr);
                        // 清空
                        stringBuf = new StringBuilder();
                        moreLine = false;
                        isSQLLine = false;
                    } else {
                        moreLine = true;
                    }
                }else{
                    //指令表
                    inputLine = line;
                    callMethod(line);
                    moreLine = false;
                    stringBuf = new StringBuilder();
                }
            }while(line!=null && !line.equals("exit"));
            System.exit(0);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public Object callMethod(String method,Object[] args){
        ExecutorBean bean = mapp.get(method);
        Object ret = null;
        if(bean!=null){
            try{
                for(Field fd :bean.getAutoSetFields()) {
                    if (fd.getName().equals("inputLine")) {
                        fd.setAccessible(true);
                        fd.set(bean.getObject(),inputLine);
                    }
                    if (fd.getName().equals("userName")) {
                        fd.setAccessible(true);
                        fd.set(bean.getObject(),GlobalValue.userName);
                    }
                }
                ret = bean.getMethod().invoke(bean.getObject(),args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            System.err.println("Not Found Command for '"+method+"'.");
        }
        return ret;
    }
    public void callMethod(String input){
        if(Func.isEmpty(input)) return;
        String[] cmds = input.split(" ");
        String method = cmds[0];
        ExecutorBean bean = mapp.get(method);
        if(bean!=null){
            try{
                int psCount = bean.getMethod().getParameterCount();
                Parameter[] parameters = bean.getMethod().getParameters();
                Object[] ps = new Object[psCount];
                //注入方法参数
                for(int i = 0;i<psCount;i++){
                    try {
                        String type = parameters[i].getType().getName();
                        if (type.equals("int") || type.equals("java.lang.Integer")) {
                            ps[i] = Integer.parseInt(cmds[i + 1]);
                        } else if (type.equals("long") || type.equals("java.lang.Long")) {
                            ps[i] = Long.parseLong(cmds[i + 1]);
                        } else if (type.equals("float") || type.equals("java.lang.Float")) {
                            ps[i] = Float.parseFloat(cmds[i + 1]);
                        } else if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
                            ps[i] = Boolean.parseBoolean(cmds[i + 1]);
                        } else {
                            ps[i] = cmds[i + 1];
                        }
                    }catch (Exception ex){}
                }
                //动态注入相关属性
                for(Field fd :bean.getAutoSetFields()) {
                    if (fd.getName().equals("inputLine")) {
                        fd.setAccessible(true);
                        fd.set(bean.getObject(),inputLine);
                    }
                    if (fd.getName().equals("userName")) {
                        fd.setAccessible(true);
                        fd.set(bean.getObject(),GlobalValue.userName);
                    }
                }
                long intime = System.currentTimeMillis();
                Object ret = Boolean.FALSE;
                //if(bean.getCheckSession()!=null){
                if(bean.getCliMethod().checkSession()){
                    if(GlobalValue.isLogin()){
                        ret = bean.getMethod().invoke(bean.getObject(), ps);
                        if(bean.getCliMethod().calcRequestTime()&&(Boolean)ret){
                            long outtime = System.currentTimeMillis();
                            System.out.println("--------------------");
                            System.out.println(method+" 请求使用了"+(outtime-intime)+"ms.");
                        }
                        //保留参数名这一选项由编译开关javac -parameters打开，默认是关闭的。
                        if(psCount!=(cmds.length-1)&&!(Boolean)ret) {
                            String buf = method + "参数不完整，参考：" + method ;
                            for(int i = 0;i<parameters.length;i++){
                                Parameter p = parameters[i];
                                buf+=" <"+p.getName()+">";
                            }
                            System.err.println(buf);
                        }
                    }else{
                        System.err.println("未登录状态，使用login指令进行登录：login <account> <password>");
                    }
                }else {
                    ret = bean.getMethod().invoke(bean.getObject(), ps);
                    if(bean.getCliMethod().calcRequestTime()&&(Boolean)ret){
                        long outtime = System.currentTimeMillis();
                        System.out.println("--------------------");
                        System.out.println(method+" 请求使用了"+(outtime-intime)+"ms.");
                    }
                    //保留参数名这一选项由编译开关javac -parameters打开，默认是关闭的。
                    if(psCount!=(cmds.length-1)&&!(Boolean)ret) {
                        String buf = method + "参数不完整，参考：" + method ;
                        for(int i = 0;i<parameters.length;i++){
                            Parameter p = parameters[i];
                            buf+=" <"+p.getName()+">";
                        }
                        System.err.println(buf);
                    }
                }
            } catch (Exception e){
                //e.printStackTrace();
            }
        }else{
            System.err.println("Not Found Command for '"+method+"'.");
        }
    }
}
