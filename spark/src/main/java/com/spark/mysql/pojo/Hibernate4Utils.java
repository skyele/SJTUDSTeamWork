package com.spark.mysql.pojo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Objects;

public class Hibernate4Utils {

    public static final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    /**
     * 获取当前线程安全Session
     */
    public static Session getCurrentSession() {
        Session session = sessionFactory.getCurrentSession();
        if (Objects.isNull(session)) {
            session = sessionFactory.openSession();
        }
        return session;
    }

    /**
     * 关闭Session
     */
    public static void closeCurrentSession() {
        Session session = sessionFactory.getCurrentSession();
        if (Objects.nonNull(session)) {
            session.close();
        }
    }
}
