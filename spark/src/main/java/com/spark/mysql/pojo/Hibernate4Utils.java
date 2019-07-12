package com.spark.mysql.pojo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Objects;

public class Hibernate4Utils {

    static {
        try {
            Configuration conf = new Configuration().configure();
            ServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(conf.getProperties()).build();
            sessionFactory = conf.buildSessionFactory(registry);
        } catch (Throwable e) {
            throw e;
        }
    }

    public static final SessionFactory sessionFactory;

    public static final ThreadLocal<Session> sessionThread = new ThreadLocal<>();

    /**
     * 获取当前线程安全Session
     */
    public static Session getCurrentSession() {
        Session session = sessionThread.get();
        if (Objects.isNull(session)) {
            session = sessionFactory.openSession();
            sessionThread.set(session);
        }
        return session;
    }

    /**
     * 关闭Session
     */
    public static void closeCurrentSession() {
        Session session = sessionThread.get();
        if (Objects.nonNull(session)) {
            session.close();
        }
        sessionThread.set(null);
    }
}
