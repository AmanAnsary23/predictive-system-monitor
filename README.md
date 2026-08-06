# 🛰️ Sentinel — Predictive System Monitoring Platform

> A real-time observability platform that predicts service failures **before they happen** — not after. Built as a microservices system using Kafka, Spring Boot, MySQL, and React.

---

## 📌 Overview

Most monitoring dashboards tell you a service is *already* down. This project takes a different approach: it continuously streams system metrics (CPU usage, DB connections, response time) from simulated microservices, processes them through a real-time pipeline, and flags anomalies **the moment they start trending toward failure** — before a full outage occurs.

Detection isn't a single hardcoded rule — it's a **hybrid engine** combining statistical outlier analysis, rule-based safety limits, and a trained **machine learning model (Isolation Forest)** served via a lightweight Python microservice.

Think of it as a smartwatch for your backend services: constantly checking vitals, and raising a flag the instant something looks off — including patterns a human-written `if` statement would never catch.

---

## 🏗️ Architecture

```
┌────────────────────────┐
│  Metrics Generator      │   Simulates real system metrics
│  (Spring Boot)          │   for order/payment/inventory
│                          │   services every few seconds
└────────────┬─────────────┘
             │  produces
             ▼
      ┌─────────────┐
      │   Apache     │   Running in Docker
      │   Kafka      │   Topic: system-metrics
      └──────┬──────┘
             │  consumes
             ▼
┌────────────────────────┐
│  Metrics Consumer        │   Listens to Kafka topic,
│  (Spring Boot)           │   orchestrates detection,
│                          │   persists data
└──────┬──────────┬───────┘
       │          │
       ▼          ▼
   ┌───────┐  ┌──────────────────────┐
   │ MySQL │  │  Hybrid Detection    │
   │  DB   │  │  Engine              │
   │       │  │ 1. Statistical       │
   │       │  │   (moving avg/stdev) │
   │       │  │ 2.Rule-based limits  │
   │       │  │3.ML model (HTTP call)│
   │       │  └──────────┬───────────┘
   │       │             │
   │       │             ▼
   │       │   ┌────────────────────┐
   │       │   │ ML Microservice    │  Python + Flask
   │       │   │ Isolation Forest   │  trained on MySQL
   │       │   │ (/predict endpoint)│  historical data
   │       │   └────────────────────┘
   │       │
   └───┬───┘
       │
       ▼
┌────────────────────────┐
│  React Dashboard       │   REST API polling,
│  (Vite + Tailwind +    │   live radial gauge chart,
│   Recharts)            │   status-highlighted table
└────────────────────────┘
```

**Data flow:** Generator → Kafka → Consumer → (MySQL + Statistical/Rule/ML Detection) → Dashboard

---

## 🧰 Tech Stack

| Layer                | Technology                                   |
|-----------------------|----------------------------------------------|
| Metric Generation      | Java, Spring Boot, `@Scheduled` tasks        |
| Messaging / Streaming  | Apache Kafka (via Docker), Zookeeper         |
| Consumer & Processing  | Java, Spring Boot, Spring Kafka              |
| Anomaly Detection      | Hybrid: statistical thresholding (Java) + rule-based limits + ML model (Python) |
| Machine Learning        | Python, scikit-learn (Isolation Forest), Flask, pandas |
| Persistence            | MySQL, Spring Data JPA / Hibernate           |
| Frontend               | React (Vite), Tailwind CSS, Recharts         |
| Infra / Containers     | Docker, Docker Compose                       |
| Serialization          | Jackson (with `jackson-datatype-jsr310`)     |

---

## ⚙️ How Kafka + Docker Is Used

- **Kafka and Zookeeper run inside Docker containers**, fully decoupled from the application code — this mirrors how message brokers are deployed in real production environments.
- The **Metrics Generator acts as a Kafka producer**, publishing a `SystemMetric` JSON payload to the `system-metrics` topic every 5 seconds, keyed by service name (`order-service`, `payment-service`, `inventory-service`).
- The **Metrics Consumer acts as a Kafka consumer** in a dedicated consumer group (`metrics-consumer-group`), reading from the same topic and processing each message independently of the producer — meaning the generator and consumer can be scaled, restarted, or deployed separately without breaking the pipeline.
- Kafka's **pub-sub model** decouples the two services completely: the generator has no knowledge of who (or how many services) consume its data, which is the core principle behind event-driven microservices.
- Producer/consumer configs were manually defined as Spring `@Bean`s (`KafkaTemplate`, `ProducerFactory`, `ConsumerFactory`) for full control over serialization, rather than relying purely on Spring Boot auto-configuration.
- JSON serialization required explicitly registering a `JavaTimeModule` with Jackson's `ObjectMapper` to correctly handle `LocalDateTime` fields across the wire — a common but easy-to-miss gap in Kafka + Spring Boot setups.

---

## 🧠 How the ML-Based Detection Works

Rule-based thresholds (`CPU > 85%`, `DB connections > 90`) catch the obvious cases, but they miss **combined patterns** — a service can look "fine" on every individual metric while its overall behavior is still abnormal. To catch that, a third detection layer was added:

- A **Python microservice** (Flask) trains an **Isolation Forest** model — an unsupervised ML algorithm that doesn't need labeled "anomaly" data. It's shown historical metrics (CPU usage, DB connections, response time together) pulled directly from the MySQL table the consumer writes to, and it learns what a "normal" combination of these three values looks like for the system as a whole.
- Isolation Forest works by trying to isolate each data point through random splits — points that are easy to isolate (i.e., different from the rest) are flagged as anomalies. No thresholds are hardcoded; the model derives "normal" purely from the data it's shown.
- The model is exposed via a single `/predict` REST endpoint. On every incoming Kafka message, the Java consumer sends the metric's three values to this endpoint and receives back `{"isAnomaly": true/false}`.
- The final anomaly decision is the logical OR of **all three layers**: statistical deviation, rule-based hard limits, and the ML model's prediction — so a metric only needs to look wrong to *one* detector to be flagged.
- **Fail-safe by design:** if the Python service is unreachable, the Java consumer catches the exception, logs it, and simply treats that layer as "no anomaly" for that reading rather than crashing — the statistical and rule-based layers keep running independently.

