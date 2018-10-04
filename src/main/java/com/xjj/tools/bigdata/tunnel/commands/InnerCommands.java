package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;
import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2018/9/19.
 */
@CliCompent
public class InnerCommands extends BaseCommand{
    @AutoSetValue
    private String inputLine;
    @AutoSetValue
    private String userName;
    @AutoSetValue
    private ConsoleReader reader;

    @CliMethod(description = "登录大数据平台",checkSession = false)
    public boolean login(String account,String password){
        if(Func.isEmpty(account)&&Func.isEmpty(password)){
            //err("指令格式不正确，参考：login <account> <password>");
            return false;
        }
        String checkTokenAction = Config.getInstance().getString("login_url");
        if(checkTokenAction!=null&&checkTokenAction.trim().length()>0){
            JSONObject pm = new JSONObject();
            pm.put("appid","16101_10");
            pm.put("grantType","client_credential");
            pm.put("loginFlag","mobile");
            pm.put("password",password);
            pm.put("username",account);
            try {
                String result = RESTfulAgent.getInstance().doPost(checkTokenAction, pm.toString());
                JSONObject obj = new JSONObject(result);
                int code = Integer.parseInt(obj.getString("code"));
                if(code==0){
                    JSONObject data = obj.getJSONObject("data");
                    GlobalValue.ticket = data.getString("accessToken");
                    JSONObject _data = new JSONObject(data.getString("data"));
                    GlobalValue.userName = _data.getString("fullname");
                    GlobalValue.account = account;
                    green("登录成功!");
                }else{
                    err(obj.getString("msg"));
                }
            }catch(DoPostException ex){
                err(ex.getMessage());
            }
        }else{
            err("您没有配置登录信息，可以使用login指令登录");
        }
        return true;
    }

    @CliMethod(description = "退出登录")
    public boolean logout(){
        GlobalValue.reset();
        println("退出登录成功", Ansi.Color.GREEN);
        return true;
    }

    @CliMethod(show = false,checkSession = false)
    public boolean checkSession(){
        return GlobalValue.isLogin();
    }

    @CliMethod(description = "查看所属仓库列表")
    public boolean list(){
        _list(true);
        return true;
    }

    @CliMethod(description = "切换数据仓库")
    public boolean use(String appid){
        if(Func.isEmpty(appid)){
            //err("参数丢失，参考：use <appid>");
            return false;
        }
        if(GlobalValue.tables==null){
            _list(false);
        }
        JSONObject table = GlobalValue.tables.get(appid);
        if(table==null){
            err(appid+"不存在");
        }else{
            GlobalValue.appid = appid;
            GlobalValue.dbSkey = table.getString("rowKey");
            yellow("已切换到"+table.getString("base:name")+"仓库");
        }
        return true;
    }

    @CliMethod(description = "查看当前仓库记录数，或者查看指定--appid的记录数")
    public boolean count(String appid){
        if(Func.isEmpty(appid)){
            appid = GlobalValue.appid;
        }
        if(Func.isEmpty(appid)){
            return false;
        }
        if(GlobalValue.tables==null){
            _list(false);
        }
        JSONObject table = GlobalValue.tables.get(appid);
        if (table == null) {
            err(appid + "不存在");
        } else {
            yellow(table.getString("base:counter"));
        }
        return true;
    }

    @CliMethod(description = "查看仓库细节")
    public boolean detail(String appid){
        if(Func.isEmpty(appid)){
            appid = GlobalValue.appid;
        }
        if(Func.isEmpty(appid)){
            return false;
        }
        if(GlobalValue.tables==null){
            _list(false);
        }
        JSONObject table = GlobalValue.tables.get(appid);
        if (table == null) {
            err(appid + "不存在");
        } else {
            String buf = "";
            buf+="仓库标识:"+table.getString("base:appid")+"\r\n";
            buf+="创建时间:"+Func.format(table.getLong("base:createtime")*1000)+"\r\n";
            buf+="仓库名称:"+table.getString("base:name")+"\r\n";
            buf+="实时处理:"+table.getString("base:onlineprovider")+"\r\n";
            buf+="数据提供者:"+table.getString("base:provider")+"\r\n";
            buf+="最近同步时间:"+Func.format(table.getLong("base:updatetime")*1000)+"\r\n";
            buf+="记录数:"+table.getString("base:rowcount")+"\r\n";
            buf+="仓库大小:"+Func.getFileSize(table.getLong("size"))+"\r\n";
            buf+="SecretKey:"+table.getString("rowKey")+"\r\n";
            print(buf);
        }
        return true;
    }

