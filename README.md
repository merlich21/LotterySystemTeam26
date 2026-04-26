# LotterySystemTeam26 - Документация проекта #
___

## Объём реализации ## 

Сценарий 1. Базовая лотерея

В системе должны быть реализованы:

- создание тиража;
- получение списка активных тиражей;
- создание билета;
- генерация выигрышной комбинации;
- проверка результата билета;
- отображение статусов билетов WIN или LOSE.

---

## 1. АРХИТЕКТУРА РЕШЕНИЯ

### 1.1 Слои приложения

```
┌─────────────────────────────────────┐
│  API Layer (Javalin)                │
│  - HTTP Routes                      │
│  - Authorization & Roles            │
│  - Request/Response DTOs            │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Service Layer                      │
│  - AuthService (auth & sessions)    │
│  - DrawService (draw lifecycle)     │
│  - TicketService (ticket management)│
│  - LotteryEngine (number generation)│
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Repository Layer (JDBC)            │
│  - JdbcUserRepository               │
│  - JdbcDrawRepository               │
│  - JdbcTicketRepository             │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Database Layer (PostgreSQL)        │
│  - users,                           │
│  - lottery_draws,                   │
│  - lottery_tickets,                 │
│  - lottery_draw_results,            │
│  - migrations (Flyway)              │
└─────────────────────────────────────┘
```


### 1.2 Технологический стек

- **Язык**: Java 17
- **Build**: Maven 4.0.0
- **REST API**: Javalin 7.2.0 (без Spring)
- **БД**: PostgreSQL 18.3
- **Пул соединений**: HikariCP
- **Миграции**: Flyway
- **ORM Framework**: Hibernate
- **Вспомогательные Frameworks**: Jakarta, Lombok
- **Логирование**: SLF4J + Logback
- **Тестирование**: JUnit 5
- **Контейнеризация**: Docker + Docker Compose

## *. Архитектура (слои)

- `api` - HTTP-роуты и валидация доступа по роли.
- `service` - бизнес-логика тиражей, билетов, аутентификации.
- `repository` - JDBC-доступ к PostgreSQL.
- `domain` - сущности и статусы.
- `config` - окружение, пул соединений, миграции.

## *. Сущности

- `User`
- `LotteryDraw`
- `LotteryTicket`
- `LotteryDrawResult`

## *. Роли

- `ADMIN`: создаёт тиражи, проводит розыгрыши
- `USER`: покупает билеты, проверяет результаты

### 1.3 Структура проекта

```
src/main/java/team26/
├── Application.java                     # Entry point
├── api/
│   └── ApiRoutes.java                   # REST routes, auth middleware
├── service/
│   ├── AuthService.java                 # Auth & session management
│   ├── DrawService.java                 # Draw lifecycle
│   ├── TicketService.java               # Ticket operations
│   └── LotteryEngine.java               # Number generation & validation
├── repository/
│   ├── JdbcUserRepository.java          # User CRUD
│   ├── JdbcDrawRepository.java          # Draw CRUD & settlement
│   └── JdbcTicketRepository.java        # Ticket CRUD
├── domain/
│   ├── lotteryDraw/
│   │   ├── LotteryDraw.java             # Draw entity (Hibernate)
│   │   ├── LotteryDrawRecord.java       # Draw entity (record)
│   │   └── LotteryDrawStatus.java       # Enum: SCHEDULED, ACTIVE, COMPLETED, CANCELLED  
│   ├── lotteryDrawResult/   
│   │   ├── LotteryDrawResults.java      # Draw result entity (Hibernate)
│   │   └── LotteryDrawResultsRecord.java  # Draw result entity (record) 
│   ├── lotteryTicket/       
│   │   ├── LotteryTicket.java           # Ticket entity (Hibernate)
│   │   ├── LotteryTicketRecord.java     # Ticket entity (record)
│   │   └── LotteryTicketStatus.java     # Enum: PENDING, WIN, LOSE
│   └── user/
│       ├── User.java                    
│       ├── UserRecord.java              # User entity (record)
│       └── UserRole.java                # Enum: ADMIN, USER              
├── config/
│   ├── api/
│   │   └── JavalinConfig.java           # API(Javalin) config
│   └── database/
│       ├── AppConfig.java               # Environment config
│       └── Database.java                # Connection pool & migration runner
├── exceptions/
│   ├── ApiException.java                # Unified error handling                    
│   ├── ErrorResponse.java               # Error Response entity (record)
│   └── UnauthorizedException.java       # Unauthorized error handling
└── util/
    ├── database/
    │   └── Helper.java                  # Lottery ticket`s numbers validation
    ├── PasswordHasher.java              # SHA-256 hashing
    └── NumberCodec.java                 # Encode/decode number lists

src/main/resources/
├── db/migration/
    └── V1__init_schema.sql              # Flyway migration (schema)

src/test/java/org/example/
└── AppTest.java                         # Unit tests for LotteryEngine

