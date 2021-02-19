package io.windfree.asm;

import java.sql.Connection;
import java.sql.SQLException;

public class TraceMain {
    public static Object start(String arg,Object object, Object object2) {
        System.out.println("injected code[pre-method]: trace main start3");
        System.out.println("injected code[pre-method] : first argument : " + arg);
        if(object != null) {
            System.out.println("injected code[pre-method]: second argument : object id : " + object.toString());
        } else {
            System.out.println("injdected code[pre-method]: second argument is null");
        }
        Object testObj = new Object();
        System.out.println("TraceMain Class Loader:" + testObj.getClass().getClassLoader().toString());
        SQLException e = null;
        Connection conn = (Connection)object;
        if(conn == null) {
            System.out.println("conn is null");
        } else {
            System.out.println("trace main : conn is not null");
        }
        return new LocalContext();
    }

    public static void end(Object arg1,Object thr)  {
        System.out.println("hello indected code end");
        if (thr == null) {
            System.out.println("no error");
        } else {
            System.out.println("error occurred");
        }
        System.out.println(arg1.toString());

    }
}

