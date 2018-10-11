package com.xjj.tools.bigdata.tunnel;

import com.xjj.tools.bigdata.tunnel.commands.BaseCommand;
import org.fusesource.jansi.Ansi;

public class Test extends BaseCommand {
    public static void main(String[] args) throws Exception{
        Test test = new Test();
        long max = 100;
        for(int num=1;num<max;num++) {
            test.print("[", 1);
            String p = Integer.toString(Math.round((num / (float) max) * 100)) + "%";
            test.print(" "+p+" ", 2, Ansi.Color.GREEN, Ansi.Color.YELLOW);
            test.print("/");
            test.print("100%", Ansi.Color.YELLOW);
            test.print(num+" byte",20);
            test.print("/");
            test.print(max+" byte", Ansi.Color.YELLOW);
            test.print("]");

            Thread.sleep(100);
        }

    }
}
