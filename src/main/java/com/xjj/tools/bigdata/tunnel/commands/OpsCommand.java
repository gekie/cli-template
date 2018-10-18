package com.xjj.tools.bigdata.tunnel.commands;

import com.xjj.tools.bigdata.tunnel.utils.ConsoleTable;
import com.xjj.tools.bigdata.tunnel.utils.Func;
import com.xjj.tools.bigdata.tunnel.utils.GlobalValue;
import com.xjj.tools.bigdata.tunnel.utils.RESTfulAgent;
import org.json.JSONArray;
import org.json.JSONObject;

@CliCompent
public class OpsCommand extends BaseCommand {
    @CliMethod(group = "show",description = "查看接入的主机列表",calcRequestTime = false)
    public boolean host(String parent,String ip,String port){
        if(!Func.isEmpty(ip)){
            if(Func.isEmpty(port)){
                return false;
            }
        }
        String api = GlobalValue.endPoint+"server/allhosts";
        JSONObject result = RESTfulAgent.getInstance().getObject(api);
        JSONObject bi = RESTfulAgent.getInstance().getObject(GlobalValue.endPoint+"server/loadAllHostBaseInfo?cache=true");
        JSONObject bitems = null;
        if(bi.getInt("errorCode")==0)
            bitems = bi.getJSONObject("items");
        else
            bitems = new JSONObject();
        if(result.getInt("errorCode")==0){
            JSONArray hosts = result.getJSONArray("hosts");
            if(!Func.isEmpty(ip)&&!Func.isEmpty(port)){
                printHost(hosts,bitems,ip,port);
            }else{
                ConsoleTable table = new ConsoleTable(8, false, -1);
                table.appendRow("Name;Host;WebServer;CPU;内存;磁盘;Oracle;负载");
                for (int i = 0; i < hosts.length(); i++) {
                    JSONObject host = hosts.getJSONObject(i);
                    String hostid = host.getString("id");
                    JSONObject hi = null;
                    if (bitems.has(hostid))
                        hi = bitems.getJSONObject(hostid);
                    table.appendRow();
                    table.appendColum(host.get("name"));
                    table.appendColum(host.get("ip") + ":" + host.get("webPort"));
                    table.appendColum(host.get("webServer"));
                    if (hi != null) {
                        if (hi.has("cpu")) {
                            JSONObject cpu = hi.getJSONObject("cpu");
                            if (cpu.has("percent"))
                                table.appendColum(cpu.get("percent") + "%[" + cpu.get("count") + "核]");
                            else
                                table.appendColum("");
                        } else
                            table.appendColum("");
                        if (hi.has("mem")) {
                            JSONObject mem = hi.getJSONObject("mem");
                            if (mem.has("mem")) {
                                mem = mem.getJSONObject("mem");
                                table.appendColum(mem.get("percent") + "%[" + Func.getFileSize(mem.getLong("total")) + "]");
                            } else {
                                table.appendColum("");
                            }
                        } else {
                            table.appendColum("");
                        }
                        if (hi.has("disk") && hi.getJSONObject("disk").has("disk")) {
                            JSONArray disks = hi.getJSONObject("disk").getJSONArray("disk");
                            String buf = "";
                            for (int k = 0; k < disks.length(); k++) {
                                String md = disks.getJSONObject(k).getString("mountpoint");
                                if (!"/boot".equals(md))
                                    buf += md + ":" + disks.getJSONObject(k).getJSONObject("usage").get("percent") + "% ";
                            }
                            table.appendColum(buf);
                        } else {
                            table.appendColum("");
                        }
                        if (hi.has("thread")) {
                            JSONObject thread = hi.getJSONObject("thread");
                            if (thread.has("average")) {
                                table.appendColum(thread.getInt("oracle"));
                                table.appendColum(thread.get("average").toString());
                            } else {
                                table.appendColum("");
                                table.appendColum("");
                            }
                        } else {
                            table.appendColum("");
                            table.appendColum("");
                        }
                    }
                }
                yellow(table.toString());
            }
        }else{
            red(result.getString("message"));
        }
        return true;
    }

