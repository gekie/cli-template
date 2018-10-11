package com.xjj.tools.bigdata.tunnel.utils;

import com.xjj.tools.bigdata.tunnel.commands.*;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.reflections.Reflections;

import java.io.IOException;
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
    //private ConsoleReader reader;
    private LineReader reader;
    private boolean cursor;
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
        String sqlStart = "SELECT;DELETE;UPSERT;INSERT;CREATE;";
        return sqlStart.indexOf(cmd+";")!=-1;
    }
    public static String getShellPrompt(boolean moreLine){
        String prompt = "[@|green xjj-bigdata|@@@|yellow "+GlobalValue.userName+"|@:";
        //String prompt = "[\u001B[32mxjj-bigdata@\u001B[33m"+GlobalValue.userName+"\u001B[32m:";
        if(!moreLine)
            return Ansi.ansi().render(prompt+">").toString();
        else{
            prompt = "[xjj-bigdata@"+GlobalValue.userName+"..:>";
            prompt= prompt.replaceAll(".",".");
            return "\u001b[33m"+prompt+">";
        }
    }
    private void print(Object obj){
        System.out.print(obj);
    }
    private void println(Object obj){
        System.out.println(obj);
    }
    private void initReader() throws IOException {
        Terminal terminal = TerminalBuilder.builder().system(true).jansi(true).build();
        List<Completer> completors = new ArrayList<Completer>();
        Iterator<String> keys = mapp.keySet().iterator();
        String[] arrs = new String[mapp.size()];
        int i = 0;
        while(keys.hasNext()){
            arrs[i++] = keys.next();
        }
        completors.add(new StringsCompleter(arrs));
        completors.add(new Completers.FileNameCompleter());
        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.HISTORY_FILE,GlobalValue.COMMAND_HISTORY_FILE)
                .variable(LineReader.HISTORY_FILE_SIZE,Config.getInstance().getInteger("command_history_max_size"))
                .completer(new ArgumentCompleter(completors))
                .appName("xjj-bigdata").build();
    }
    public LineReader getConsoleReader(){
        return reader;
    }
    public void listenInput(String[] args){
        try {
            GlobalValue.init();
            initReader();
            String line = null;
            boolean moreLine = false;
            boolean isSQLLine = false;
            StringBuilder stringBuf = new StringBuilder();
            do {
                line = reader.readLine(getShellPrompt(moreLine));
                if (Func.isEmpty(line)){
                    showCursor();
                    continue;
                }
                if (!isSQLLine && isSQLCommand(line))
                    isSQLLine = true;
                if (isSQLLine) {
                    stringBuf.append(line + " ");
                    // 只有;结束，才执行
                    if (line.trim().endsWith(";")) {
                        String commandStr = stringBuf.toString();
                        inputLine = commandStr;
                        //System.out.println(commandStr);
                        long intime = System.currentTimeMillis();
                        if (commandStr.toUpperCase().startsWith("SELECT"))
                            callMethod("querySQL", new String[]{commandStr});
                        else if (commandStr.toUpperCase().startsWith("CREATE"))
                            callMethod("createSQL", new String[]{commandStr});
                        long outtime = System.currentTimeMillis();
                        System.out.println("--------------------");
                        System.out.println("SQL Execute use " + (outtime - intime) + "ms.");
                        // 清空
                        stringBuf = new StringBuilder();
                        moreLine = false;
                        isSQLLine = false;
                        //history.flush();
                    } else {
                        moreLine = true;
                    }
                } else {
                    //指令表
                    inputLine = line;
                    callMethod(line);
                    moreLine = false;
                    stringBuf = new StringBuilder();
                    //history.flush();
                }
            } while (line != null && !line.equals("exit"));
            //history.flush();
            System.exit(0);
        }catch(UserInterruptException ex) {
            ex.printStackTrace();
        }catch (EndOfFileException ex){
            ex.printStackTrace();
            return;
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
        //if(input.startsWith("history")) return;
        String[] cmds = input.split(" ");
        String method = cmds[0];
        if(method.toLowerCase().equals("show")){
            method = cmds[1];
        }
        ExecutorBean bean = mapp.get(method);
        if(bean!=null){
            try{
                int psCount = bean.getMethod().getParameterCount();
                Parameter[] parameters = bean.getMethod().getParameters();
                Object[] ps = new Object[psCount];
                //注入方法参数
                for(int i = 0;i<psCount;i++){
                    String type = parameters[i].getType().getName();
                    try {
                        String cmd = cmds[i+1];
                        if(cmd.indexOf(":")!=-1) {
                            String[] cvs = cmd.split(":");
                            if (parameters[i].getName().toLowerCase().equals(cvs[0].toLowerCase())) {
                                cmd = cvs[1];
                            } else {
                                continue;
                            }
                        }
                        if (type.equals("int") || type.equals("java.lang.Integer")) {
                            ps[i] = Integer.parseInt(cmd);
                        } else if (type.equals("long") || type.equals("java.lang.Long")) {
                            ps[i] = Long.parseLong(cmd);
                        } else if (type.equals("float") || type.equals("java.lang.Float")) {
                            ps[i] = Float.parseFloat(cmd);
                        } else if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
                            ps[i] = Boolean.parseBoolean(cmd);
                        } else {
                            ps[i] = cmd;
                        }
                    }catch (Exception ex){
                        ps[i] = initNormalValue(type);
                    }
                }
                for(String cmd:cmds){
                    if(cmd.indexOf(":")!=-1){
                        String[] cvs = cmd.split(":");
                        setMethodParameter(parameters,ps,cvs[0],cvs[1]);
                    }
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
                    if(fd.getName().equals("reader")){
                        fd.setAccessible(true);
                        fd.set(bean.getObject(),reader);
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
                            print(Ansi.ansi().fgRed().a(method + "参数不完整，参考：" + method).reset());
                            for(int i = 0;i<parameters.length;i++){
                                Parameter p = parameters[i];
                                print(" ");
                                print(Ansi.ansi().bgGreen().fgBlack().a("<"+p.getName()+">").reset());
                            }
                            print("\n");
                        }
                    }else{
                        //System.err.println("未登录状态，使用login指令进行登录：login <account> <password>");
                        System.out.print(Ansi.ansi(50).fgRed().a("未登录状态，使用login指令进行登录："));
                        System.out.print(Ansi.ansi().fgYellow().a("login ").reset());
                        System.out.print(Ansi.ansi().bgGreen().fgBlack().a("<account>").reset().a(" "));
                        System.out.println(Ansi.ansi().bgGreen().fgBlack().a("<password>").reset());
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
                        print(Ansi.ansi().fgRed().a(method + "参数不完整，参考：" + method).reset());
                        for(int i = 0;i<parameters.length;i++){
                            Parameter p = parameters[i];
                            print(" ");
                            print(Ansi.ansi().bgGreen().fgBlack().a("<"+p.getName()+">").reset());
                        }
                        print("\n");
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }else if(!method.equals("exit")){
            System.err.println("Not Found Command for '"+method+"'.");
        }
    }
    private void setMethodParameter(Parameter[] pms,Object[] ps,String pname,String pvalue){
        for(int i=0;i<ps.length;i++){
            String name = pms[i].getName().toLowerCase();
            if(name.equals(pname.toLowerCase())){
                String type = pms[i].getType().getName();
                if (type.equals("int") || type.equals("java.lang.Integer")) {
                    ps[i] = Integer.parseInt(pvalue);
                } else if (type.equals("long") || type.equals("java.lang.Long")) {
                    ps[i] = Long.parseLong(pvalue);
                } else if (type.equals("float") || type.equals("java.lang.Float")) {
                    ps[i] = Float.parseFloat(pvalue);
                } else if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
                    ps[i] = Boolean.parseBoolean(pvalue);
                } else {
                    ps[i] =pvalue;
                }
            }
        }
    }
    private Object initNormalValue(String type){
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
    public void hideCursor(){
        System.out.print("\u001B[?25l");
        cursor = true;
    }
    public void showCursor(){
        System.out.print("\u001B[?25h"); //显示光标
        cursor=false;
    }
    public boolean isHideCursor(){
        return cursor;
    }
}
