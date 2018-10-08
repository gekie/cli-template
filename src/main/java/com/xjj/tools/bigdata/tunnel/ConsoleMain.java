package com.xjj.tools.bigdata.tunnel;import com.xjj.tools.bigdata.tunnel.utils.CommandUtils;import com.xjj.tools.bigdata.tunnel.utils.Config;import org.fusesource.jansi.Ansi;import org.fusesource.jansi.AnsiConsole;/** * Created by admin on 2018/9/19. */public class ConsoleMain {    public static void main(String[] args) throws Exception{        AnsiConsole.systemInstall();        printWellcome();        CommandUtils.getInstance().initCommandClass();        String account = Config.getInstance().getString("account");        String password = Config.getInstance().getString("password");        if(account!=null&&account.trim().length()>0){            CommandUtils.getInstance().callMethod("login", new Object[]{account, password});            //CommandUtils.getInstance().callMethod("list",null);        }        CommandUtils.getInstance().listenInput(args);        System.out.println("exit Console.");    }    private static void setFgColor(Ansi.Color color){        System.out.print(Ansi.ansi().eraseLine().fg(color).a(""));    }    private static void resetColor(){        System.out.print(Ansi.ansi().reset().a(""));    }    private static void printWellcome(){		setFgColor(Ansi.Color.YELLOW);        System.out.println("=======================================");        System.out.println("*                                     *");        System.out.println("*       Xjj BigData Shell Tools       *");        System.out.println("*                                     *");        System.out.println("=======================================");        setFgColor(Ansi.Color.GREEN);        System.out.println("Version:1.0.1");        System.out.println("end_point："+Config.getInstance().getString("end_point"));        System.out.println("login_url："+Config.getInstance().getString("login_url"));        resetColor();        System.out.println("@Copyright 2018 Xjjrj Cloud Computing Co., Ltd. All rights reserved.");    }}