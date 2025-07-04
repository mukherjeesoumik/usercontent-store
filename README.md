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

## Postman Testing Guide

### 1. Add Content
- **Method:** POST
- **URL:** http://localhost:8080/content
- **Headers:**
  - Content-Type: application/json
- **Body (raw, JSON):**
  ```json
{
  "name": "Kohli",
  "message": "Kohli holds the most Player of the Series and second most Player of the Match awards across all formats combined, received the Arjuna Award (2013), Padma Shri (2017), and Khel Ratna Award (2018), was listed in Time's 100 most influential people in 2018, retired from T20Is after the 2024 World Cup final, retired from Tests in May 2025 at 36, is married to actress Anushka Sharma with two children, was born on 5 November 1988 in Delhi to a Punjabi Hindu family, showed early cricket interest, trained at West Delhi Cricket Academy under Rajkumar Sharma, overcame early setbacks including missing the U-14 Delhi team, advanced through youth cricket captaining Delhi U-15, scoring multiple centuries, excelled in Vijay Merchant Trophy with the U-17 team, was praised for temperament by coach Ajit Chaudhary, and was described by Ashish Nehra as a young kid who became a perfect athlete through dedication on and off the field."
}

  ```
- **Send** the request. You should get a response with the saved content and its ID.

### 2. Get Content by ID
- **Method:** GET
- **URL:** http://localhost:8080/content/{id}
- Replace `{id}` with the ID returned from the POST request.
- **Send** the request. You should get the content as plain text.

### 3. Delete Content by ID
- **Method:** DELETE
- **URL:** http://localhost:8080/content/{id}
- Replace `{id}` with the ID to delete.
- **Send** the request. You should get a 204 No Content response.

### 4. Download Content in Various Formats
- **Method:** GET
- **URL:** http://localhost:8080/content/{id}/download?type={format}
- Replace `{id}` with the content ID and `{format}` with one of:
  - json, txt, xml, docx, pdf, js, css, py, svg, jpg, jpeg, png, gif
- **Send** the request. The file will be downloaded in the chosen format.

### 5. Tips
- Use the Postman "Save Response" feature to save downloaded files.
- You can test with different content types (plain text, JSON, code, etc.).
- If you get a 404, check that the ID exists.
- If you get a 400, check your request body and headers.

## License
MIT

---

**GitHub repository name suggestion:** `usercontent-store`
