package ru.mephi.team26.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.entity.DrawResult;
import ru.mephi.team26.entity.Ticket;
import ru.mephi.team26.entity.User;

public class HibernateConfig {
    private static final SessionFactory SESSION_FACTORY;

    static {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/lottery_db"))
                .setProperty("hibernate.connection.username", getOrDefault("DB_USER", "postgres"))
                .setProperty("hibernate.connection.password", getOrDefault("DB_PASSWORD", "postgres"))
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .addAnnotatedClass(Draw.class)
                .addAnnotatedClass(DrawResult.class)
                .addAnnotatedClass(Ticket.class)
                .addAnnotatedClass(User.class);
        SESSION_FACTORY = configuration.buildSessionFactory();
    }

    public static String getOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }
}
