# TODO API

REST API for task management built with Spring Boot 3.4.0 and Java 21.

## Features

- CRUD operations for tasks
- Mark tasks as completed or pending
- Search tasks by keyword
- Filter by completion status
- Task statistics endpoint
- Input validation
- Exception handling
- Full test coverage

## Tech Stack

- Java 21
- Spring Boot 3.4.0
- Spring Data JPA
- H2 Database
- Lombok
- JUnit 5 & Mockito
- Maven

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.6+

### Running the application

```bash
git clone https://github.com/hnrdejesus/todo-api.git
cd todo-api
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

You'll see a startup banner with all available endpoints and access information.

### Quick Access

Once running, you can access:

- **API Base**: http://localhost:8080/api/tasks
- **H2 Console**: http://localhost:8080/h2-console
    - JDBC URL: `jdbc:h2:mem:testdb`
    - Username: `sa`
    - Password: (empty)

## API Endpoints

### Task Operations

```
GET    /api/tasks                      - List all tasks
GET    /api/tasks?completed=true       - Filter by status
GET    /api/tasks/{id}                 - Get task by ID
GET    /api/tasks/search?keyword=text  - Search tasks
GET    /api/tasks/stats                - Get statistics
POST   /api/tasks                      - Create task
PUT    /api/tasks/{id}                 - Update task
PATCH  /api/tasks/{id}/toggle          - Toggle completion
DELETE /api/tasks/{id}                 - Delete task
```

### Example Request

Create a task:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot",
    "description": "Complete tutorial"
  }'
```

Response:

```json
{
  "id": 1,
  "title": "Learn Spring Boot",
  "description": "Complete tutorial",
  "completed": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## Validation Rules

- Title: required, 3-100 characters
- Description: optional, max 500 characters
- Completed: required for updates

## Error Responses

The API returns standard HTTP status codes:

- 200: Success
- 201: Created
- 204: No Content
- 400: Bad Request (validation error)
- 404: Not Found
- 409: Conflict (duplicate title)
- 500: Server Error

Error response format:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task with id 999 not found"
}
```

## H2 Console

Access the H2 database console at http://localhost:8080/h2-console

Connection settings:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## Running Tests

```bash
mvn test
```

Test coverage:
- Repository: 11 tests
- Service: 13 tests
- Controller: 13 tests

## Project Structure

```
src/main/java/
  └── com/github/hnrdejesus/todo_api/
      ├── controller/
      ├── service/
      ├── repository/
      ├── model/
      ├── dto/
      └── exception/
```

## License

MIT License

## Author

Henrique de Jesus - [GitHub](https://github.com/hnrdejesus)