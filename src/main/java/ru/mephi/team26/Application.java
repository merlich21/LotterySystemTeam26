package ru.mephi.team26;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import ru.mephi.team26.config.AppConfig;
import ru.mephi.team26.config.DatabaseConfig;
import ru.mephi.team26.controller.DrawController;
import ru.mephi.team26.controller.TicketController;
import ru.mephi.team26.controller.UserController;
import ru.mephi.team26.exception.ExceptionHandler;
import ru.mephi.team26.mapper.DrawMapper;
import ru.mephi.team26.mapper.DrawResultMapper;
import ru.mephi.team26.mapper.TicketMapper;
import ru.mephi.team26.mapper.UserMapper;
import ru.mephi.team26.repository.DrawRepository;
import ru.mephi.team26.repository.DrawResultRepository;
import ru.mephi.team26.repository.TicketRepository;
import ru.mephi.team26.repository.UserRepository;
import ru.mephi.team26.security.JwtFilter;
import ru.mephi.team26.security.JwtProvider;
import ru.mephi.team26.service.DrawService;
import ru.mephi.team26.service.TicketService;
import ru.mephi.team26.service.UserService;
import ru.mephi.team26.validator.DrawValidator;
import ru.mephi.team26.validator.TicketValidator;
import ru.mephi.team26.validator.UserValidator;

public class Application {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            config.jsonMapper(new JavalinJackson(mapper, false));
        });

        AppConfig config = AppConfig.fromEnv();
        DatabaseConfig dbConfig = new DatabaseConfig(config);
        //dbConfig.migrate();

        JwtProvider jwtProvider = new JwtProvider();
        JwtFilter jwtFilter = new JwtFilter(jwtProvider);
        jwtFilter.init(app);

        UserRepository userRepository = new UserRepository();
        DrawRepository drawRepository = new DrawRepository();
        DrawResultRepository drawResultRepository = new DrawResultRepository();
        TicketRepository ticketRepository = new TicketRepository();

        UserMapper userMapper = new UserMapper(jwtProvider);
        UserValidator userValidator = new UserValidator();
        UserService userService = new UserService(userRepository, userMapper, userValidator);
        userService.init();
        UserController userController = new UserController(userService);
        userController.init(app);

        DrawMapper drawMapper = new DrawMapper();
        DrawResultMapper drawResultMapper = new DrawResultMapper();
        DrawValidator drawValidator = new DrawValidator();
        DrawService drawService = new DrawService(drawRepository, drawResultRepository, drawMapper, drawResultMapper, drawValidator);
        DrawController drawController = new DrawController(drawService);
        drawController.init(app);

        TicketMapper ticketMapper = new TicketMapper();
        TicketValidator ticketValidator = new TicketValidator();
        TicketService ticketService = new TicketService(ticketRepository, drawRepository, ticketMapper, ticketValidator);
        TicketController ticketController = new TicketController(ticketService);
        ticketController.init(app);

        ExceptionHandler exceptionHandler = new ExceptionHandler();
        exceptionHandler.init(app);

        app.start(config.getPort());
    }
}
