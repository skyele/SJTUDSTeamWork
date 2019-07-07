package com.spark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;

public class ConnectionPool {
    private static LinkedList<Connection> connectionQueue;

    /**
     * 加载驱动
     */
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接，多线程访问并发控制
     * @return
     */
    public synchronized static Connection getConnection() {
        try {
            if(connectionQueue == null) {
                connectionQueue = new LinkedList<Connection>();
                for(int i = 0; i < 10; i++) {
                    Connection conn = DriverManager.getConnection(
                            "jdbc:mysql://mysql:5601/test",
                            "root",
                            "123456");
                    connectionQueue.push(conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectionQueue.poll();
    }

    /**
     * 还回去一个连接
     */
    public static void returnConnection(Connection conn) {
        connectionQueue.push(conn);
    }
}
