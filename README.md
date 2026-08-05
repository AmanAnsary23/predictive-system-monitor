# 🛰️ Sentinel — Predictive System Monitoring Platform

> A real-time observability platform that predicts service failures **before they happen** — not after. Built as a microservices system using Kafka, Spring Boot, MySQL, and React.

---

## 📌 Overview

Most monitoring dashboards tell you a service is *already* down. This project takes a different approach: it continuously streams system metrics (CPU usage, DB connections, response time) from simulated microservices, processes them through a real-time pipeline, and flags anomalies **the moment they start trending toward failure** — before a full outage occurs.

Think of it as a smartwatch for your backend services: constantly checking vitals, and raising a flag the instant something looks off.

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
│  (Spring Boot)           │   runs anomaly detection,
│                          │   persists data
└──────┬──────────┬───────┘
       │          │
       ▼          ▼
   ┌───────┐  ┌─────────────┐
   │ MySQL │  │ Rule-based   │
   │  DB   │  │ Anomaly      │
   │       │  │ Detection     │
   └───┬───┘  └─────────────┘
       │
       ▼
┌────────────────────────┐
│  React Dashboard         │   REST API polling,
│  (Vite + Tailwind +      │   live radial gauge chart,
│   Recharts)               │   status-highlighted table
└────────────────────────┘
```

**Data flow:** Generator → Kafka → Consumer → MySQL + Detection → Dashboard

---

## 🧰 Tech Stack

| Layer                | Technology                                   |
|-----------------------|----------------------------------------------|
| Metric Generation      | Java, Spring Boot, `@Scheduled` tasks        |
| Messaging / Streaming  | Apache Kafka (via Docker), Zookeeper         |
| Consumer & Processing  | Java, Spring Boot, Spring Kafka              |
| Anomaly Detection      | Rule-based thresholding (Java)               |
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

## ✨ Features

- 📡 **Live metric simulation** across multiple microservices (CPU %, DB connections, response time)
- 🔄 **Event-driven pipeline** using Kafka producer/consumer architecture
- 🗄️ **Persistent storage** of every metric reading in MySQL for historical tracking
- 🚨 **Rule-based anomaly detection** — flags high CPU, DB connection saturation, or slow response times in real time
- 📊 **Live dashboard** — radial gauge chart per service + a styled, auto-refreshing table with color-coded health status ("Healthy" / "Alert" badges)
- 🧩 **True microservice separation** — generator, consumer, and dashboard are three independently runnable services communicating only through Kafka and a REST API

---

## 📸 Screenshots

**Live Dashboard — Radial Gauge + Metrics Table**

<img width="1897" height="912" alt="Screenshot 2026-08-06 022441" src="https://github.com/user-attachments/assets/1a644139-42de-4fb8-bbcf-41bb0c87e201" />

**Metrics Table — Anomaly Highlighting**

<img width="1901" height="911" alt="Screenshot 2026-08-06 024321" src="https://github.com/user-attachments/assets/cfb73146-58c7-4508-b516-09b7d7ae79a8" />

> *(Place your two screenshots inside a `screenshots/` folder in the repo root, named `dashboard.png` and `table.png`, so these render correctly on GitHub.)*

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven
- Node.js 18+
- Docker Desktop
- MySQL (local or containerized)

### 1. Start Kafka & Zookeeper (Docker)
```bash
docker compose up -d
```

### 2. Create the database
```sql
CREATE DATABASE system_predector;
```

### 3. Run the Metrics Generator
```bash
cd metrics-generator-service
mvnw spring-boot:run
```

### 4. Run the Metrics Consumer
```bash
cd metrics-consumer-service
mvnw spring-boot:run
```

### 5. Run the Dashboard
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

---

## 🔭 Future Improvements

- Replace rule-based thresholds with a lightweight ML model (e.g., Isolation Forest) for smarter anomaly detection
- Add Locust-based load simulation to demonstrate behavior under realistic traffic spikes
- Batch database writes for higher-throughput scenarios
- WebSocket-based live push to the dashboard instead of polling
- Dockerize all three services for one-command startup

---

## 👤 Author

**Aman Ansary**
Associate SDET @ Mechlin Technologies | Backend SDE aspirant
