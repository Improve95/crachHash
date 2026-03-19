# CrackHash Manager

Сервис-оркестратор распределённого подбора хешей. Принимает запросы на взлом хешей от клиентов через REST API, распределяет задачи между воркерами, собирает и агрегирует результаты.

## Как это работает

1. Клиент отправляет POST-запрос с хешем и максимальной длиной пароля.
2. Менеджер разбивает пространство перебора на равные части и раздаёт их воркерам.
3. Каждый воркер перебирает свой диапазон паролей из заданного алфавита.
4. Менеджер собирает ответы воркеров и возвращает найденные совпадения клиенту.

---

## Режимы работы

Сервис поддерживает два режима передачи задач воркерам, переключаемых через переменную окружения `SEND_TYPE`.

### REST-режим (`SEND_TYPE=rest`)

- Задачи отправляются воркерам по HTTP.
- Состояние задач хранится в памяти (теряется при перезапуске).
- Подходит для разработки и тестирования.

### Kafka-режим (`SEND_TYPE=kafka`)

- Задачи отправляются через Kafka-топик.
- Состояние задач персистентно хранится в MongoDB (набор реплик из 3 узлов).
- Устойчив к перезапускам.
- Рекомендуется для production-окружения.

### Переключение режима

Установить переменную окружения перед запуском:

```bash
# REST-режим (по умолчанию)
SEND_TYPE=rest

# Kafka-режим
SEND_TYPE=kafka
```

В `docker-compose.yml` переменная задаётся в секции `environment` сервиса `crackhash-manager-service`:

```yaml
environment:
  SEND_TYPE: kafka   # или rest
```

---

## Конфигурация

### Переменные окружения

| Переменная       | По умолчанию            | Описание                                 |
|------------------|-------------------------|------------------------------------------|
| `SEND_TYPE`      | `rest`                  | Режим отправки задач: `rest` или `kafka` |
| `WORKER_NUMBER`  | `3`                     | Количество воркеров                      |
| `WORKER_URL_1`   | `http://localhost:8086` | URL первого воркера                      |
| `WORKER_URL_2`   | `http://localhost:8086` | URL второго воркера                      |
| `WORKER_URL_3`   | `http://localhost:8086` | URL третьего воркера                     |
| `KAFKA_URL`      | `localhost:9092`        | Адрес Kafka-брокера                      |
| `MONGO_DB_HOST`  | `localhost`             | Хост MongoDB                             |
| `MONGO_DB_PORT`  | `27017`                 | Порт MongoDB                             |

### Параметры в `application.yaml`

```yaml
server:
  port: 8085                          # Порт сервиса

send-type: ${SEND_TYPE:rest}          # Режим отправки задач

worker:
  number: ${WORKER_NUMBER:3}          # Количество воркеров
  urls:
    - ${WORKER_URL_1:http://localhost:8086}
    - ${WORKER_URL_2:http://localhost:8086}
    - ${WORKER_URL_3:http://localhost:8086}

crack-hash:
  task:
    in_process_lifetime-duration-threshold: PT5S   # Таймаут IN_PROGRESS → FAILED
    half_ready_lifetime-duration-threshold: PT10S  # Таймаут HALF_READY → FAILED

  kafka:
    producer:
      config:
        bootstrap-servers: ${KAFKA_URL:localhost:9092}
        acks: all
        retries: 0
      topic: crackhash_task_request_topic
    consumer:
      config:
        bootstrap-servers: ${KAFKA_URL:localhost:9092}
        group-id: crackhash_task_result_group_id
        auto-offset-reset: earliest
      topic: crackhash_task_result_topic

spring:
  mongodb:
    host: ${MONGO_DB_HOST:localhost}
    port: ${MONGO_DB_POST:27017}
    database: crackhash_db_mongo
    username: mongo_user
    password: mongo_password
    authentication-database: admin
```

### Алфавит

Алфавит для перебора паролей задан в классе `AlphabetConfig`:

```java
// src/main/java/ru/nsu/crackhash/manager/config/alphabet/AlphabetConfig.java
private final List<String> alphabet = List.of("r", "i", "s", "e", "m", "1", "2");
```

Для изменения алфавита — отредактируйте этот список и пересоберите сервис.

---

## REST API

Базовый путь: `/api/hash`

### POST `/api/hash/crack` — запустить взлом хеша

**Тело запроса:**
```json
{
  "hash": "5d41402abc4b2a76b9719d911017c592",
  "maxLength": 5
}
```

**Ответ:**
```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

### GET `/api/hash/request/status?requestId={uuid}` — статус задачи

**Ответ:**
```json
{
  "crackingHashTaskStatus": "READY",
  "answers": ["hello"]
}
```

**Возможные статусы задачи:**

| Статус        | Описание                                              |
|---------------|-------------------------------------------------------|
| `WAITING`     | Задача в очереди, ещё не распределена                 |
| `IN_PROGRESS` | Задача распределена по воркерам, ожидаются ответы     |
| `HALF_READY`  | Часть воркеров ответила                               |
| `READY`       | Все воркеры ответили, результаты готовы               |
| `FAILED`      | Задача превысила таймаут и помечена как упавшая       |

---

### POST `/api/hash/task/result` — получить результат от воркера (внутренний, только для REST-режима)

**Тело запроса:**
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "answers": ["hello", "world"]
}
```

---

## Запуск

### Требования

- Docker и Docker Compose
- Или Java 21 + Gradle (для локального запуска без Docker)

### Предварительная настройка: создание Docker-сети

Перед первым запуском необходимо создать общую сеть:

```bash
docker network create crackhash-net
```

### 1. REST-режим (только менеджер + воркеры)

Убедитесь, что в `docker-compose.yml` задан `SEND_TYPE=rest`, затем:

```bash
docker-compose up --build
```

Сервис будет доступен на `http://localhost:8085`.

### 2. Kafka-режим (менеджер + MongoDB + Kafka)

```bash
# Запустить MongoDB replica set + менеджер
docker-compose -f docker-compose-mongo.yml -f docker-compose.yml up --build
```

> Перед первым запуском MongoDB replication set нужно инициализировать вручную. Подключитесь к `mongo-1` и выполните:
> ```js
> rs.initiate({
>   _id: "rs0",
>   members: [
>     { _id: 0, host: "mongo-1:27017" },
>     { _id: 1, host: "mongo-2:27017" },
>     { _id: 2, host: "mongo-3:27017" }
>   ]
> })
> ```

### 3. Локальный запуск без Docker

```bash
# Сборка
./gradlew bootJar

# Запуск в REST-режиме
java -jar build/libs/*.jar

# Запуск в Kafka-режиме с кастомными параметрами
SEND_TYPE=kafka KAFKA_URL=localhost:9092 MONGO_DB_HOST=localhost java -jar build/libs/*.jar
```

---

## Технический стек

| Компонент       | Версия / Описание                    |
|-----------------|--------------------------------------|
| Java            | 21                                   |
| Spring Boot     | 4.0.2                                |
| Spring Kafka    | —                                    |
| Spring Data MongoDB | —                               |
| Gradle          | 9.0.0                                |
| MongoDB         | 8.0 (replica set rs0, 3 узла)        |
| Kafka           | confluent 7.9.0                      |
| Docker base     | eclipse-temurin:21-jre               |