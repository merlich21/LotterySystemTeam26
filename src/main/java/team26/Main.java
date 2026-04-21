package team26;

import team26.config.database.database.DatabaseConfig;

import java.sql.Connection;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.print("Hello and welcome!");
        System.out.println(System.getProperty("java.version"));

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }

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