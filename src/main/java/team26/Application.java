package team26;


import team26.config.database.DatabaseConfig;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;
import team26.domain.user.User;
import team26.domain.user.UserRole;
import team26.repository.lotteryDraw.JdbcLotteryDrawRepository;
import team26.repository.user.JdbcUserRepository;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

            JdbcUserRepository userRepository = new JdbcUserRepository();
            JdbcLotteryDrawRepository lotteryDrawRepository = new JdbcLotteryDrawRepository();

            lotteryDrawRepository.updateStatus(UUID.fromString("493216d1-0462-43a7-afc4-b7d3a27dfdbc"), LotteryDrawStatus.ACTIVE);

//            System.out.println(lotteryDraw.size());
//            Optional<User> user = userRepository.findByLogin("testLogin1");
//            if (user.isPresent()) {
//                User resUser = user.get();
//            }
//                List<User> list = userRepository.findAllByRole(UserRole.USER);
//
//                System.out.println("User is exists by " + "phone" + " " + "null" + ": " + list);
//            resUser.setName("Tima");
//            User res = userRepository.save(new User("testName3", "testSUrname3", "testLogin3", "testEmail3@gmail.com", null, "hashedPass"));


        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}