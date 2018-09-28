package com.xjj.tools.bigdata.tunnel.commands;
import org.fusesource.jansi.Ansi;
/**
 * Created by cjh on 18/9/20.
 */
public class BaseCommand {
    protected void setFgColor(Ansi.Color color){
        System.out.print(Ansi.ansi().eraseLine().fg(color).a(""));
    }
    protected void resetColor(){
        System.out.print(Ansi.ansi().reset().a(""));
    }

    protected void println(Object text,Ansi.Color color){
        setFgColor(color);
        System.out.println(text);
        resetColor();
    }
    protected void print(Object text,Ansi.Color color){
        setFgColor(color);
        System.out.print(text);
        resetColor();
    }
    protected void print(Object text){
        System.out.print(text);
    }
    protected void println(Object text){
        System.out.println(text);
    }
    protected void err(Object text){
        System.err.println(text);
    }
    protected void green(Object text){
        println(text, Ansi.Color.GREEN);
    }
    protected void yellow(Object text){
        println(text, Ansi.Color.YELLOW);
    }
    protected void red(Object text){
        println(text, Ansi.Color.RED);
    }
}