Docker/Infra:
├── Dockerfile                           # Multi-stage build
├── docker-compose.yml                   # App + PostgreSQL
├── .env_example                         # Environment template
├── db_dump_lottery_schema.sql           # Schema dump
└── README.md                            # Setup & API docs
```

---

## 2. МОДЕЛЬ ДАННЫХ

### 2.1 Таблица `users`

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    surname         VARCHAR(100) NOT NULL,
    login           VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(12) CHECK (phone ~ '^[0-9]{12}$') UNIQUE,
    role            VARCHAR(20)           DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    hashed_password VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_email_valid CHECK ( email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$' )
);
```

**Роли**:
- `ADMIN`: создаёт тиражи, проводит розыгрыши
- `USER`: покупает билеты, проверяет результаты

### 2.2 Таблица `lottery_draws`

```sql
CREATE TABLE lottery_draws (
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    draw_number   INTEGER     NOT NULL UNIQUE GENERATED ALWAYS AS IDENTITY,
    draw_name     VARCHAR(100)         DEFAULT null,
    total_tickets INTEGER     NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at    TIMESTAMP            DEFAULT current_timestamp
);
```

**Статусы**:
- `SCHEDULED`: тираж запланирован
- `ACTIVE`: идёт приём билетов
- `COMPLETED`: завершён, результаты определены
- `CANCELLED`: тираж отменен

### 2.3 Таблица `lottery_draw_results`

```sql
CREATE OR REPLACE FUNCTION generate_lottery_numbers()
    RETURNS INTEGER[] AS
$$
DECLARE
    result   INTEGER[] := ARRAY []::INTEGER[];
    next_num INTEGER;
BEGIN
    WHILE COALESCE(array_length(result, 1), 0) < 5
        LOOP
            next_num := floor(random() * 45 + 1)::INTEGER;

            IF NOT next_num = ANY (result) THEN
                result := array_append(result, next_num);
            END IF;
        END LOOP;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

```

```sql
CREATE TABLE lottery_draws_result
(
    id              UUID PRIMARY KEY    DEFAULT gen_random_uuid(),
    result_numbers  INTEGER[5] NOT NULL default generate_lottery_numbers(),
    created_at      TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    lottery_draw_id UUID       NOT NULL unique,

    CONSTRAINT fk_lottery_draws_id FOREIGN KEY (lottery_draw_id) REFERENCES lottery_draws (id) ON DELETE RESTRICT,

    CONSTRAINT check_array_length CHECK (array_length(result_numbers, 1) = 5),

    CONSTRAINT check_numbers_range CHECK (
        1 <= ALL (result_numbers) AND 45 >= ALL (result_numbers)
        ),

    CONSTRAINT check_unique_numbers CHECK (
        result_numbers[1] != result_numbers[2] AND
        result_numbers[1] != result_numbers[3] AND
        result_numbers[1] != result_numbers[4] AND
        result_numbers[1] != result_numbers[5] AND
        result_numbers[2] != result_numbers[3] AND
        result_numbers[2] != result_numbers[4] AND
        result_numbers[2] != result_numbers[5] AND
        result_numbers[3] != result_numbers[4] AND
        result_numbers[3] != result_numbers[5] AND
        result_numbers[4] != result_numbers[5]
        )
);

```

### 2.4 Таблица `lottery_tickets`

```sql
CREATE TABLE lottery_tickets (
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    lottery_draw_id UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK ( status IN ('PENDING', 'WIN', 'LOSE') ),
    ticket_numbers  INTEGER[5]  NOT NULL,
    created_at      TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_lottery_draw_id FOREIGN KEY (lottery_draw_id) REFERENCES lottery_draws (id) ON DELETE CASCADE,

    CONSTRAINT check_array_length CHECK (array_length(ticket_numbers, 1) = 5),

    CONSTRAINT check_numbers_range CHECK (
        1 <= ALL (ticket_numbers) AND 45 >= ALL (ticket_numbers)
        ),

    CONSTRAINT check_unique_numbers CHECK (
        ticket_numbers[1] != ticket_numbers[2] AND
        ticket_numbers[1] != ticket_numbers[3] AND
        ticket_numbers[1] != ticket_numbers[4] AND
        ticket_numbers[1] != ticket_numbers[5] AND
        ticket_numbers[2] != ticket_numbers[3] AND
        ticket_numbers[2] != ticket_numbers[4] AND
        ticket_numbers[2] != ticket_numbers[5] AND
        ticket_numbers[3] != ticket_numbers[4] AND
        ticket_numbers[3] != ticket_numbers[5] AND
        ticket_numbers[4] != ticket_numbers[5]
        )
);
```

**Статусы**:
- `PENDING`: ждёт завершения тиража
- `WIN`: билет выиграл
- `LOSE`: билет проиграл

### 2.5 Индексы

```sql
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_login ON users (login);

CREATE INDEX idx_draws_status ON lottery_draws (status);
CREATE INDEX idx_draws_name ON lottery_draws (draw_name);
CREATE INDEX idx_draws_number ON lottery_draws (draw_number);

CREATE INDEX idx_tickets_user ON lottery_tickets (user_id);
CREATE INDEX idx_tickets_lottery_draw ON lottery_tickets (lottery_draw_id);
CREATE INDEX idx_tickets_status ON lottery_tickets (status);

CREATE INDEX idx_lottery_draws_id ON lottery_draws_result (lottery_draw_id);
```

---

## 3. REST API


