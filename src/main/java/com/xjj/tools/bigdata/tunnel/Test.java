package com.xjj.tools.bigdata.tunnel;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class Test {
    public static void main(String[] args) throws Exception{
        AnsiConsole.systemInstall();

        int i=1;
        while(true){
            System.out.print(Ansi.ansi(20).bold().cursorToColumn(10).fgYellow().a(i).fgRed().a("/100"));
            if(i>=100){
                break;
            }
            i++;
            Thread.sleep(1000);
        }
    }
}
