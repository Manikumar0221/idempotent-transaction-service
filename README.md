# Idempotent Background Transaction Processing Service (Spring Boot 3 / Java 21)

Production-aware background transaction processing service built with **Spring Boot 3.2+** and **Java 21**, designed to safely process asynchronous transactions featuring idempotency, duplicate detection, out-of-order sequence buffering, and transient retry resilience.

## Core Features
- **Idempotency & Deduplication Engine**: Prevents double-processing using unique transaction/request key constraints.
- **Out-of-Order Engine**: Sequence validation that automatically buffers out-of-sequence events in `PENDING_ORDER` and drains them sequentially upon missing item arrival.
- **Resilience & Retries**: Automatic safe handling and state recovery for transient processing failures.
- **Observability**: Execution reports, balance tracking, and metrics endpoint `/api/v1/observability/summary`.
- **H2 Interactive Database Console**: Web interface for live database record inspection.

## Running the Application
```bash
mvn spring-boot:run