package com.xjj.tools.bigdata.tunnel.utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConsoleTable {
    private static Font font = new Font("宋体", Font.PLAIN, 16);
    private List<List> rows = new ArrayList<>();

    private int colum;

    private int[] columLen;

    private static int margin =2;
    private static int maxLen = 50;
    private boolean printHeader = false;
    private int maxRowCount = GlobalValue.printMaxRow;
    public ConsoleTable(int colum, boolean printHeader) {
        this.printHeader = printHeader;
        this.colum = colum;
        this.columLen = new int[colum];
    }
    public ConsoleTable(int colum, boolean printHeader,int maxRowCount) {
        this(colum,printHeader);
        this.maxRowCount = maxRowCount;
    }
    public ConsoleTable(String column,int maxRowCount){
        String[] cs = column.split(";");
        this.printHeader= false;
        this.colum = cs.length;
        this.columLen = new int[this.colum];
        this.maxRowCount = maxRowCount;
        this.appendRow();
        for(String c:cs){
            this.appendColum(c);
        }
    }
    public void appendRow(Object[] rows){
        this.appendRow();
        for(Object r:rows){
            this.appendColum(r);
        }
    }
    public void appendRow(String rows){
        String[] rs = rows.split(";");
        appendRow(rs);
    }
    public void setMaxRowCount(int limit){
        this.maxRowCount = limit;
    }

    public void appendRow() {
        List row = new ArrayList(colum);
        rows.add(row);
    }

    public ConsoleTable appendColum(Object value) {
        if (value == null) {
            value = "NULL";
        }
        List row = rows.get(rows.size() - 1);
        row.add(value);
        //int len = value.toString().getBytes().length;
        int len = getStringLen(value.toString());
        len = len>maxLen?maxLen:len;
        if (columLen[row.size() - 1] < len)
            columLen[row.size() - 1] = len;
        return this;
    }
    public static Integer getStrPixelsLenth(String str){
        return sun.font.FontDesignMetrics.getMetrics(font).stringWidth(str) / 8;
    }
    private int getStringLen(String str){
        try {
            String a = new String(str.getBytes("GBK"), "iso-8859-1");
            return a.length();
        }catch (Exception ex){
            return str.length();
        }
    }
    private String substring(String str,int count){
        try {
            String a = new String(str.getBytes("GBK"), "iso-8859-1");
            a=a.substring(0,count);
            return new String(a.getBytes("iso-8859-1"),"GBK");
        }catch(Exception ex){
            return "";
        }
    }
    public String toString() {
        StringBuilder buf = new StringBuilder();
        boolean more= false;
        int sumlen = 0;
        for (int len : columLen) {
            sumlen += len;
        }
        if (printHeader)
            buf.append("+").append(printChar('=', sumlen + margin * 2 * colum + (colum - 1))).append("+\n");
        else
            buf.append("+").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("+\n");
        for (int ii = 0; ii < rows.size(); ii++) {
            List row = rows.get(ii);
            for (int i = 0; i < colum; i++) {
                String o = "";
                if (i < row.size()) {
                    o = row.get(i).toString();
                    if(getStringLen(o)>columLen[i]){
                        //o = o.substring(0,columLen[i]-6)+"...";
                        o = substring(o,columLen[i]-3)+"...";
                    }
                }
                if(i==0)
                    buf.append('|').append(printChar(' ', margin)).append(o);
                else
                    buf.append('|').append(printChar(' ', margin)).append(o);
                //buf.append(printChar(' ', columLen[i] - o.getBytes().length + margin));
                buf.append(printChar(' ', columLen[i] -getStringLen(o) + margin));
            }
            buf.append("|\n");
            //if (printHeader && ii == 0)
            //    buf.append("│").append(printChar('=', sumlen + margin * 2 * colum + (colum - 1))).append("│\n");
            //else
            //    buf.append("│").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("│\n");
            if(ii==0)
                buf.append("+").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("+\n");
            if(ii>maxRowCount&&maxRowCount!=-1){
                more = true;
                buf.append("+").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("+\n");
                for (int i = 0; i < colum; i++) {
                    String o = "...";

                    if(i==0)
                        buf.append('|').append(printChar(' ', margin)).append(o);
                    else
                        buf.append('|').append(printChar(' ', margin)).append(o);
                    //buf.append(printChar(' ', columLen[i] - o.getBytes().length + margin));
                    buf.append(printChar(' ', columLen[i] -getStringLen(o) + margin));
                }
                buf.append("|\n");
                break;
            }
        }
        buf.append("+").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("+\n");
        if(more){
            buf.append("输出"+maxRowCount+"条记录,共"+rows.size()+"条记录\n");
        }
        return buf.toString();
    }

    private String printChar(char c, int len) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buf.append(c);
        }
        return buf.toString();
    }
}