    @CliMethod(description = "显示指令帮助列表",checkSession = false,calcRequestTime = false)
    public boolean help(){
        yellow("使用使用 Xjj BigData Shell Tools，以下是支持的指令列表及使用参数：");
        println("---------------------");
        Map<String,ExecutorBean> maps = CommandUtils.getInstance().getCommandMaps();
        for(Map.Entry<String, ExecutorBean> entry: maps.entrySet()){
            ExecutorBean bean = entry.getValue();
            if(bean.getCliMethod().show()) {
                if(!Func.isEmpty(bean.getCliMethod().group())){
                    print(bean.getCliMethod().group()+" ",Ansi.Color.CYAN);
                }
                print(entry.getKey(), Ansi.Color.CYAN);
                Parameter[] ps = bean.getMethod().getParameters();
                for (int i = 0; i < ps.length; i++) {
                    print(" "+ps[i].getName(), Ansi.Color.YELLOW);
                    print(":");
                    print("<" + ps[i].getName() + ">", Ansi.Color.GREEN);
                }
                println("\r\n\t" + bean.getCliMethod().description());
            }
        }
        return true;
    }

    private void _list(boolean show){
        JSONObject obj = RESTfulAgent.getInstance().getObject(GlobalValue.MY_TABLE_API);
        int errorCode = obj.getInt("errorCode");
        if(errorCode==0){
            JSONArray items = obj.getJSONArray("items");
            HashMap<String,JSONObject> tables = new HashMap<>();
            if(show)
                println("========您的仓库列表=============");
            if(show&&items.length()==0){
                yellow("\t数据仓库为空，您可以使用CREATE TABLE指令创建仓库");
            }
            for(int i = 0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                tables.put(item.getString("base:appid"),item);
                if(show) {
                    print((i + 1) + ".");
                    if (i % 2 == 0)
                        green(item.getString("base:name") + " <" + item.getString("base:appid") + ">");
                    else
                        yellow(item.getString("base:name") + " <" + item.getString("base:appid") + ">");
                }
            }
            GlobalValue.tables=tables;
        }else{
            err(obj.getString("message"));
        }
    }
    @CliMethod(description = "测试使意位置传参",checkSession = false)
    public boolean test(String name,String sex,String job,String hobby,String mobile) {
        yellow("=================================");
        yellow("*                               *");
        yellow("*    测试使用随意位置传参          *");
        yellow("*                               *");
        yellow("=================================");
        yellow("name:"+name);
        yellow("sex:"+sex);
        yellow("job:"+job);
        yellow("hobby:"+hobby);
        yellow("mobile:"+mobile);
        return true;
    }

    @CliMethod(description = "查看或清空输入历史列表",checkSession = false,calcRequestTime = false)
    public boolean history(String clean) throws IOException{
        if(!Func.isEmpty(clean)&&clean.equals("-c")){
            println("command history clear");
            FileHistory history = (FileHistory)reader.getHistory();
            history.clear();
            history.flush();
        }
        try {
            FileReader m = new FileReader(new File(GlobalValue.COMMAND_HISTORY_FILE));
            BufferedReader bf = new BufferedReader(m);
            String line = bf.readLine();
            int i = 1;
            while(line!=null){
                yellow(i+"  "+line);
                line = bf.readLine();
                i++;
            }
        }catch (Exception ex){
            red("没有记录");
        }
        return true;
    }

    @CliMethod(description = "查看配置",checkSession = false,calcRequestTime = false)
    public boolean config(){
        ConsoleTable table = new ConsoleTable(2,false,-1);
        table.appendRow();
        table.appendColum("配置项");
        table.appendColum("配置值");
        table.appendRow();
        table.appendColum("api_end_point");
        table.appendColum(GlobalValue.endPoint);

        table.appendRow();
        table.appendColum("login_url");
        table.appendColum(Config.getInstance().getString("login_url"));

        table.appendRow();
        table.appendColum("shell_commands_package");
        table.appendColum(Config.getInstance().getString("shell_commands_package"));

        table.appendRow();
        table.appendColum("print_max_row");
        table.appendColum(Config.getInstance().getInteger("print_max_row"));

        table.appendRow();
        table.appendColum("command_history_max_size");
        table.appendColum(Config.getInstance().getInteger("command_history_max_size"));

        table.appendRow();
        table.appendColum("print_max_row");
        table.appendColum(GlobalValue.printMaxRow);

        table.appendRow();
        table.appendColum("current_token_table_api");
        table.appendColum(GlobalValue.MY_TABLE_API);

        table.appendRow();
        table.appendColum("execute_sql_api");
        table.appendColum(GlobalValue.EXECUTE_SQL_API);

        table.appendRow();
        table.appendColum("query_sql_api");
        table.appendColum(GlobalValue.QUERY_SQL_API);

        table.appendRow();
        table.appendColum("create_repository_api");
        table.appendColum(GlobalValue.Create_Repository_API);

        table.appendRow();
        table.appendColum("drop_repository_api");
        table.appendColum(GlobalValue.Drop_Repository_API);

        table.appendRow();
        table.appendColum("command_history_file");
        table.appendColum(GlobalValue.COMMAND_HISTORY_FILE);

        yellow(table.toString());
        return true;
    }
    @CliMethod(description = "清屏操作",checkSession = false,calcRequestTime = false)
    public void clean()throws IOException{
        reader.clearScreen();
    }

}