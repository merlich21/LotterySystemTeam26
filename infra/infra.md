## Инструкции по работе

## Скрипт запуска БД PostgreSQL и что делает?
- **run-db.bat** - скрипт Windows запуска БД PostgreSQL в контейнере
- **run-db.sh** - скрипт Linux запуска БД PostgreSQL в контейнере

- Запускать в директории infra
- Удаляет старые образ и контейнер
- Спрашивает желаемый пароль для БД в контейнере
- Стягивает базовый образ с PostgreSQL
- Собирает новый образ
- Запускает контейнер с нужными параметрами
- При остановке или удалении контейнера **данные сохраняются** (созданные таблицы и данные в них)

## Как запустить контейнер с Java?
- docker build -t hackathon_java -f .\infra\java-dockerfile .
- docker run --name hackathon_java -p 8080:8080 -e DB_URL=jdbc:postgresql://hackathon_postgres:7432/lottery_db -e DB_USER_NAME=postgres -e DB_USER_PASSWORD=postgres --network hackathon_network -d hackathon_java

## Как запустить приложение целиком?
- Первый раз:
- docker compose -f infra/docker-compose.yaml up -d --build
- Без пересборки:
- docker compose -f infra/docker-compose.yaml up -d