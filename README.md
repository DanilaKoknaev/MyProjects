# Finance Management System

Backend-сервис для централизованного учёта доходов, расходов, переводов, бюджетов и финансовой отчётности подразделений.

## Стек

- Java 21, Spring Boot 4.1.1, Spring MVC, Lombok для доменных моделей и records для DTO;
- Spring Data JPA, Hibernate, PostgreSQL 17, Flyway;
- Spring Security, JWT access/refresh, Google OAuth2/OIDC;
- JUnit, Mockito, Testcontainers;
- Maven multi-module, Docker multi-stage build.

## Архитектура

Проект является модульным монолитом с группировкой `package-by-feature`.

```text
finance-management-parent
├── finance-service
│   ├── account
│   ├── audit
│   ├── auth
│   ├── budget
│   ├── category
│   ├── counterparty
│   ├── department
│   ├── report
│   ├── security
│   ├── transaction
│   └── user
└── startup-logger-spring-boot-starter
```

Финансовая операция хранится отдельно от проводок по счетам. Доход и расход создают одну проводку, перевод — две проводки с противоположными знаками в одной транзакции БД. Баланс счёта вычисляется как сумма проводок недрафтовых операций и напрямую не редактируется.

Подтверждённая операция не изменяется и не удаляется. Исправление создаёт отдельную подтверждённую сторнирующую операцию с обратными проводками и ссылкой на оригинал. Инвариант защищён внутри доменной сущности, а не только контроллером.

## Аутентификация

### Логин и пароль

```http
POST /api/v1/auth/login
Content-Type: application/json

{"email":"admin@example.com","password":"..."}
```

Access JWT действует 15 минут, refresh-токен — 30 дней. Сроки изменяются через `JWT_ACCESS_TTL` и `JWT_REFRESH_TTL`. Refresh-токены ротируются, отзываются при logout и хранятся в БД только как SHA-256 hash. Пароли хранятся только как BCrypt hash.

### Google OIDC

Начало входа: `GET /oauth2/authorization/google`. Callback: `/login/oauth2/code/google`. После успешного входа система ищет активного локального пользователя по подтверждённому Google email и возвращает собственную пару access/refresh.

Автосоздание пользователей намеренно выключено: роли и подразделения назначает `ADMIN`, поэтому внешний Google-аккаунт не может самостоятельно получить доступ. Перед Google-входом пользователь должен быть создан администратором с тем же email.

## Роли

- `ADMIN` — полное управление;
- `FINANCE_MANAGER` — бюджеты, отчёты всех подразделений и контрагенты;
- `ACCOUNTANT` — операции и бюджеты своего подразделения;
- `AUDITOR` — чтение всех финансовых данных и журнала аудита.

Критические команды защищены через `@PreAuthorize`; ограничения подразделения дополнительно проверяются в сервисном слое.

## REST API

Все прикладные маршруты версионированы префиксом `/api/v1`.

```text
/api/v1/auth
/api/v1/users
/api/v1/departments
/api/v1/accounts
/api/v1/categories
/api/v1/counterparties
/api/v1/transactions
/api/v1/budgets
/api/v1/reports/cash-flow?departmentId={uuid}&currency=RUB&from=2026-01-01&to=2026-01-31
/api/v1/reports/budget-execution?departmentId={uuid}&from=2026-01-01&to=2026-01-31
/api/v1/audit-events
```

Команды операции:

```text
POST   /api/v1/transactions
PUT    /api/v1/transactions/{id}
POST   /api/v1/transactions/{id}/confirm
POST   /api/v1/transactions/{id}/reverse
DELETE /api/v1/transactions/{id}
```

Все списочные endpoints используют Spring Data pagination: `?page=0&size=20`. Поле сортировки должно существовать у соответствующей сущности, например для транзакций: `?page=0&size=20&sort=operationDate,desc`.

Отчёты возвращаются в JSON. Этот формат выбран как нативный для REST, напрямую подходит фронтенду и BI-клиентам и не связывает расчёт отчёта с файловым форматом. CSV/XLSX может быть добавлен позднее как отдельное представление тех же расчётов.

Ошибки API и Spring Security имеют единый формат:

