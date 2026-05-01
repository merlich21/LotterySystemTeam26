package team26;


import team26.config.database.DatabaseConfig;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;
import team26.domain.lotteryDrawResult.LotteryDrawResult;
import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.user.User;
import team26.domain.user.UserRole;
import team26.repository.lotteryDraw.HibernateLotteryDrawRepository;
import team26.repository.lotteryDraw.JdbcLotteryDrawRepository;
import team26.repository.lotteryDrawResult.HibernateLotteryDrawResultRepository;
//import team26.repository.lotteryDrawResult.JdbcLotteryDrawResultRepository;
//import team26.repository.lotteryTickets.JdbcLotteryTicketsRepository;
import team26.repository.lotteryTickets.HibernateLotteryTicketsRepository;
import team26.repository.user.HibernateUserRepository;
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

//            JdbcUserRepository userRepository = new JdbcUserRepository();
//            JdbcLotteryDrawRepository lotteryDrawRepository = new JdbcLotteryDrawRepository();
            HibernateUserRepository hibernateUserRepository = new HibernateUserRepository();
            HibernateLotteryDrawRepository hibernateLotteryDrawRepository = new HibernateLotteryDrawRepository();
            HibernateLotteryTicketsRepository hibernateLotteryTicketsRepository = new HibernateLotteryTicketsRepository();
            HibernateLotteryDrawResultRepository hibernateLotteryDrawResultRepository = new HibernateLotteryDrawResultRepository();

//            User user = new User("Qwer", "wefwefwefvwf");
//            hibernateUserRepository.save(user);
//
//            LotteryDraw lotteryDraw = new LotteryDraw("Hello");
//            hibernateLotteryDrawRepository.save(lotteryDraw);

//            Optional<User> user1 = hibernateUserRepository.findById(UUID.fromString("7944baf0-9fa7-42c1-8f0e-ba20f2c42838"));
//            Optional<LotteryDraw> lotteryDraw1 = hibernateLotteryDrawRepository.findByDrawName("Hello");
//
//            LotteryTicket lotteryTicket = new LotteryTicket(user1.get(), lotteryDraw1.get(), new Integer[]{23, 40, 39, 21, 4});
//            hibernateLotteryTicketsRepository.save(lotteryTicket);
//
//            Optional<LotteryDraw> draw = hibernateLotteryDrawRepository.findByDrawName("Hello");
//            LotteryDrawResult result = new LotteryDrawResult(draw.get(), new Integer[]{13, 21, 17, 3, 2});
//            hibernateLotteryDrawResultRepository.save(result);

//            hibernateLotteryDrawRepository.save(new LotteryDraw("Hello"));

//            System.out.println(user.get().getLogin());

//            Optional<User> user = userRepository.findByLogin("Qwer");
//            new HibernateUserRepository().save(new User("Qwer", "QWerwefwef"));
//            Optional<LotteryDraw> lotteryDraw = lotteryDrawRepository.findByDrawNumber(1);

//            Integer[] arr = new Integer[]{24, 13, 25, 17, 8};
//            LotteryTicket ticket = new JdbcLotteryTicketsRepository().save(new LotteryTicket(user.get(), lotteryDraw.get(), arr));
//
//            LotteryDrawResult result = new JdbcLotteryDrawResultRepository().save(new LotteryDrawResult(lotteryDraw.get()));

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}