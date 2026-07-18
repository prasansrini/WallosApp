# Wallos Companion Android App & Deployment Guide

This repository contains the self-hosted deployment files for the **Wallos** server and the codebase for the native **Wallos Companion Android Application**.

Wallos is a powerful, self-hosted personal subscription and expense tracker. Since Wallos natively only provides a Progressive Web App (PWA), this project aims to deliver a high-performance, native Android experience utilizing the Wallos REST API.

---

## 📂 Project Structure

```text
WallosApp/
├── wallos-server/                     # Server deployment directory
│   └── docker-compose.yaml            # Docker compose configuration for Wallos
├── wallos_full_deployment_dev_plan.md # Comprehensive roadmap & app dev plan
└── README.md                          # This documentation file
```

---

## 🚀 Part 1: Wallos Server Deployment

The Wallos server runs in a Docker container using Docker Compose.

### Quick Start
1. Ensure you have [Docker Desktop](https://www.docker.com/products/docker-desktop/) or Docker Engine installed.
2. Open a terminal and navigate to the `wallos-server` directory:
   ```bash
   cd wallos-server
   ```
3. Start the container in detached mode:
   ```bash
   docker compose up -d
   ```
4. Access the web panel to create your admin account:
   * URL: [http://localhost:8282](http://localhost:8282)

### Persistent Volumes
Data is stored locally on the host machine to ensure persistence across container updates:
* `wallos-server/db/` - Contains the SQLite database (`wallos.db`)
* `wallos-server/logos/` - Stores uploaded subscription logo images

*(Note: These local data directories are ignored by Git in `.gitignore` to protect sensitive information).*

---

## 📱 Part 2: Native Android Application Roadmap

The companion Android application will be built as a native client interfacing with the Wallos REST API. 

### Tech Stack & Architecture
* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material Design 3)
* **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
* **Networking**: Retrofit2 & OkHttp
* **Local Cache & Offline Mode**: Room DB
* **Dependency Injection**: Dagger Hilt
* **Secure Storage**: EncryptedSharedPreferences (for API Key storage)

For detailed information on design patterns, module structure, and testing, see the full developer plan: [wallos_full_deployment_dev_plan.md](wallos_full_deployment_dev_plan.md).