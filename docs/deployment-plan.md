# Deployment Plan – Smart Library Management System

## 1. Overview

This document describes how to deploy the Smart Library Management System in both development and production environments. The application consists of three main components:

- **Spring Boot Backend** – runs on Java 17, serves the web interface and REST API.
- **MySQL Database** – stores all application data.
- **Python AI Microservice** – provides book recommendations (optional, but recommended).

---

## 2. Prerequisites

Before deploying, ensure the following are installed on the target machine:

- **Java 17** (or higher)
- **Maven 3.8+**
- **MySQL 8.0** (or higher)
- **Python 3.10+** (for the AI service)
- **Git** (to clone the repository)
- **Optional:** Docker (for containerized deployment)

---

## 3. Local Development Setup

### 3.1 Clone the Repository
```bash
git clone https://github.com/leekim-wq/library-management.git
cd library-management