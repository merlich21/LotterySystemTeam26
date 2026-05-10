package ru.mephi.team26.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import ru.mephi.team26.entity.TicketStatus;

public class SerializationConfig {

    public static void init(JavalinConfig config) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        config.jsonMapper(new JavalinJackson(mapper, false));
        config.validation.register(TicketStatus.class, TicketStatus::valueOf);
    }
}
