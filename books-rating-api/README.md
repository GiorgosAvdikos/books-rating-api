# Book Rating API

A small Spring Boot service for the Moro Technology Software Engineer Challenge. It lets
you search Project Gutenberg books (via the [Gutendex](https://gutendex.com/) API), post
ratings and reviews, and fetch a book's details together with its average rating and reviews.

## Requirements

- **JDK 21** (the bundled Maven wrapper downloads Maven for you).
- No database setup needed: it uses a local SQLite file (`bookrating.db`), created on first run.

## Run it

The API starts on **http://localhost:8080** and **auto-opens the Swagger UI** in your
default browser, where every endpoint can be tried interactively.

First go to project folder with "cd books-rating-api":

And after:

| Platform | Command |
|----------|---------|
| Windows  | `run.bat`  (or `mvnw.cmd spring-boot:run`) |
| macOS/Linux | `./run.sh`  (or `make run`) |

If the browser doesn't open (headless machine, etc.), go to
**http://localhost:8080/swagger-ui.html** manually. The root URL `/` also redirects there.

## Test it

```bash
make test          # or:  ./mvnw test      (mvnw.cmd test on Windows)
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET  | `/api/books?search={title}&page={n}` | Search books by title (Part 1; `page` optional) |
| POST | `/api/reviews` | Post a rating (0–5) and review for a book (Part 2) |
| GET  | `/api/books/{id}` | Book details + average rating + reviews (Part 3) |
| GET  | `/api/books/top?limit={n}` | Top N books by average rating (bonus) |
| GET  | `/api/books/{id}/ratings/monthly` | Average rating per month (bonus) |

Sample review payload (`POST /api/reviews`):

```json
{ "bookId": 84, "rating": 4, "review": "It's been fifty years since I had read Frankenstein..." }
```

## Notes

- **Persistence:** SQLite via Spring Data JPA. The schema is defined in
  [`src/main/resources/schema.sql`](src/main/resources/schema.sql) and Hibernate DDL is off
  (`spring.jpa.hibernate.ddl-auto=none`).
- **Caching:** Gutendex responses are cached with Caffeine (see `application.yml`).
- **Error handling:** upstream Gutendex failures return `502`, unknown book ids `404`, and
  invalid payloads `400`, all as RFC 7807 `ProblemDetail` responses.
- **Config:** to disable the browser auto-open, set `app.open-browser=false`
  (already off during tests).