```json
{
  "timestamp": "2026-09-10T18:00:00Z",
  "status": 409,
  "code": "BUSINESS_RULE_VIOLATION",
  "message": "Confirmed transaction is immutable",
  "path": "/api/v1/transactions/...",
  "fieldErrors": []
}
```

## Запуск тестов

Требуются Java 21 и запущенный Docker Engine. PostgreSQL для интеграционных тестов создаётся Testcontainers автоматически.

```powershell
.\mvnw.cmd clean verify
```

Если Docker отсутствует, Testcontainers-тесты помечаются skipped, unit-тесты продолжают выполняться. При приёмке интеграционные тесты следует запускать с доступным Docker.

## Локальный/test профиль

Поднять PostgreSQL:

```powershell
$env:DB_PASSWORD='finance'
$env:JWT_SECRET='local-development-secret-must-be-at-least-32-characters'
docker compose up -d postgres
$env:SPRING_PROFILES_ACTIVE='test'
$env:DB_URL='jdbc:postgresql://localhost:5432/finance'
$env:DB_USERNAME='finance'
.\mvnw.cmd -pl finance-service -am spring-boot:run
```

Значения `finance/finance` относятся только к одноразовой локальной БД из Compose и не используются в prod.

### Запуск из IntelliJ IDEA

Для разработки удобно запусать PostgreSQL в Docker, а Spring Boot — из IntelliJ IDEA. Если Compose-контейнер приложения уже запущен, остановите его, чтобы освободить порт `8080`, и поднимите только базу:

```powershell
docker compose stop app
$env:DB_PASSWORD='finance'
docker compose up -d postgres
```

Откройте `Run | Edit Configurations`, выберите `FinanceManagementApplication` и укажите:

- main class: `org.example.finance.FinanceManagementApplication`;
- active profile: `test` (либо VM option `-Dspring.profiles.active=test`);
- environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/finance
DB_USERNAME=finance
DB_PASSWORD=finance
```

После этого запустите `FinanceManagementApplication` через Run или Debug. Файл `.env` IntelliJ IDEA и Spring Boot сами не загружают: для запуска без Compose переменные нужно добавить в Run Configuration или подключить `.env` средствами IDE.

## Prod профиль

Скопируйте `.env.example` в локальный `.env`, задайте настоящие секреты и не коммитьте этот файл (`.env` уже добавлен в `.gitignore`).

```powershell
Copy-Item .env.example .env
# Отредактируйте .env перед следующим шагом
docker compose --env-file .env up --build
```

Обязательные prod-переменные:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET                 минимум 32 случайных символа
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

Для первого запуска можно передать `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_FULL_NAME`. Пользователь создаётся только при отсутствии email; пароль немедленно BCrypt-хэшируется. После первичной настройки bootstrap-переменные рекомендуется удалить.

Проверка состояния: `GET /actuator/health`.

### Запуск собранного Docker-образа без Compose

Сначала соберите образ:

```powershell
docker build -t finance-management-system .
```

При `docker run` PostgreSQL уже должен быть доступен контейнеру. Передайте профиль, подключение и секреты через `--env-file`; значение `DB_URL` в таком случае должно указывать на доступное из контейнера имя или адрес БД, а не на `localhost` хостовой машины.

```powershell
docker run --rm --env-file .env -p 8080:8080 finance-management-system
```

### Настройка Google OAuth2/OIDC

В Google Cloud Console создайте OAuth client типа **Web application**. Для локального запуска добавьте authorized redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

Полученные Client ID и Client Secret запишите в локальный `.env` как `GOOGLE_CLIENT_ID` и `GOOGLE_CLIENT_SECRET`. Сам `.env` не коммитится. Пользователь с тем же email должен быть заранее создан в системе администратором; автосоздание через Google намеренно отключено.

## Flyway

Hibernate работает с `ddl-auto: validate`. Схема создаётся только миграциями из `finance-service/src/main/resources/db/migration`; изменение схемы требует новой миграции, а не редактирования уже применённой.

## Startup logger starter

Модуль `startup-logger-spring-boot-starter` подключён обычной зависимостью и регистрируется через `AutoConfiguration.imports`. При старте он выводит активные профили, версию приложения, URL и пользователя БД без пароля, а также длительность старта контекста.

Отключение без изменения кода:

```yaml
app:
  startup-logger:
    enabled: false
```
