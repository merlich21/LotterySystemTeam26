package team26;


import team26.config.database.DatabaseConfig;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;
import team26.domain.lotteryDrawResult.LotteryDrawResult;
import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.user.User;
import team26.domain.user.UserRole;
import team26.repository.lotteryDraw.JdbcLotteryDrawRepository;
import team26.repository.lotteryDrawResult.JdbcLotteryDrawResultRepository;
import team26.repository.lotteryTickets.JdbcLotteryTicketsRepository;
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

            Optional<User> user = userRepository.findByLogin("Tima");
            Optional<LotteryDraw> lotteryDraw = lotteryDrawRepository.findByDrawNumber(1);
            Integer[] arr = new Integer[]{24, 13, 25, 17, 8};
            LotteryTicket ticket = new JdbcLotteryTicketsRepository().save(new LotteryTicket(user.get(), lotteryDraw.get(), arr));

            LotteryDrawResult result = new JdbcLotteryDrawResultRepository().save(new LotteryDrawResult(lotteryDraw.get()));

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}