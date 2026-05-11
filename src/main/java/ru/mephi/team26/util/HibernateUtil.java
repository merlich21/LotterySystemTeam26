package ru.mephi.team26.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.mephi.team26.config.HibernateConfig;

import java.util.function.Function;

public class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = HibernateConfig.buildSessionFactory();

    public static <T> T inTransaction(Function<Session, T> function) {
        try (Session session = SESSION_FACTORY.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = function.apply(session);
                tx.commit();
                return result;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }
}
