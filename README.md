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

### 2.2 Таблица `draws`

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
- `ACTIVE`: идёт приём билетов
- `COMPLETED`: завершён, результаты определены

### 2.3 Таблица `draw_results`

```sql
CREATE TABLE draw_results (
    draw_id BIGINT PRIMARY KEY REFERENCES draws(id),
    winning_numbers VARCHAR(128) NOT NULL, -- "1,7,11,23,35"
    generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 2.4 Таблица `tickets`

```sql
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    draw_id BIGINT NOT NULL REFERENCES draws(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    numbers VARCHAR(128) NOT NULL,        -- "1,7,11,23,35"
    status VARCHAR(16) NOT NULL,          -- 'PENDING' | 'WIN' | 'LOSE'
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    checked_at TIMESTAMPTZ                 -- когда определён результат
);
```

**Статусы**:
- `PENDING`: ждёт завершения тиража
- `WIN`: билет выиграл
- `LOSE`: билет проиграл

### 2.5 Индексы

```sql
CREATE INDEX idx_draws_status ON draws(status);
CREATE INDEX idx_tickets_draw_id ON tickets(draw_id);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
```

---

## 3. REST API


