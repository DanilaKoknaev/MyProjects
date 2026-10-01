# Order Processing System

Учебная событийная система обработки заказов на Java 21, Spring Boot, Apache Kafka и PostgreSQL.

## Архитектура

```text
POST /api/orders -> new_orders -> Payment -> payed_orders -> Shipping -> sent_orders -> Notifications
                                      |             |
                                      +-------------+----> Orders обновляет статус
```

- **Orders** принимает и хранит заказы, публикует `new_orders`, возвращает текущий статус.
- **Payment** обрабатывает оплату и публикует `payed_orders`.
- **Shipping** упаковывает заказ, создаёт трек-номер и публикует `sent_orders`.
- **Notifications** получает `sent_orders` и отправляет уведомление (в учебной реализации — запись в лог).

Все события публикуются с ключом `orderId`. Поэтому события одного заказа попадают в одну партицию, а разные заказы равномерно распределяются стандартным Kafka partitioner. Количество партиций задаётся переменной `KAFKA_PARTITIONS`, число потоков каждого consumer — `KAFKA_CONSUMER_CONCURRENCY`.

После трёх повторных попыток с экспоненциальной задержкой необработанное событие направляется в `<topic>.DLT`. Payment, Shipping и Notifications сохраняют `eventId` в таблице `processed_events`, поэтому повторная доставка события не приводит к повторному выполнению операции.

## Требования

- Docker Desktop с Docker Compose;
- для локального запуска из IDE — JDK 21;
- Maven устанавливать отдельно не обязательно: проект содержит Maven Wrapper 3.9.11.

Проверьте активную Java перед сборкой:

```powershell
java -version
.\mvnw.cmd -version
```

Обе команды должны показывать Java 21. Если Maven использует другую Java, задайте `JAVA_HOME` на каталог JDK 21, перезапустите терминал и повторите `.\mvnw.cmd -version`. Docker-сборка всегда использует Temurin 21 из `Dockerfile`.

## Запуск

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
```

Создать заказ:

```powershell
$body = @{
  userId = "USR-501"
  items = @(@{ productId = "PRD-10"; quantity = 2; price = 1500.00 })
  totalAmount = 3000.00
  currency = "RUB"
} | ConvertTo-Json -Depth 5

$order = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/orders `
  -ContentType "application/json" -Body $body
$order
```

Через несколько секунд проверить итоговый статус и логи:

```powershell
Invoke-RestMethod http://localhost:8081/api/orders/$($order.id)
docker compose logs payment-service shipping-service notification-service
```

Остановить приложения, сохранив данные:

```powershell
docker compose down
```

Удалить контейнеры вместе с учебной базой данных:

```powershell
docker compose down -v
```

## Локальная разработка

Инфраструктуру можно запустить в Docker, а приложения — из IDE:

```powershell
docker compose up -d postgres kafka kafka-init
.\mvnw.cmd clean verify
```

Порты приложений: Orders `8081`, Payment `8082`, Shipping `8083`, Notifications `8084`. Health endpoints доступны по `/actuator/health`.

Не запускайте одновременно один и тот же сервис из IDE и Docker: второй экземпляр не сможет занять тот же порт. При ошибке `port is already allocated` остановите экземпляр в IDE либо соответствующий контейнер.

`Dockerfile` собирает образ только одного микросервиса (по умолчанию Orders). Он не запускает PostgreSQL, Kafka и остальные три сервиса. Для всей системы используется `docker-compose.yml`. В IntelliJ откройте этот файл и нажмите зелёную кнопку запуска рядом с `services:` либо выберите готовую конфигурацию **Docker Compose — All Services**. Команды в терминале для этого не нужны.

Если приложения запускаются из IntelliJ как Java-классы, в Docker должны работать только инфраструктурные сервисы:

1. Запустите конфигурацию **Docker Compose — Infrastructure** и не нажимайте Stop у неё.
2. Дождитесь запуска PostgreSQL и Kafka.
3. Запускайте четыре `*Application` из IntelliJ.

Эквивалентные команды для диагностики:

```powershell
docker compose stop order-service payment-service shipping-service notification-service
docker compose up -d postgres kafka kafka-init
```

## Масштабирование

При шести партициях можно, например, запустить два экземпляра Payment по три consumer-потока. Суммарное число активных consumer-потоков одной группы не должно превышать число партиций, иначе лишние потоки будут простаивать.

Для горизонтального масштабирования через Compose сначала уберите фиксированный `ports` у масштабируемого сервиса (или замените его на `expose`), затем выполните:

```powershell
docker compose up -d --scale payment-service=2
```