    private void printHost(JSONArray hosts,JSONObject bi,String ip,String port){
        String hostid = null;
        JSONObject host = null;
        int _port = Integer.parseInt(port);
        for(int i=0;i<hosts.length();i++){
            host = hosts.getJSONObject(i);
            if(host.getString("ip").equals(ip)&&host.getInt("webPort")==_port){
                hostid = host.getString("id");
                break;
            }
        }
        if(hostid!=null) {
            if (bi.has(hostid)) {
                ConsoleTable table=new ConsoleTable(2,false,-1);
                table.appendRow("主机项目;项目值");
                table.appendRow(new Object[]{"主机名称",host.getString("name")});
                table.appendRow(new Object[]{"Web服务",host.getString("webServer")});
                table.appendRow(new Object[]{"所属单位标识",host.getString("unitId")});
                table.appendRow(new Object[]{"------------","------------------------"});
                JSONObject hi = bi.getJSONObject(hostid);

                if (hi.has("cpu")) {
                    JSONObject cpu = hi.getJSONObject("cpu");
                    if (cpu.has("percent"))
                        table.appendRow(new Object[]{"CPU",cpu.get("percent") + "%[" + cpu.get("count") + "核]"});
                }
                if (hi.has("mem")) {
                    JSONObject mem = hi.getJSONObject("mem");
                    if (mem.has("mem")) {
                        mem = mem.getJSONObject("mem");
                        table.appendRow(new Object[]{"内存",mem.get("percent") + "%[" + Func.getFileSize(mem.getLong("total")) + "]"});
                    }
                }
                if (hi.has("disk") && hi.getJSONObject("disk").has("disk")) {
                    JSONArray disks = hi.getJSONObject("disk").getJSONArray("disk");
                    String buf = "";
                    for (int k = 0; k < disks.length(); k++) {
                        String md = disks.getJSONObject(k).getString("mountpoint");
                        if (!"/boot".equals(md))
                            buf += md + ":" + disks.getJSONObject(k).getJSONObject("usage").get("percent") + "% ";
                    }
                    table.appendRow(new Object[]{"磁盘",buf});
                }
                if (hi.has("thread")) {
                    JSONObject thread = hi.getJSONObject("thread");
                    if(thread.has("conf")){
                        table.appendRow(new Object[]{"字符集",thread.getJSONObject("conf").getString("URIEncoding")});
                        table.appendRow(new Object[]{"请求超时时间",thread.getJSONObject("conf").getString("connectionTimeout")});
                        table.appendRow(new Object[]{"最大进程数",thread.getJSONObject("conf").getString("maxThreads")});
                    }
                    if (thread.has("average")) {
                        table.appendRow(new Object[]{"Oralce并发",thread.getInt("oracle")});
                        table.appendRow(new Object[]{"负载",thread.get("average").toString()});
                    }
                    if(thread.has("webEstab"))
                        table.appendRow(new Object[]{"Web并发",thread.get("webEstab")});
                    if(thread.has("webProcess")){
                        JSONObject web = thread.getJSONObject("webProcess");
                        table.appendRow(new Object[]{"------------","------------------------"});
                        table.appendRow(new Object[]{"Web启动时间",web.get("start")});
                        table.appendRow(new Object[]{"Web启动用户",web.get("user")});
                        table.appendRow(new Object[]{"Web进程状态",web.get("stat")});
                        table.appendRow(new Object[]{"Web占用CPU",web.get("cpu")+"%"});
                        table.appendRow(new Object[]{"Web占用内存",web.get("mem")+"%"});
                        table.appendRow(new Object[]{"Web运行累计",web.get("time")});
                    }

                }
                yellow(table.toString());
            } else {
                red("没有主机:"+ip+",端口:"+port+"的监控信息");
            }
        }else{
            red("主机:"+ip+",端口:"+port+"没有接入");
        }
    }
}
