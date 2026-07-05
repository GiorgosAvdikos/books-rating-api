@echo off
REM Start the Book Rating API. Swagger UI opens automatically once it's up.
cd /d "%~dp0"
call mvnw.cmd spring-boot:run %*