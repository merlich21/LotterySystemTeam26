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

## Инструкции по развёртыванию ##

    - Локальный запуск
    - Docker запуск
    - API примеры

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
- **Build**: Maven 3.9.9, Maven-wrapper-3.3.4
- **REST API**: Javalin 7.2.0 (без Spring)
- **БД**: PostgreSQL 18.3
- **Пул соединений**: HikariCP
- **Миграции**: Flyway
- **ORM Framework**: Hibernate
- **Вспомогательные Frameworks**: Jakarta, Lombok
- **Логирование**: SLF4J + Logback
- **Тестирование**: JUnit 5
- **Контейнеризация**: Docker + Docker Compose


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
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL,                           -- ADMIN | USER
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

**Роли**:
- `ADMIN`: создаёт тиражи, проводит розыгрыши
- `USER`: покупает билеты, проверяет результаты

### 2.2 Таблица `draws`

```sql
CREATE TABLE IF NOT EXISTS draws (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,                    -- ACTIVE | COMPLETED
    numbers_count INT NOT NULL,
    max_number INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

**Статусы**:
- `ACTIVE`: идёт приём билетов
- `COMPLETED`: приём билетов завершён, результаты определены


### 2.3 Таблица `draw_results`

```sql
CREATE TABLE IF NOT EXISTS draw_results (
    draw_id BIGINT PRIMARY KEY REFERENCES draws(id),
    winning_numbers VARCHAR(128) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 2.4 Таблица `tickets`

```sql
CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    draw_id BIGINT NOT NULL REFERENCES draws(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    numbers VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,                    -- PENDING | WIN | LOSE
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

**Статусы**:
- `PENDING`: ждёт завершения тиража
- `WIN`: билет выиграл
- `LOSE`: билет проиграл

### 2.5 Индексы

```sql
CREATE INDEX IF NOT EXISTS idx_draws_status ON draws(status);
CREATE INDEX IF NOT EXISTS idx_tickets_draw_id ON tickets(draw_id);
CREATE INDEX IF NOT EXISTS idx_tickets_user_id ON tickets(user_id);
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
- `controller/*`
- `security/*`
- `dto/*`

**Время**: 5-7 часов

---

#### **Участник 3 (Backend-Logic разработчик)**
- Бизнес-логика сервисов
- Статусы и переходы состояний
- Алгоритмы генерации и сравнения чисел
- Unit-тесты логики

**Файлы**:
- `service/*`
- `util/*`
- `validator/*`
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
- `repository/*`
- `entity/*`
- `mapper/*`
- `resources/db.migration/V1__init_schema.sql`

**Время**: 6-8 часов

---

#### **Участник 5 (DevOps / Documentation) - опционально**
- Docker конфигурация
- Docker Compose стек
- README и документация
- Подготовка к демонстрации
- Инструкции по развёртыванию

**Файлы**:
- `infra/*`
- `Dockerfile`
- `docker-compose.yml`
- `resources/hibernate.cfg.xml`
- `README.md`
- `.env_example`

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


