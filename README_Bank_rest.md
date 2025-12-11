# Система управления банковскими картами

Сборка и запуск через Docker Compose

1. Перейдите в корень проекта.

2. Постройте и запустите контейнеры:
   docker-compose up --build

Docker Compose поднимет два сервиса:
db — PostgreSQL, порт 5432
app — Spring Boot, порт 8080

Для фонового режима (detached mode):
docker-compose up -d --build

Проверка логов приложения:
docker-compose logs -f app

3. Проверка работы приложения

Swagger UI: http://localhost:8080/swagger-ui/index.html

REST API: 

/auth/login — логин, получение access + refresh токена

/cards — CRUD карт

/transfers — переводы между своими картами

Все эндпоинты, кроме /auth/login и /auth/refresh, требуют JWT Access Token в заголовке Authorization: Bearer <token>