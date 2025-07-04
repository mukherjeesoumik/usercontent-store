# usercontent-store

A Spring Boot application for storing, retrieving, and downloading user-submitted content (text, HTML, JSON, etc.) in a PostgreSQL database. Supports multiple file formats for download and easy API testing with Postman.

## Features
- Submit any content (text, HTML, JSON, etc.)
- Store content as `TEXT` in PostgreSQL
- Retrieve, delete, and download content by ID
- Download content in various formats: JSON, TXT, XML, DOCX, PDF, JS, CSS, PY, SVG, JPG, PNG, JPEG, GIF
- Renders text as images for image downloads
- API fully testable via Postman

## Technologies Used
- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Apache POI (DOCX)
- iText (PDF)
- Lombok

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- PostgreSQL

### Database Setup
1. Create a PostgreSQL database (e.g., `userstoredb`).
2. Create the table:
   ```sql
   CREATE TABLE user_contents (
       id SERIAL PRIMARY KEY,
       content TEXT,
       created_at TIMESTAMP
   );
   ```
3. Update `src/main/resources/application.properties` with your DB credentials.

### Build & Run
```sh
mvn clean install
mvn spring-boot:run
```

### API Usage

#### Add Content
- **POST** `/content`
- Body (raw JSON or text):
  ```json
  {
    "content": "Your content here"
  }
  ```

#### Get Content
- **GET** `/content/{id}`

#### Delete Content
- **DELETE** `/content/{id}`

#### Download Content
- **GET** `/content/{id}/download?type={format}`
  - Supported formats: `json`, `txt`, `xml`, `docx`, `pdf`, `js`, `css`, `py`, `svg`, `jpg`, `jpeg`, `png`, `gif`

### Testing with Postman
- Import endpoints and test with various content and formats.

## License
MIT

---

**GitHub repository name suggestion:** `usercontent-store`
