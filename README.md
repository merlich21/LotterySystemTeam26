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

## 1. Инструкции по развёртыванию приложения ##

## 1.1 Развёртывание и запуск на локальном сервере (ПК)

### Требования:

#### На сервере (ПК) должны быть установлены:
````
- Java 17 (JDK) или выше.
- Maven 3.9.9
- БД PostgreSQL версии 18.3 или выше.
````

#### По умолчанию программа будет пытаться установить соединение с БД используя настройки:
````
- DB_URL=jdbc:postgresql://hackathon_postgres:5432/lottery_db
- DB_USER=postgres
- DB_PASSWORD=postgres
````

#### Для задания собственных настроек подключения к БД **НЕОБХОДИМО СОЗДАТЬ ПЕРЕМЕННЫЕ ОКРУЖЕНИЯ**:
```
- DB_URL=jdbc:postgresql://[HOST]:[PORT]/[DB_NAME]
- DB_USER=[USERNAME]
- DB_PASSWORD=[PASSWORD]
```

### Сборка приложения:

Для сборки приложения нужно открыть директорию с проектом в терминале и выполнить команду:
```shell
    mvn clean package
```

После успешного выполнения этой команды в папке проекта появится новая папка target,
внутри которой будет лежать файл 
```LotterySystemTeam26-1.0.jar.``` 
Это и есть наше готовое приложение.

### Для запуска приложения нужно:
- открыть терминал, перейти в директорию с файлом ```LotterySystemTeam26-1.0.jar```
- выполнить команду
```shell
    java -jar LotterySystemTeam26-1.0.jar
```
API будет доступно на `http://localhost:8080`.



## 1.2 Развертывание и запуск в Docker

### Требования:

#### На сервере (ПК) должны быть установлены:
````
- Docker
- Docker-compose
````
#### ВАЖНО!!!
```text
!!! В файле mvnw окончания строк должны быть LF!!!
!!! Если установлены символы окончания строки CRLF,
при сборке Docker-образа возникнет ошибка!!!
```


### Скрипт запуска БД PostgreSQL(папка infra) и что делает:

#### Скрипт Windows запуска БД PostgreSQL в контейнере:
````shell 
    run-db.bat 
````
#### Cкрипт Linux запуска БД PostgreSQL в контейнере
```shell
    run-db.sh
```

- Запускать в директории infra
- Удаляет старые образ и контейнер
- Спрашивает желаемый пароль для БД в контейнере
- Стягивает базовый образ с PostgreSQL
- Собирает новый образ
- Запускает контейнер с нужными параметрами
- При остановке или удалении контейнера **данные сохраняются** (созданные таблицы и данные в них)

### Как запустить контейнер с Java:
````shell
    docker build -t hackathon_java -f .\infra\java-dockerfile .
    docker run --name hackathon_java -p 8080:8080 -e DB_URL=jdbc:postgresql://hackathon_postgres:5432/lottery_db -e DB_USER_NAME=postgres -e DB_USER_PASSWORD=postgres --network hackathon_network -d hackathon_java
````


### Как запустить приложение целиком (предыдущие шаги необязательны):
- #### В первый раз:
```shell
    docker compose -f infra/docker-compose.yaml up -d --build
```
 
- #### Последующие разы без пересборки:
```shell
    docker compose -f infra/docker-compose.yaml up -d
```
API будет доступно на `http://localhost:8080`.

---

## 2. Инструкция по работе приложения


См. разделы 5 и 7.


---

## 3. АРХИТЕКТУРА РЕШЕНИЯ

