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

ЗДЕСЬ БУДЕТ ОПИСАНИЕ REST API

---

## 4. БИЗНЕС-ЛОГИКА И СТАТУСЫ

### 4.1 Жизненный цикл тиража

```
                 ┌─────────────────────┐
                 │   СОЗДАНИЕ ТИРАЖА   │
                 │   (ADMIN создаёт)   │
                 └──────────┬──────────┘
                            ↓
                 ┌─────────────────────┐
                 │  Статус: ACTIVE     │
                 │  Принимаем билеты   │
                 └──────────┬──────────┘
                            ↓
              ┌─────────────────────────────┐
              │   ADMIN нажимает COMPLETE   │
              │  (POST /draws/{id}/complete)│
              └─────────────┬───────────────┘
                            ↓
        ┌─────────────────────────────────────────┐
        │   1. Генерируем выигрышные числа        │
        │   2. Создаём DrawResult                 │
        │   3. Обновляем статус Draw → COMPLETED  │
        │   4. Обновляем статусы Tickets:         │
        │      - совпадают с winning → WIN        │
        │      - остальные → LOSE                 │
        └───────────────────┬─────────────────────┘
                            ↓
                 ┌─────────────────────┐
                 │  Статус: COMPLETED  │
                 │  Результаты готовы  │
                 └─────────────────────┘
```

### 4.2 Жизненный цикл билета

```
              ┌──────────────────┐
              │  СОЗДАНИЕ БИЛЕТА │
              │  (USER покупает) │
              └────────┬─────────┘
                       ↓
        ┌─────────────────────────────┐
        │       Статус: PENDING       │
        │    Ждём завершения тиража   │
        └──────────────┬──────────────┘
                       ↓
  ┌─────────────────────────────────────────┐
  │     После COMPLETE тиража происходит    │
  │         сравнение чисел билета с        │
  │             winning_numbers             │
  └────────────────────┬────────────────────┘
                       ↓
       ┌────────────────────────────────┐
       │ Если совпадают/не совпадают:   │
       │       Статус: WIN/LOSE         │
       └────────────────────────────────┘
    
```

### 4.3 Правила валидации

**Draw создание**:
- `title` не пусто
- `numbersCount` ∈ [3, 10]
- `maxNumber` ∈ [10, 99]
- `numbersCount < maxNumber`

**Ticket создание**:
- Draw статус = ACTIVE
- Количество чисел = numbersCount тиража
- Все числа уникальны
- Все числа ∈ [1, maxNumber]

**Авторизация**:
- Все эндпоинты кроме `/auth/*` и `/health` требуют Bearer токен
- `/api/draws` требует ADMIN
- `/api/tickets` требует USER
- USER может видеть только свои билеты (кроме ADMIN)

---

## 5. РАСПРЕДЕЛЕНИЕ РАБОТЫ ПО КОМАНДЕ (6 человек)

#### **Участник 1 (Архитектор / Lead Backend)**
- Дизайн архитектуры слоёв
- Настройка проекта (Maven, структура пакетов)
- Настройка окружения (конфиг, Database, Flyway)
- Интеграция компонентов
- Code Review

**Файлы**:
- `pom.xml`
- `Application.java`
- `config/*`
- `exception/*`

**Время**: 4-6 часов

---

#### **Участники 2, 3 (Backend-API разработчики)**
- REST API слой (Javalin маршруты)
- HTTP middleware (авторизация)
- Request/Response обработка
- Error handling
- Тестирование API (Postman / curl)

**Файлы**:
- `api/ApiRoutes.java`

**Время**: 5-7 часов

---

#### **Участник 3 (Backend-Logic разработчик)**
- Бизнес-логика сервисов
- Статусы и переходы состояний
- Алгоритмы генерации и сравнения чисел
- Unit-тесты логики

**Файлы**:
- `service/AuthService.java`
- `service/DrawService.java`
- `service/TicketService.java`
- `service/LotteryEngine.java`
- `AppTest.java`

**Время**: 6-8 часов

---

#### **Участник 4 (Database разработчик)**
- Проектирование схемы БД
- Миграции (Flyway)
- JDBC Repository слой
- Работа с транзакциями (settlement)
- Тестирование на БД

**Файлы**:
- `repository/Jdbc*.java`
- `domain/*`
- `db/migration/V1__init_schema.sql`
- `util/NumberCodec.java`, `util/PasswordHasher.java`

**Время**: 6-8 часов

---

#### **Участник 5 (DevOps / Documentation) - опционально**
- Docker конфигурация
- Docker Compose стек
- README и документация
- Подготовка к демонстрации
- Инструкции по развёртыванию

**Файлы**:
- `Dockerfile`
- `docker-compose.yml`
- `README.md`
- `.env.example`
- `db_dump_lottery_schema.sql`

**Время**: 3-4 часа

---

## 6. ЭТАПЫ РАЗРАБОТКИ

