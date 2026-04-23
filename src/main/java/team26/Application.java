package team26;


import team26.config.database.DatabaseConfig;

import java.sql.Connection;

public class Application {
    public static void main(String[] args) {

        System.out.println(System.getProperty("java.version"));

        try {
            // Инициализируем базу данных и запускаем миграции
            DatabaseConfig.init();

            try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
                System.out.println("Connected to database!");
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseConfig.shutdown();
            }));

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}