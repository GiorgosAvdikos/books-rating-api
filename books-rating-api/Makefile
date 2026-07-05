# Book Rating API
# On macOS/Linux use these targets. On Windows without `make`, use run.bat
# (or the Maven wrapper directly, e.g. `mvnw.cmd spring-boot:run`).

MVNW := ./mvnw

.PHONY: help run test build package clean

help:
	@echo "Book Rating API"
	@echo "  make run      Start the API and auto-open Swagger UI (http://localhost:8080/swagger-ui.html)"
	@echo "  make test     Run the full test suite"
	@echo "  make build    Clean, run tests, and package the executable jar"
	@echo "  make package  Package the jar, skipping tests"
	@echo "  make clean    Remove build output"

run:
	$(MVNW) spring-boot:run

test:
	$(MVNW) test

build:
	$(MVNW) clean package

package:
	$(MVNW) clean package -DskipTests

clean:
	$(MVNW) clean