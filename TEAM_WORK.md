## РАСПРЕДЕЛЕНИЕ РАБОТЫ В КОМАНДЕ ##

### Участник 1 (Архитектор / Lead Backend) (Александр Скотников) ###
- Дизайн архитектуры слоёв
- Настройка проекта (Maven, структура пакетов)
- Настройка окружения (конфиг, Database, Flyway)
- Интеграция компонентов
- Code Review


### Участники 2, 3 (Backend-API разработчик) (Сергей Кондрахин , Иван Кошевой) ###
- REST API слой (Javalin маршруты)
- HTTP middleware (авторизация)
- Request/Response обработка
- Error handling
- Тестирование API (Postman / curl)

### Файлы: ###
- api/*.java

### Участник 4 (Backend-Logic разработчик) (Александр Шарыкин)###
- Бизнес-логика сервисов
- Статусы и переходы состояний
- Алгоритмы генерации и сравнения чисел
- Unit-тесты логики

### Файлы: ###
service/*.java
AppTest.java

### Участник 5 (Database разработчик) (Тимофей Пичугин) ###
- Проектирование схемы БД
- Миграции (Flyway)
- JDBC Repository слой
- Работа с транзакциями (settlement)
- Тестирование на БД

### Файлы: ###
- repository/Jdbc*.java
- domain/*
- db/migration/schema.sql
- util/NumberCodec.java, util/PasswordHasher.java

### Участник 6 (DevOps / Documentation) (Егор Петренко) ###
- Docker конфигурация
- Docker Compose стек
- README и документация
- Подготовка к демонстрации
- Инструкции по развёртыванию

### Файлы: ###
- Dockerfile
- docker-compose.yml
- README.md
- env.example
- db_dump_lottery_schema.sql