### 3.1 Слои приложения

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
│  - GeneratorUtil (number generation)│
│  - [Draw/Ticker/User]Validator      │
│          (validations)              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Repository Layer (JDBC)            │
│  - UserRepository                   │
│  - DrawRepository                   │
│  - DrawResultRepository             │
│  - TicketRepository                 │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Database Layer (PostgreSQL)        │
│  - users,                           │
│  - draws,                           │
│  - tickets,                         │
│  - draw_results                     │
└─────────────────────────────────────┘
```


### 3.2 Технологический стек

- **Язык**: Java 17 (JDK)
- **Build**: Maven 3.9.9, Maven-wrapper-3.3.4
- **REST API фреймворк**: Javalin 6.1.3 (без Spring)
- **ORM фреймворк**: Hibernate
- **Вспомогательные библиотеки**: Lombok, Jsonwebtoken
- **СУБД**: PostgreSQL 18.3
- **Контейнеризация**: Docker + Docker Compose

---

## 4. МОДЕЛЬ ДАННЫХ

![DB_diagram_draw.jpeg](DB_diagram_draw.jpeg)

![DB_diagram_users.jpeg](DB_diagram_users.jpeg)

### 4.1 Таблица `users`

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

### 4.2 Таблица `draws`

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


### 4.3 Таблица `draw_results`

```sql
CREATE TABLE IF NOT EXISTS draw_results (
    draw_id BIGINT PRIMARY KEY REFERENCES draws(id),
    winning_numbers INTEGER[] NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 4.4 Таблица `tickets`

```sql
CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    draw_id BIGINT NOT NULL REFERENCES draws(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    numbers INTEGER[] NOT NULL,
    status VARCHAR(16) NOT NULL,                    -- PENDING | WIN | LOSE
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

**Статусы**:
- `PENDING`: ждёт завершения тиража
- `WIN`: билет выиграл
- `LOSE`: билет проиграл

### 4.5 Индексы

```sql
CREATE INDEX IF NOT EXISTS idx_draws_status ON draws(status);
CREATE INDEX IF NOT EXISTS idx_tickets_draw_id ON tickets(draw_id);
CREATE INDEX IF NOT EXISTS idx_tickets_user_id ON tickets(user_id);
```

---

## 5. REST API

### 5.1 Аутентификация

#### `POST /api/auth/register`
Регистрация пользователя
```
Content-Type: application/json

Request:
{
  "username": "user1",
  "password": "user123"
}

Response (201):
{
  "id": 3,
  "username": "user1",
  "role": "USER"
  "jwt": "eyJhbGciOiJIUzI1NiJ9..."
}

Error (409): User with that username already exists
```

#### `POST /api/auth/login`
Авторизация пользователя/администратора
```
Content-Type: application/json

Request:
{
  "username": "admin",
  "password": "admin123"
}

Response (200):
{
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
  "jwt": "eyJhbGciOiJIUzI1NiJ9..."
}

Error (401): Invalid credentials
```

### 5.2 Управление тиражами

#### `POST /api/draws` (ADMIN only)
Создание тиража
```
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

Request:
{
  "title": "First Draw",
  "numbersCount": 5,
  "maxNumber": 30
}

Response (201):
{
  "id": 1,
  "title": "First Draw",
  "status": "ACTIVE",
  "numbersCount": 5,
  "maxNumber": 30,
  "createdAt": "2026-05-09T19:00:00.0000000Z"
}

Error (400): Title is required
Error (400): Numbers count must be in range [3..10]
Error (400): Max number must be in range [10..99]
Error (400): Numbers count must be less than max number
```

#### `GET /api/draws/active`
Получение списка активных тиражей
```
Authorization: Bearer <TOKEN>

Response (200):
[
    {
      "id": 1,
      "title": "First Draw",
      "status": "ACTIVE",
      "numbersCount": 5,
      "maxNumber": 30,
      "createdAt": "2026-05-09T19:00:00.0000000Z"
    }
]
```

#### `POST /api/draws/{drawId}/complete` (ADMIN only)
Завершение тиража и генерация выигрышной комбинации
```
Authorization: Bearer <ADMIN_TOKEN>

Response (200):
{
  "id": 1,
  "title": "First Draw",
  "status": "COMPLETED",
  "numbersCount": 5,
  "maxNumber": 30,
  "createdAt": "2026-05-09T19:00:00.0000000Z"
}

Внутри:
- Генерируется выигрышная комбинация
- Обновляются статусы всех билетов (WIN/LOSE)
- Используется транзакция для консистентности

Error (404): Draw with id (drawId) was not found
Error (409): Draw with id (drawId) is not completed yet
```

### 5.3 Управление билетами

#### `POST /api/tickets` (USER only)
Создание билета
```
Authorization: Bearer <USER_TOKEN>
Content-Type: application/json

Request:
{
  "drawId": 1,
  "numbers": [1, 7, 11, 23, 25]
}

Response (201):
{
  "id": 1,
  "drawId": 1,
  "userId": 3,
  "numbers": [1, 7, 11, 23, 25],
  "status": "PENDING",
  "createdAt": "2026-05-09T19:30:00.0000000Z"
}

Error (400): Numbers count must be exactly (draw.numbers_count)
Error (400): Each number must be between 1 and (draw.max_number)
Error (400): Numbers count must be less than max number
Error (404): Draw with id (drawId) was not found
Error (409): Draw with id (drawId) is already completed. Tickets can only be created for an active draw
```

#### `GET /api/tickets/{ticketId}/result`
Проверка результата билета
```
Authorization: Bearer <TOKEN>

Response (200):
{
  "id": 1,
  "drawId": 1,
  "userId": 3,
  "numbers": [1, 7, 11, 23, 25],
  "status": "PENDING",
  "createdAt": "2026-05-09T19:30:00.0000000Z"
}

Error (403): You can only access your own ticket (USER only)
Error (404): Ticket not found
```

### 5.4 Результаты тиража

#### `GET /api/draws/{drawId}/result`
```
Authorization: Bearer <TOKEN>

Response (200):
{
  "drawId": 1,
  "winningNumbers": [1, 7, 11, 23, 35],
  "generatedAt": "2026-04-15T20:00:00Z"
}

Error (404): Draw not found
Error (409): Draw is not completed yet
```

---

## 6. БИЗНЕС-ЛОГИКА И СТАТУСЫ

### 6.1 Жизненный цикл тиража

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

### 6.2 Жизненный цикл билета

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

### 6.3 Правила валидации

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
- Все эндпоинты кроме `/auth/*` требуют Bearer токен
- `/api/draws` требует ADMIN
- `/api/tickets` требует USER
- USER может видеть только свои билеты (кроме ADMIN)

---

## 7. ТЕСТОВЫЕ СЦЕНАРИИ

### Сценарий 1: Happy Path

```
1. Регистрируем ADMIN-а
   POST /api/auth/register → {username: "admin", password: "admin123"}

2. Логинимся как ADMIN
   POST /api/auth/login → получаем token_admin

3. Создаём тираж
   POST /api/draws → title: "Weekly", numbersCount: 5, maxNumber: 36
   → Draw id = 1, status = ACTIVE

4. Регистрируем USER-а
   POST /api/auth/register → {username: "user1", password: "user123"}

5. Логинимся как USER
   POST /api/auth/login → получаем token_user

6. Покупаем билет
   POST /api/tickets → drawId: 1, numbers: [1,7,11,23,35]
   → Ticket id = 1, status = PENDING

7. Завершаем тираж (ADMIN)
   POST /api/draws/1/complete
   → Генерируется winning_numbers, все tickets переходят в WIN/LOSE

8. Проверяем результат
   GET /api/tickets/1/result → result: "WIN" или "LOSE"
```

### Сценарий 2: Ошибки авторизации

```
1. Попытка создать draw без токена
   POST /api/draws → 401 Unauthorized

2. Попытка создать draw с USER токеном
   POST /api/draws (token: token_user) → 403 Forbidden

3. Попытка купить билет с ADMIN токеном
   POST /api/tickets (token: token_admin) → 403 Forbidden

4. Попытка доступа к чужому билету
   GET /api/tickets/2/result (token: token_user, но ticket.user_id ≠ user_id)
   → 403 Forbidden
```

### Сценарий 3: Граничные случаи

```
1. Попытка создать тираж с invalid параметрами
   POST /api/draws → numbersCount: 100 → 400 Bad Request

2. Попытка купить билет с дублирующимися числами
   POST /api/tickets → numbers: [1,1,7,11,23] → 400 Bad Request

3. Попытка купить билет для завершённого тиража
   POST /api/tickets (drawId: завершённый) → 409 Conflict

4. Попытка завершить уже завершённый тираж
   POST /api/draws/1/complete (второй раз) → 409 Conflict
```

---

