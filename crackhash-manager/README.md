# CrackHash Manager

Сервис-оркестратор распределённого подбора хешей. Принимает запросы на взлом хешей от клиентов через REST API, распределяет задачи между воркерами, собирает и агрегирует результаты.

## Схема сервиса

![Схема сервиса](schema/service-schema.jpeg)

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
| `CLUSTER_CONFIG_TYPE` | `FILE` | Тип конфигурации кластера: `ENV` или `FILE` |
| `WORKER_NUMBER` | `1` | Количество воркеров в режиме env |
| `WORKER_URIS` | `localhost:8086` | Адреса воркеров через запятую, каждый со своим портом; схема по умолчанию `http://` |
| `CLUSTER_INFO_PATH` | `./config/clusterInfo.yaml` | Путь к YAML в режиме `FILE` |
| `KAFKA_URL`      | `localhost:9092`        | Адрес Kafka-брокера                      |
| `MONGO_DB_HOST`  | `localhost`             | Хост MongoDB                             |
| `MONGO_DB_PORT`  | `27017`                 | Порт MongoDB                             |

### Параметры в `application.yaml`

```yaml
server:
  port: 8085                          # Порт сервиса

send-type: ${SEND_TYPE:rest}          # Режим отправки задач

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

### Два способа настройки воркеров

`ClusterConfiguration` создаёт один бин `ClusterConfig` через `@Bean`.
Сам `ClusterConfig` — объект с `Map<String, WorkerConfig>`, без `@ConfigurationProperties`.
Переменная `CLUSTER_CONFIG_TYPE` задаёт свойство `cluster.config-type` в `application.yaml`.
Spring связывает его с enum `ClusterConfigType` в `ClusterProperties` через
`@ConfigurationProperties(prefix = "cluster")`. Метод создания бина выбирает
ветку `ENV` или `FILE` по этому значению. Неизвестный тип останавливает запуск.

По умолчанию данные берутся из файла (`FILE`). В режиме `ENV` YAML-файл не читается и не требуется.
Пример для одного воркера в Kubernetes (добавьте в `env` контейнера менеджера):

```yaml
env:
  - name: CLUSTER_CONFIG_TYPE
    value: ENV
  - name: WORKER_NUMBER
    value: "1"
  - name: WORKER_URIS
    value: "10.100.123.143:80"
```

Получится `worker-1` с URL `http://10.100.123.143:80`. Указывайте порт Service,
доступный менеджеру. Для нескольких воркеров задайте адреса через запятую и соответствующее
количество; несовпадение останавливает запуск. Например:

```env
CLUSTER_CONFIG_TYPE=ENV
WORKER_NUMBER=3
WORKER_URIS=10.100.123.143:80,http://worker-2:8087,https://worker-3:8443
```

Каждый адрес задаёт собственный порт. Можно передать `host:port` или полный HTTP/HTTPS URL.
Отдельная переменная `WORKER_PORT` больше не используется.
Для переключения с файла на env задайте `CLUSTER_CONFIG_TYPE=ENV`.

### Список воркеров из файла

Задайте `CLUSTER_CONFIG_TYPE=FILE`. Файл `config/clusterInfo.yaml` имеет прежний формат:

```yaml
cluster:
  workers:
    worker-1:
      url: http://localhost:8086
    "42":
      url: http://localhost:8087
```

В ветке `FILE` конфигурационный класс читает YAML через `YamlPropertySourceLoader`,
связывает `cluster.workers` с мапой через Spring Binder и создаёт `ClusterConfig`.
Остальные настройки приложения из этого файла не импортируются.
Ключ — строковое имя или ID,
значение — расширяемый класс `WorkerConfig` с полем `url`. Число частей задачи
вычисляется по размеру мапы и в REST-, и в Kafka-режиме. В Kafka URL не используется
для отправки: задачи по-прежнему передаются через топик.

Используйте простые ID из букв, цифр и дефисов; числовые ID заключайте в кавычки.
Для ключей со специальными символами используйте синтаксис Spring, например `"[worker/east]"`.
Файл обязателен: отсутствие файла, пустая мапа или пустой URL останавливают запуск.
В этом режиме `WORKER_NUMBER` и `WORKER_URIS` не используются; URL каждого воркера берётся из YAML.

Путь задаётся без префикса `file:`. Относительный путь считается от рабочего каталога процесса.
Пример запуска в PowerShell:

```powershell
$env:CLUSTER_CONFIG_TYPE = 'FILE'
$env:CLUSTER_INFO_PATH = 'C:/configs/clusterInfo.yaml'
java -jar build/libs/manager-service-0.0.1-SNAPSHOT.jar
```

Dockerfile включает файл в образ по пути `/app/config/clusterInfo.yaml`.
Docker Compose задаёт `CLUSTER_CONFIG_TYPE=FILE` и этот абсолютный путь.
При изменении встроенного файла пересоберите образ и обновите контейнер.
Для Kubernetes с файлом внутри образа задайте:

```yaml
env:
  - name: CLUSTER_CONFIG_TYPE
    value: FILE
  - name: CLUSTER_INFO_PATH
    value: /app/config/clusterInfo.yaml
```

### Хранение в Kubernetes

Храните конфиги окружений в Git рядом с манифестами развёртывания (при необходимости
в отдельном инфраструктурном репозитории). В кластере создавайте из файла ConfigMap
и монтируйте её как каталог. В URL указывайте стабильные DNS-имена Service воркеров,
а не IP Pod. При переходе на этот вариант файл из ConfigMap будет использоваться
вместо встроенного в образ.

Создать или обновить ConfigMap из подготовленного для Kubernetes файла:

```bash
kubectl create configmap crackhash-cluster --from-file=clusterInfo.yaml=./config/clusterInfo.yaml --dry-run=client -o yaml | kubectl apply -f -
```

Фрагмент Deployment (добавьте к остальным настройкам контейнера менеджера):

```yaml
spec:
  template:
    spec:
      containers:
        - name: crackhash-manager
          env:
            - name: CLUSTER_CONFIG_TYPE
              value: FILE
            - name: CLUSTER_INFO_PATH
              value: /etc/crackhash/clusterInfo.yaml
          volumeMounts:
            - name: cluster-info
              mountPath: /etc/crackhash
              readOnly: true
      volumes:
        - name: cluster-info
          configMap:
            name: crackhash-cluster
```

После обновления ConfigMap перезапустите Deployment (подставьте его имя и namespace):

```bash
kubectl rollout restart deployment/crackhash-manager
kubectl rollout status deployment/crackhash-manager
```

Spring читает конфиг при старте: новые Pod получат актуальный файл без пересборки JAR
и образа. Изменение только ConfigMap не запускает rollout; повторный `apply` неизменного
Deployment тоже не перезапустит Pod. Для автоматизации можно менять checksum-аннотацию
в `spec.template.metadata.annotations` средствами Helm при изменении конфига.
Горячее перечитывание в работающем приложении не предусмотрено.

Документация: [внешняя конфигурация Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html),
[обновление ConfigMap](https://kubernetes.io/docs/tutorials/configuration/updating-configuration-via-a-configmap/).

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
