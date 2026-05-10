package ru.mephi.team26.util;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.mephi.team26.config.HibernateConfig;

import java.util.function.Function;

public class HibernateUtil {

    public static <T> T inTransaction(Function<Session, T> function) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
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