---

- 📡 **Live metric simulation** across multiple microservices (CPU %, DB connections, response time)
- 🔄 **Event-driven pipeline** using Kafka producer/consumer architecture
- 🗄️ **Persistent storage** of every metric reading in MySQL for historical tracking
- 🚨 **Hybrid anomaly detection** — statistical outlier analysis, rule-based safety limits, and a trained ML model (Isolation Forest) served over REST, combined into a single decision
- 📊 **Live dashboard** — radial gauge chart per service + a styled, auto-refreshing table with color-coded health status ("Healthy" / "Alert" badges)
- 🧩 **True microservice separation** — generator, consumer, ML service, and dashboard are four independently runnable services communicating only through Kafka and REST APIs

---

## 📸 Screenshots

**Live Dashboard — Radial Gauge + Metrics Table**

<img width="1897" height="912" alt="Screenshot 2026-08-06 022441" src="https://github.com/user-attachments/assets/ee8a27a1-3a99-44fc-b073-ba1a950d61f9" />

**Metrics Table — Anomaly Highlighting**

<img width="1901" height="911" alt="Screenshot 2026-08-06 024321" src="https://github.com/user-attachments/assets/4c42e8a7-e92d-4bc4-a717-eaa211cbc942" />

---

## 🚀 Getting Started

### Prerequisites
- Java 21+ & Maven (for backend services)
- Node.js 18+ (for the React dashboard)
- Python 3.10+ & pip (for the ML anomaly detection service)
- Docker Desktop (for Kafka & Zookeeper)
- MySQL (local or containerized)

### 1. Start Kafka & Zookeeper (Docker)
```bash
docker compose up -d
```

### 2. Create the database
```sql
CREATE DATABASE system_predector;
```

### 3. Configure environment variables
Each backend service reads DB credentials from environment variables (with safe local defaults), so no secrets are committed to the repo. Copy the example file and fill in your own values:
```bash
cp .env.example .env
```
```
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
```

### 4. Run the Metrics Generator
```bash
cd metrics-generator-service
mvnw spring-boot:run
```

### 5. Run the Metrics Consumer
```bash
cd metrics-consumer-service
mvnw spring-boot:run
```

### 6. Run the ML Anomaly Detection Service
```bash
cd anomaly-ml-service
pip install -r requirements.txt
python app.py
```
> The service trains an Isolation Forest model on existing MySQL data at startup, then serves predictions at `http://localhost:5000/predict`. It should be started after some metrics already exist in the database (run the generator/consumer for a bit first). If this service is offline, the consumer still runs fine — it just falls back to statistical and rule-based detection.

### 7. Run the Dashboard
```bash
cd metrics-dashboard
npm install
npm run dev
```

Visit `http://localhost:5173` to view the live dashboard.

---

## 🧗 Challenges Faced & What I Learned

Building this end-to-end taught me far more than following a tutorial ever could — most of the real learning came from debugging:

- **Spring Boot auto-configuration isn't magic** — when `KafkaTemplate` wasn't auto-wiring correctly, I learned to manually define Kafka beans (`ProducerFactory`, `ConsumerFactory`, `KafkaTemplate`) instead of depending blindly on auto-config, and to actually understand *why* a bean does or doesn't get created.
- **Serialization is never "just JSON"** — hit real `SerializationException` and `ClassNotFoundException` errors around Jackson and `LocalDateTime`, which pushed me to understand how Kafka serializers/deserializers actually work under the hood, and how producer/consumer type headers need to match across services.
- **Debugging via Spring's Condition Evaluation Report** — learned to read `--debug` output and "Positive/Negative matches" to diagnose *why* Spring did or didn't wire a bean, instead of guessing.
- **Microservice boundaries are a design decision, not a default** — decided which pieces (generator, consumer, dashboard) deserved to be separate services vs. which logic (like anomaly detection) belonged inside an existing service, rather than over-fragmenting everything.
- **Trade-offs matter more than "correct" answers** — e.g., understanding that per-message DB inserts are fine at small scale, but batching would be the right call at higher throughput — and being able to articulate *why* in an interview setting.
- **Docker as infrastructure, not magic** — running Kafka/Zookeeper/MySQL as containers and understanding port mapping, container lifecycle, and how services outside Docker connect to services inside it.
- **First real machine learning implementation** — with no prior ML background, learned the fundamentals of unsupervised anomaly detection through Isolation Forest, trained it on real pipeline data pulled from MySQL, and exposed it as a REST API that a completely different language (Java) could call — a practical introduction to polyglot, ML-integrated microservices.
- **Designing for graceful degradation** — deliberately wrapped the ML API call in a try/catch that fails safe rather than fails loud, so a Python service outage never brings down the core Java pipeline — a small design choice that mirrors how real distributed systems handle partial failures.

---

## 🔭 Future Improvements

- Add Locust-based load simulation to demonstrate behavior under realistic traffic spikes
- Batch database writes for higher-throughput scenarios
- Periodically retrain the ML model on fresh data instead of only at service startup
- WebSocket-based live push to the dashboard instead of polling
- Dockerize all four services for one-command startup

---

## 👤 Author

**Aman Ansary**
Backend Developer | Aspiring SDE — building production-style systems with Java, Spring Boot, and Kafka
