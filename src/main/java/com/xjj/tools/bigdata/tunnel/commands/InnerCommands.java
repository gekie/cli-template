package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.*;
import org.fusesource.jansi.Ansi;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
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
    private LineReader reader;

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
                    list();
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
            ConsoleTable ct = new ConsoleTable("列项目;列值",-1);
            ct.appendRow(new Object[]{"仓库标识",table.getString("base:appid")});
            ct.appendRow(new Object[]{"创建时间",Func.format(table.getLong("base:createtime") * 1000)});
            ct.appendRow(new Object[]{"仓库名称",table.getString("base:name")});
            ct.appendRow(new Object[]{"实时处理",table.getString("base:onlineprovider")});
            ct.appendRow(new Object[]{"数据提供者",table.getString("base:provider")});
            if(table.has("base:updatetime"))
                ct.appendRow(new Object[]{"最近同步时间",Func.format(table.getLong("base:updatetime") * 1000)});
            if(table.has("base:rowcount"))
                ct.appendRow(new Object[]{"记录数",table.getString("base:rowcount")});
            if(table.has("size"))
                ct.appendRow(new Object[]{"仓库大小",Func.getFileSize(table.getLong("size"))});
            ct.appendRow(new Object[]{"SecretKey",table.getString("rowKey")});
            yellow(ct.toString());
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
            if(show&&items.length()==0){
                yellow("\t数据仓库为空，您可以使用CREATE TABLE指令创建仓库");
            }
            items = sortJsonArray(items,"base:updatetime",true);
            ConsoleTable table = new ConsoleTable(6,false,-1);
            table.appendRow();
            table.appendColum("*");
            table.appendColum("AppId");
            table.appendColum("仓库名");
            table.appendColum("仓库大小");
            table.appendColum("创建时间");
            table.appendColum("最近更新");
            for(int i = 0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                tables.put(item.getString("base:appid"),item);
                if(show) {
                    table.appendRow();
                    table.appendColum((i + 1));
                    table.appendColum(item.getString("base:appid"));
                    table.appendColum(item.getString("base:name"));
                    table.appendColum(Func.getFileSize(item.getLong("size")));
                    table.appendColum(Func.format(item.getLong("base:createtime")*1000));
                    table.appendColum(Func.format(item.getLong("base:updatetime")*1000));
                }
            }
            if(show){
                yellow(table.toString());
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
            /*
            println("command history clear");
            reader.getHistory()
            FileHistory history = (FileHistory)reader.getHistory();
            history.clear();
            history.flush();
            */

            //reader.runMacro(reader.getHistory().get(1));
            History history = reader.getHistory();
            File file = new File(GlobalValue.COMMAND_HISTORY_FILE);
            if(file.exists()){
                FileWriter fileWriter =new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
            history.load();
            println("command history clear");
        }
        try {
            FileReader m = new FileReader(new File(GlobalValue.COMMAND_HISTORY_FILE));
            BufferedReader bf = new BufferedReader(m);
            String line = bf.readLine();
            int i = 1;
            while(line!=null){
                String[] ls = line.split(":");
                yellow(i+"  "+ls[1]);
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
        table.appendRow("配置项;配置值");
        table.appendRow(new Object[]{"api_end_point",GlobalValue.endPoint});
        table.appendRow(new Object[]{"login_url",Config.getInstance().getString("login_url")});
        table.appendRow(new Object[]{"shell_commands_package",Config.getInstance().getString("shell_commands_package")});
        table.appendRow(new Object[]{"print_max_row",Config.getInstance().getInteger("print_max_row")});
        table.appendRow(new Object[]{"command_history_max_size",Config.getInstance().getInteger("command_history_max_size")});
        table.appendRow(new Object[]{"print_max_row",GlobalValue.printMaxRow});
        table.appendRow(new Object[]{"command_history_file",GlobalValue.COMMAND_HISTORY_FILE});
        table.appendRow(new Object[]{"current_token_table_api",GlobalValue.MY_TABLE_API});
        table.appendRow(new Object[]{"execute_sql_api",GlobalValue.EXECUTE_SQL_API});
        table.appendRow(new Object[]{"query_sql_api",GlobalValue.QUERY_SQL_API});
        table.appendRow(new Object[]{"create_repository_api",GlobalValue.Create_Repository_API});
        table.appendRow(new Object[]{"drop_repository_api",GlobalValue.Drop_Repository_API});
        table.appendRow(new Object[]{"csv_data_upload_api",GlobalValue.DATA_UPLOAD_API});
        table.appendRow(new Object[]{"show_upload_log_api",GlobalValue.SHOW_UPLOAD_LOG});
        table.appendRow(new Object[]{"export_csv_api",GlobalValue.exportCSV_API});

        yellow(table.toString());
        return true;
    }
    @CliMethod(description = "清屏操作",checkSession = false,calcRequestTime = false)
    public boolean clean()throws IOException{
        return true;
    }
    @CliMethod(description = "测试数数",checkSession = false)
    public boolean calc(int max,int sleep) throws InterruptedException{

        for(int i=0;i<max;i++){
            print(i,1, Ansi.Color.BLUE, Ansi.Color.WHITE);
            print("/");
            print(max, Ansi.Color.GREEN);
            Thread.sleep(sleep);
        }
        reader.printAbove("");
        return true;
    }
}