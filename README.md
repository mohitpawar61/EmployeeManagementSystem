# Employee Management System

A backend REST API for managing employee records, built with **Spring Boot**, **Spring Data JPA**, and **MySQL**. It exposes endpoints to create, read, update, and paginate employee data, including profile picture uploads.

> **Note:** This repository currently contains only the **backend (Spring Boot REST API)**. There is no frontend code in the repo, though the API is pre-configured (via `@CrossOrigin`) to accept requests from an Angular app running on `http://localhost:4200`, suggesting an Angular frontend is intended to consume this API (not included here).

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Setup & Installation](#setup--installation)
- [Running the Project](#running-the-project)
- [Configuration Notes / Known Issues](#configuration-notes--known-issues)
- [Possible Improvements](#possible-improvements)

---

## Overview

The Employee Management System (EMS) lets an organization store and manage employee records — name, gender, email, salary, address, mobile number, department, date of birth, and a profile picture. Salary is **auto-assigned by the backend** based on the employee's department (see [How It Works](#how-it-works)).

Typical use cases:
- HR/admin tools for adding and viewing employee records
- Learning project for Spring Boot + Spring Data JPA + REST API fundamentals
- Base backend to plug into a frontend (Angular/React) for a full-stack HR dashboard

---

## Architecture

The project follows a classic **layered (N-tier) Spring Boot architecture**:

```
┌─────────────────────────────┐
│        Client (Angular/      │   → not included in this repo
│      Postman/any REST client)│
└──────────────┬───────────────┘
               │ HTTP (JSON + multipart/form-data)
┌──────────────▼───────────────┐
│   EmployeeRestController      │   REST layer (@RestController)
│   /employee/**                │   - Handles HTTP requests/responses
└──────────────┬───────────────┘   - File upload for profile pictures
               │
┌──────────────▼───────────────┐
│   EmployeeService (interface) │   Service layer
│   EmployeeServiceImpl         │   - Business logic (salary rules)
└──────────────┬───────────────┘   - Delegates persistence to repo
               │
┌──────────────▼───────────────┐
│   EmployeeRepo                │   Repository layer (Spring Data JPA)
│   extends JpaRepository       │   - CRUD + pagination out of the box
└──────────────┬───────────────┘
               │ JDBC / Hibernate ORM
┌──────────────▼───────────────┐
│   MySQL Database               │   Persistence layer
│   Table: employees             │
└───────────────────────────────┘
```

**Layers:**
1. **Entity layer** (`entity/Employee.java`) — JPA entity mapped to the `employees` table via Hibernate.
2. **Repository layer** (`repository/EmployeeRepo.java`) — Spring Data JPA interface providing CRUD + pagination + a custom `existsByEmail` query, with no manual SQL needed.
3. **Service layer** (`service/EmployeeService.java` + `service/impl/EmployeeServiceImpl.java`) — business logic, including automatic salary assignment and profile picture file handling.
4. **REST/Controller layer** (`rest/EmployeeRestController.java`) — exposes HTTP endpoints under `/employee`, handles JSON + file uploads, returns structured JSON responses.

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring Web MVC (`spring-boot-starter-webmvc`) |
| Persistence | Spring Data JPA (Hibernate ORM) |
| Database | MySQL (via `mysql-connector-j`) |
| Boilerplate reduction | Lombok (`@Data` for getters/setters/etc.) |
| Build tool | Maven (with Maven Wrapper `mvnw`/`mvnw.cmd`) |
| Dev tooling | Spring Boot DevTools (hot reload) |
| Testing | Spring Boot Test starter (JUnit 5) |
| Intended frontend | Angular (implied by CORS config on port 4200) — not included |

---

## Project Structure

```
EmployeeManagementSystem/
├── pom.xml                                  # Maven build & dependency config
├── mvnw / mvnw.cmd                          # Maven wrapper scripts
└── src/
    ├── main/
    │   ├── java/com/employeeapp/
    │   │   ├── EmployeeAppApplication.java  # Spring Boot entry point (main method)
    │   │   ├── entity/
    │   │   │   └── Employee.java            # JPA entity / DB table mapping
    │   │   ├── repository/
    │   │   │   └── EmployeeRepo.java        # Spring Data JPA repository
    │   │   ├── service/
    │   │   │   ├── EmployeeService.java     # Service interface (contract)
    │   │   │   └── impl/
    │   │   │       └── EmployeeServiceImpl.java # Business logic implementation
    │   │   └── rest/
    │   │       └── EmployeeRestController.java  # REST API endpoints
    │   └── resources/
    │       └── application.properties       # DB connection + app config
    └── test/
        └── java/com/employeeapp/
            └── EmployeeAppApplicationTests.java # Spring context load test
```

---

## How It Works

1. **Startup** — `EmployeeAppApplication` boots the Spring context via `@SpringBootApplication`, which triggers component scanning, auto-configuration, and embedded Tomcat server startup (default port `8080`).

2. **Creating an employee (`POST /employee/save`)**
   - Client sends a `multipart/form-data` request with employee fields (`name`, `gender`, `email`, `address`, `mobileNo`, `dep`, `dob`) plus a `file` (profile picture).
   - The controller saves the uploaded file to a local folder (currently hardcoded to `D:\mohit\RESUME\EmployeeProfiles\`) with a timestamp prefixed to the filename to avoid collisions.
   - It checks if the email already exists (`existsByEmail`) — if so, returns a `400 Bad Request` with an error message.
   - Otherwise, it calls `employeeService.saveEmployee(emp)`.

3. **Automatic salary assignment (business rule in `EmployeeServiceImpl`)**
   Salary is *not* provided by the client — it's derived from the `dep` (department) field:
   | Department | Salary |
   |---|---|
   | IT | ₹45,000 |
   | HR | ₹30,000 |
   | Finance | ₹25,000 |
   | Marketing | ₹22,000 |
   | Any other | ₹15,000 |

4. **Reading employees**
   - `GET /employee/all?page=0&size=5` — returns a paginated list (Spring Data `Page<Employee>`, default page 0, size 5).
   - `GET /employee/{id}` — returns a single employee by ID.
   - `GET /employee/get/{fileName}` — streams back the profile picture file stored on disk.

5. **Updating an employee (`PUT /employee/update/{id}`)**
   - Fetches the existing employee by ID, overwrites its fields with the new values.
   - If a new file is provided, it's saved to disk and the `profilePicture` field is updated; otherwise the old picture reference remains (though the current code will throw a `NullPointerException` if `file` is omitted — see [Known Issues](#configuration-notes--known-issues)).
   - Note: unlike `saveEmployee`, `updateEmployee` does **not** recalculate salary based on department.

6. **Persistence** — Hibernate (via `spring.jpa.hibernate.ddl-auto=update`) automatically creates/updates the `employees` table schema in MySQL based on the `Employee` entity, so no manual SQL/migration scripts are needed for basic use.

---

## API Endpoints

Base path: `/employee` (CORS enabled only for `http://localhost:4200`)

| Method | Endpoint | Description | Body / Params |
|---|---|---|---|
| `GET` | `/employee/all?page={n}&size={n}` | Paginated list of all employees | Query params: `page` (default 0), `size` (default 5) |
| `POST` | `/employee/save` | Create a new employee | `multipart/form-data`: `name`, `gender`, `email`, `address`, `mobileNo`, `dep`, `dob`, `file` |
| `GET` | `/employee/{id}` | Get one employee by ID | Path param: `id` |
| `GET` | `/employee/get/{fileName}` | Retrieve a stored profile picture | Path param: `fileName` |
| `PUT` | `/employee/update/{id}` | Update an existing employee | Path param: `id`; `multipart/form-data`: same fields as `/save`, `file` optional |

**Example — create an employee (cURL):**
```bash
curl -X POST http://localhost:8080/employee/save \
  -F "name=John Doe" \
  -F "gender=Male" \
  -F "email=john@example.com" \
  -F "address=123 Main St" \
  -F "mobileNo=9876543210" \
  -F "dep=IT" \
  -F "dob=1995-05-10" \
  -F "file=@/path/to/photo.jpg"
```

**Example — list employees:**
```bash
curl "http://localhost:8080/employee/all?page=0&size=10"
```

---

## Database Schema

Table: `employees` (auto-generated by Hibernate from the `Employee` entity)

| Column | Type | Constraints |
|---|---|---|
| `id` | INT | Primary key, auto-increment |
| `name` | VARCHAR | |
| `gender` | VARCHAR | |
| `email` | VARCHAR | Unique |
| `salary` | DOUBLE | Set automatically by backend logic |
| `address` | VARCHAR | |
| `mobile_no` | VARCHAR | |
| `dep` | VARCHAR | Department |
| `dob` | VARCHAR | Date of birth (stored as string) |
| `profile_picture` | VARCHAR | Filename of the uploaded picture |

---

## Setup & Installation

### Prerequisites
- **Java 25 JDK** installed and on your `PATH`
- **MySQL Server** installed and running
- **Maven** (or just use the included `mvnw` wrapper — no separate Maven install needed)
- An API client for testing (Postman, cURL, or a frontend app)

### 1. Clone the repository
```bash
git clone https://github.com/mohitpawar61/EmployeeManagementSystem.git
cd EmployeeManagementSystem
```

### 2. Create the MySQL database
```sql
CREATE DATABASE EmployeeApp;
```
(Hibernate will auto-create the `employees` table on first run because `spring.jpa.hibernate.ddl-auto=update`.)

### 3. Configure database credentials
Edit `src/main/resources/application.properties` to match your local MySQL setup:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/EmployeeApp
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 4. Fix the file upload path (important)
The profile picture upload directory is currently **hardcoded** to a Windows path:
```java
private final String uploadProfilePicturesPath = "D:\\mohit\\RESUME\\EmployeeProfiles\\";
```
This appears in **two files**:
- `EmployeeServiceImpl.java`
- `EmployeeRestController.java` (in two places: save and get)

Before running, change this to a folder that exists on your machine, e.g.:
```java
private final String uploadProfilePicturesPath = "C:\\uploads\\EmployeeProfiles\\";
// or on Linux/Mac:
private final String uploadProfilePicturesPath = "/home/youruser/uploads/EmployeeProfiles/";
```
Make sure the folder actually exists (create it manually) — the code does not create it automatically.

---

## Running the Project

### Option A — Using Maven Wrapper (recommended, no local Maven needed)
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Option B — Using a local Maven install
```bash
mvn spring-boot:run
```

### Option C — Build a JAR and run it
```bash
./mvnw clean package
java -jar target/Employee-App-0.0.1-SNAPSHOT.jar
```

The application starts on the default Spring Boot port:
```
http://localhost:8080
```

You can now hit the endpoints listed above using Postman, cURL, or a frontend app running on `http://localhost:4200` (the only origin currently allowed by CORS).

---

## Configuration Notes / Known Issues

These are things worth knowing/fixing if you build on this project:

- **Hardcoded Windows file path** — `D:\mohit\RESUME\EmployeeProfiles\` will fail on any machine that doesn't have that exact drive/folder. Should be moved into `application.properties` as a configurable property.
- **Plaintext DB credentials** — the MySQL password is committed directly in `application.properties`. For anything beyond local learning, use environment variables or a secrets manager.
- **No frontend included** — the CORS policy (`http://localhost:4200`) implies an Angular app is meant to consume this API, but it isn't part of this repository.
- **`updateEmployee` requires a file** — if `file` is `null` (not provided) on update, the code will throw a `NullPointerException` when calling `file.getOriginalFilename()`. The controller declares the file param as optional, but the service doesn't null-check it.
- **No validation** — fields like `email`, `mobileNo`, and `dob` aren't validated (e.g., no `@Email`, `@Pattern`, or `@NotBlank` annotations), so malformed data can be saved.
- **No authentication/authorization** — all endpoints are open; there's no login, roles, or access control.
- **`dob` stored as `String`** — using `LocalDate` would allow proper date validation and querying.
- **Salary logic is hardcoded** — department-to-salary mapping lives directly in Java code rather than a configurable table, so changing pay bands requires a code change and redeploy.

---

## Possible Improvements

- Externalize the file storage path and DB credentials into `application.properties` / environment variables.
- Add Bean Validation (`@NotBlank`, `@Email`, `@Pattern`) on the `Employee` entity/DTOs.
- Add a `DELETE /employee/{id}` endpoint (currently missing — no way to delete a record via the API).
- Add Spring Security for authentication/authorization (e.g., JWT-based).
- Add a DTO layer to avoid exposing the JPA entity directly in the REST layer.
- Add global exception handling via `@ControllerAdvice` instead of try/catch blocks scattered across the controller.
- Store files in cloud storage (e.g., AWS S3) instead of the local filesystem for portability.
- Build/attach the intended Angular frontend to complete the full-stack application.
- Recalculate salary consistently in `updateEmployee` (currently only `saveEmployee` applies the salary rule).
