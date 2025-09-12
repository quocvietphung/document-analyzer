# 📄 Document API

**Document API** is a backend service built with **Java 21** and **Spring Boot 3**.  
It provides RESTful APIs to manage **users** and **documents**, including creating accounts, logging in, and uploading, viewing, and deleting documents.

---

## ✨ Features

### User Management
- Create new users
- User login with email and password
- Update and retrieve user details
- List all registered users

### Document Management
- Upload documents (PDF, DOCX, JPG, PNG, etc.)
- View and download documents
- Delete documents
- Retrieve all documents for a specific user

---

## 🛠️ Tech Stack
- Java 21
- Spring Boot 3
- Spring Web (REST API)
- Spring Data JPA
- Jakarta Validation
- Maven
- (Optional) Docker & Docker Compose

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven 3.x
- (Optional) Docker

### Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Application runs at: [http://localhost:8080](http://localhost:8080)

---

## 📌 API Endpoints

### User API
| Method | Endpoint               | Description               |
|--------|------------------------|---------------------------|
| POST   | `/api/users/create`    | Create a new user         |
| POST   | `/api/users/login`     | Login with email/password |
| GET    | `/api/users`           | List all users            |
| GET    | `/api/users/{id}`      | Get user by ID            |
| PUT    | `/api/users/{id}`      | Update user details       |

### Document API
| Method | Endpoint                                     | Description                   |
|--------|----------------------------------------------|-------------------------------|
| POST   | `/api/documents/upload`                      | Upload a document             |
| GET    | `/api/documents/view?documentId=...&userId=...` | View or download a document   |
| DELETE | `/api/documents/delete?documentId=...&userId=...` | Delete a document             |
| GET    | `/api/documents/getUserDocuments?userId=...` | List all documents of a user  |

---