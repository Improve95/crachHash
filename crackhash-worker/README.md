# crackhash-worker

Воркер-сервис распределённой системы взлома MD5-хешей методом перебора. Получает задачи на взлом от менеджера, перебирает комбинации символов из заданного алфавита в своей части пространства поиска, находит совпадения и отправляет результаты обратно.

## Что делает сервис

1. Получает задачу: хеш, алфавит, максимальную длину строки, номер и общее количество партиций.
2. Генерирует все перестановки с повторениями символов алфавита длиной от 1 до `maxLength`.
3. Обрабатывает только свою партицию — диапазон `[partNumber, partCount)`.
4. Хеширует каждый кандидат MD5 и сравнивает с целевым хешем.
5. Отправляет найденные совпадения обратно менеджеру.

Обработка задач выполняется асинхронно в фиксированном пуле потоков.

---

## Режимы работы

Сервис поддерживает два режима коммуникации с менеджером, управляемых через свойство `send-type`.

### REST-режим (по умолчанию)

```
send-type: rest
```

- Задачи поступают через POST `/internal/api/worker/hash/crack/task`.
- Результаты отправляются менеджеру через Feign-клиент на `POST /api/hash/task/result`.

### Kafka-режим

```
send-type: kafka
```

- Задачи поступают из топика `crackhash_task_request_topic`.
- Результаты публикуются в топик `crackhash_task_result_topic`.

---

## Конфигурация

Основной файл конфигурации: `src/main/resources/application.yaml`

| Параметр | Переменная окружения | По умолчанию | Описание |
|---|---|---|---|
| `send-type` | `SEND_TYPE` | `rest` | Режим коммуникации: `rest` или `kafka` |
| `crack-hash.cracking-hash-thread-pool-size` | `CRACKING_HASH_THREAD_POOL_SIZE` | `10` | Размер пула потоков для перебора |
| `server.port` | — | `8086` | HTTP-порт сервиса |
| `MANAGER_URL` | `MANAGER_URL` | `http://localhost:8085` | URL менеджер-сервиса (для REST-режима) |
| `KAFKA_URL` | `KAFKA_URL` | `localhost:9092` | Адрес Kafka-брокера (для Kafka-режима) |
| `crack-hash.kafka.consumer.properties.concurrency` | `WORKER_NUMBER` | `2` | Количество параллельных Kafka-потребителей |

### Kafka-топики

| Топик | Назначение |
|---|---|
| `crackhash_task_request_topic` | Входящие задачи на взлом |
| `crackhash_task_result_topic` | Исходящие результаты |

### REST API

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/internal/api/worker/hash/crack/task` | Принять задачу на взлом (только REST-режим) |

---

## Переключение режима

**Через переменную окружения:**

```bash
SEND_TYPE=kafka java -jar worker-service.jar
```

**Через аргумент приложения:**

```bash
java -jar worker-service.jar --send-type=kafka
```

**В `application.yaml`:**

```yaml
send-type: kafka
```

---

## Запуск

### Требования

- Docker и Docker Compose
- Внешняя Docker-сеть `crackhash-net` (создаётся один раз)
- Запущенный менеджер-сервис (`crackhash-manager-service`) в той же сети

### Создание сети

Перед первым запуском создайте внешнюю Docker-сеть, к которой подключаются все сервисы системы:

```bash
docker network create crackhash-net
```

### Запуск

```bash
docker compose up --build
```

Запускает 3 воркера:

| Контейнер        | Внешний порт |
|------------------|--------------|
| `worker-service-1` | 8086       |
| `worker-service-2` | 8087       |
| `worker-service-3` | 8088       |

Все воркеры по умолчанию запускаются в REST-режиме и обращаются к менеджеру по адресу `http://crackhash-manager-service:8085`.

### Изменение параметров запуска

Переменные окружения задаются в `docker-compose.yml` в секции `environment` каждого сервиса:

```yaml
environment:
  SEND_TYPE: rest          # rest или kafka
  MANAGER_URL: http://crackhash-manager-service:8085
  KAFKA_URL: crackhash_kafka:9092
  WORKER_NUMBER: 3
```

### Остановка

```bash
docker compose down
```

---