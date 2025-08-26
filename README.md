# Contextual News API

A Spring Boot 3.5.5 application that provides REST APIs for managing contextual news articles.  
The project integrates **MongoDB**, **Swagger (OpenAPI 3)**, and **WebClient** (for calling external LLM/OpenAI APIs).  
It also includes **Global Exception Handling** for clean error responses.

---

## 🚀 Features

- **Spring Boot 3.5.5** – latest stable boot version
- **REST API** – CRUD operations for News Articles
- **MongoDB** – document database integration
- **Spring Validation** – request validation with `@Valid`
- **Global Exception Handling** – centralized error responses
- **Swagger (OpenAPI 3)** – interactive API documentation
- **WebClient** – reactive HTTP client for calling external APIs (OpenLLM, external services)
- **Lombok** – reduces boilerplate with annotations
- **SLF4J Logging** – structured logs

---

## 📦 Tech Stack

- **Java**: 21
- **Spring Boot**: 3.5.5
- **MongoDB**: NoSQL database
- **Swagger (springdoc-openapi)**: 2.8.11
- **Maven**: Build tool

---

## 📖 API Documentation (Swagger)
Swagger UI is enabled for interactive documentation.

Open in browser:
👉 http://localhost:8080/swagger-ui/index.html

OpenAPI spec (JSON):
👉 http://localhost:8080/v3/api-docs

## 📂 Upload News Data (File Upload)
curl -F "file=@news_data.json" http://localhost:8080/api/v1/admin/load