### Этап 1: Инициализация проекта (4 часа)

**Цель**: Подготовить базовую структуру и окружение

**Задачи**:
- [ ] Создать структуру папок (`src/main/java/org/example/*`)
- [ ] Настроить `pom.xml` с зависимостями (Javalin, PostgreSQL, Flyway, HikariCP)
- [ ] Добавить плагины (maven-compiler, maven-shade, exec-maven)
- [ ] Создать `AppConfig` для управления окружением
- [ ] Создать `Database` для управления пулом соединений
- [ ] Создать `ApiException` для единообразной обработки ошибок
- [ ] Инициализировать `Application.main()` как entry point

**Ответственный**: Участник 1

**Проверка**: Проект собирается (`mvn clean compile`)

---

### Этап 2: Модель данных и миграции (3 часа)

**Цель**: Определить сущности и схему БД

**Задачи**:
- [ ] Создать domain records: `User`, `Draw`, `Ticket`, `DrawResult`
- [ ] Создать enums: `Role`, `DrawStatus`, `TicketStatus`
- [ ] Создать `V1__init_schema.sql` с таблицами
- [ ] Добавить индексы на часто используемые колонки
- [ ] Создать `NumberCodec` для кодирования списков чисел

**Ответственный**: Участник 4

**Проверка**: Миграция применяется на локальной БД без ошибок

---

### Этап 3: Repository слой (4 часа)

**Цель**: Реализовать JDBC доступ к БД

**Задачи**:
- [ ] `JdbcUserRepository`: CRUD для users, поиск по username
- [ ] `JdbcDrawRepository`: CRUD для draws, поиск активных, settlement
- [ ] `JdbcTicketRepository`: CRUD для tickets, поиск по draw
- [ ] Реализовать транзакцию в `completeAndSettle()` для консистентности

**Ответственный**: Участник 4

**Проверка**: Unit-тесты на операции (create, findById, update)

---

### Этап 4: Сервисный слой (6 часов)

**Цель**: Реализовать бизнес-логику

**Задачи**:
- [ ] `LotteryEngine`: генерация чисел, валидация
- [ ] `AuthService`: регистрация, логин, session management, seed users
- [ ] `DrawService`: создание тиража, завершение, поиск активных
- [ ] `TicketService`: создание билета, проверка результата
- [ ] Логирование ключевых операций

**Ответственный**: Участник 3

**Проверка**: Unit-тесты покрывают основной flow (валидация, генерация)

---

### Этап 5: REST API слой (5 часов)

**Цель**: Реализовать HTTP маршруты и авторизацию

**Задачи**:
- [ ] `ApiRoutes`: регистрация всех маршрутов
- [ ] Middleware для Bearer token авторизации
- [ ] Middleware для проверки роли (ADMIN/USER)
- [ ] Request/Response mapping (DTO классы)
- [ ] Error handling и HTTP статус коды

**Ответственный**: Участник 2

**Проверка**: Тестирование curl/Postman (создание тиража, покупка билета, проверка результата)

---

### Этап 6: Интеграция и тестирование (4 часа)

**Цель**: Убедиться, что все слои работают вместе

**Задачи**:
- [ ] Интеграционное тестирование на локальной БД
- [ ] E2E сценарий: регистрация → логин → создание тиража → покупка билета → завершение → проверка
- [ ] Проверка транзакций при settlement
- [ ] Проверка авторизации (403, 401)
- [ ] Load-тестирование (10-20 одновременных билетов)

**Ответственный**: Участник 1 (координирует), вся команда

**Проверка**: Все сценарии в README работают без ошибок

---

### Этап 7: Контейнеризация и документация (3 часа)

**Цель**: Подготовить к production-like запуску

**Задачи**:
- [ ] Написать `Dockerfile` (multi-stage build)
- [ ] Написать `docker-compose.yml` (app + postgres)
- [ ] Написать подробный `README.md`:
    - Архитектура
    - Локальный запуск
    - Docker запуск
    - API примеры
    - Статусы и переходы
    - Распределение ролей
- [ ] Создать `.env.example`
- [ ] Создать SQL дамп схемы

**Ответственный**: Участник 5 (опционально, можно участник 1)

**Проверка**: `docker compose up --build` запускает приложение и БД

---

### Этап 8: Финальная подготовка к демо (2 часа)

**Цель**: Убедиться, что готово к защите

**Задачи**:
- [ ] Создать скрипт демонстрации (curl команды)
- [ ] Подготовить тестовые данные (дефолтные пользователи)
- [ ] Проверить обработку граничных случаев:
    - Попытка создать дублирующийся username
    - Попытка завершить уже завершённый тираж
    - Попытка купить билет для неактивного тиража
    - USER пытается создать тираж (403)
- [ ] Проверить восстановление после перезагрузки (данные в БД сохраняются)

**Ответственный**: Участник 1 + вся команда

**Проверка**: Demo script работает и демонстрирует все основные сценарии

---


