package com.xjj.tools.bigdata.tunnel.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by admin on 2018/9/19.
 */
public class ExecutorBean {
    private Object object;
    private List<Field> autoSetFields;
    private Method method;
    private CliMethod cliMethod;
    private CheckSession checkSession;
    public CliMethod getCliMethod() {
        return cliMethod;
    }

    public CheckSession getCheckSession() {
        return checkSession;
    }

    public void setCheckSession(CheckSession checkSession) {
        this.checkSession = checkSession;
    }

    public void setCliMethod(CliMethod cliMethod) {
        this.cliMethod = cliMethod;
    }


    public List<Field> getAutoSetFields() {
        return autoSetFields;
    }

    public void setAutoSetFields(List<Field> autoSetFields) {
        this.autoSetFields = autoSetFields;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
