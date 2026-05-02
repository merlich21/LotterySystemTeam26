package team26.util.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import team26.config.database.DatabaseConfig;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDrawResult.LotteryDrawResult;
import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.user.User;

import java.util.Properties;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            // 🔥 Регистрируем ВСЕ entity
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(LotteryTicket.class);
            configuration.addAnnotatedClass(LotteryDraw.class);
            configuration.addAnnotatedClass(LotteryDrawResult.class);

            Properties settings = new Properties();
            settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            settings.put("hibernate.show_sql", "true");
            settings.put("hibernate.format_sql", "true");
            settings.put("hibernate.hbm2ddl.auto", "validate");

            settings.put("hibernate.connection.datasource", DatabaseConfig.getDataSource());

            configuration.setProperties(settings);

            return configuration.buildSessionFactory();

        } catch (Exception e) {
            throw new ExceptionInInitializerError("SessionFactory creation failed " + e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}