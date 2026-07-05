# Implementation notes

A few notes on how I approached the challenge and why I made the choices I did.
The README covers how to run it, so this is more about the reasoning.

## Tech Stack

Java 21 and Spring Boot. It's what I'm most productive in for this kind of task,
and Spring gives me the web layer, validation, JPA and caching without much setup.
For storage I went with SQLite since the brief suggested it and there's no real
reason to run anything heavier for this. The DB file is created on first run, so
there's nothing to install. Build and dependency management is Maven, via the bundled wrapper.

## Structure

I kept the usual layering:

- `controller` – the HTTP endpoints, thin and focused on request/response handling.
- `service` – the application logic (searching, combining book data with reviews, aggregations).
- `client` – the Gutendex integration, isolated behind a small interface so the services do not depend on the external API implementation.
- `repository` – Spring Data JPA access to the reviews table.
- `dto` / `domain` – request/response models and the Review entity.

Other packages:
- `exception` – application-specific exceptions and centralized API error handling.
- `config` -  Application setup and startup classes: caching, OpenAPI/Swagger docs, and auto-opening the Swagger UI on startup.

The Gutendex call sits behind an interface (BookProviderClient) — mostly for loose coupling: it's easy to mock in tests, and easy to swap or extend later if the provider changes.

## Endpoints

Parts 1–3 map to search, post a review, and get book details with the average
rating and reviews. I also did the bonus tasks: top N books by average rating,
and average rating per month for a book. Both are simple aggregate queries on the
reviews table.


| Method | Path | Description |
|--------|------|-------------|
| GET  | `/api/books?search={title}&page={n}` | Search books by title (Part 1; `page` optional) |
| POST | `/api/reviews` | Post a rating (0–5) and review for a book (Part 2) |
| GET  | `/api/books/{id}` | Book details + average rating + reviews (Part 3) |
| GET  | `/api/books/top?limit={n}` | Top N books by average rating (bonus) |
| GET  | `/api/books/{id}/ratings/monthly` | Average rating per month (bonus) |

## A few decisions worth mentioning

**Schema managed by hand.** I turned Hibernate's DDL generation off and define the
table in a `schema.sql` instead. This gave me more predictable behaviour with SQLite
and keeps the schema consistent between the tests and the running app.

**Caching.** Gutendex responses are cached with Caffeine. Book details don't change
often and the search/detail endpoints hit the same book repeatedly, so it saves a lot
of redundant upstream calls. This covers the caching bonus.

**Validation.** The review payload is validated (rating has to be 0–5, book id required,
etc.) so bad data doesn't reach the database.

**Error handling.** There's a single `@ControllerAdvice` that turns the common failures
into proper responses: upstream Gutendex problems become 502, an unknown book id is 404,
and invalid input is 400. All returned as standard `ProblemDetail` bodies.

**Swagger.** The app opens the Swagger UI in the browser on startup, which makes it
easy to try the endpoints without reaching for curl or Postman. It's disabled during
tests.

## Testing

There are unit tests for the services and validation, and integration tests for the
controllers, the Gutendex client, and the repository. I focused on the paths that
actually matter, the happy cases plus the error handling.
