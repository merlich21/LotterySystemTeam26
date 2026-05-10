package ru.mephi.team26;

import io.javalin.Javalin;
import ru.mephi.team26.config.SerializationConfig;
import ru.mephi.team26.controller.DrawController;
import ru.mephi.team26.controller.TicketController;
import ru.mephi.team26.controller.UserController;
import ru.mephi.team26.exception.ExceptionHandler;
import ru.mephi.team26.mapper.impl.DrawMapper;
import ru.mephi.team26.mapper.impl.DrawResultMapper;
import ru.mephi.team26.mapper.impl.TicketMapper;
import ru.mephi.team26.mapper.impl.UserMapper;
import ru.mephi.team26.repository.DrawRepository;
import ru.mephi.team26.repository.DrawResultRepository;
import ru.mephi.team26.repository.TicketRepository;
import ru.mephi.team26.repository.UserRepository;
import ru.mephi.team26.security.JwtFilter;
import ru.mephi.team26.security.JwtProvider;
import ru.mephi.team26.service.DrawService;
import ru.mephi.team26.service.TicketService;
import ru.mephi.team26.service.UserService;
import ru.mephi.team26.validator.impl.DrawValidator;
import ru.mephi.team26.validator.impl.TicketValidator;
import ru.mephi.team26.validator.impl.UserValidator;

public class Application {

    public static void main(String[] args) {
        Javalin app = Javalin.create(SerializationConfig::init);

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

        TicketMapper ticketMapper = new TicketMapper();
        TicketValidator ticketValidator = new TicketValidator();
        TicketService ticketService = new TicketService(ticketRepository, userRepository, drawRepository, ticketMapper, ticketValidator);
        TicketController ticketController = new TicketController(ticketService);
        ticketController.init(app);

        DrawMapper drawMapper = new DrawMapper();
        DrawResultMapper drawResultMapper = new DrawResultMapper();
        DrawValidator drawValidator = new DrawValidator();
        DrawService drawService = new DrawService(drawRepository, drawResultRepository, drawMapper, drawResultMapper, drawValidator);
        DrawController drawController = new DrawController(drawService, ticketService);
        drawController.init(app);

        ExceptionHandler exceptionHandler = new ExceptionHandler();
        exceptionHandler.init(app);

        app.start(8080);
    }
}
