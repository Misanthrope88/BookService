# BookService

Spring Boot REST API for an online bookstore, built with Java 17, Maven,
Spring Security, MySQL and Liquibase.

## Run with Docker

Install Docker with the Docker Compose plugin and start the Docker daemon.
Java, Maven and MySQL are provided by the containers.

1. Create the local environment file in the project root:

   ```bash
   cp .env.template .env
   ```

   `.env.sample` is an equivalent empty template. If `.env` already exists,
   edit it instead of overwriting it.

2. Fill in every variable in `.env`:

   | Variable | Value to provide |
   | --- | --- |
   | `MYSQLDB_USER` | Application database user, for example `bookservice`. Do not use `root`. |
   | `MYSQLDB_PASSWORD` | A strong, unique password for the application database user. |
   | `MYSQLDB_ROOT_PASSWORD` | A separate strong password for the MySQL root user. |
   | `MYSQLDB_DATABASE` | Database name, for example `book_service`. Use letters, digits and underscores. |
   | `MYSQLDB_LOCAL_PORT` | An available host port for MySQL, for example `3307`. |
   | `SPRING_LOCAL_PORT` | An available host port for the API, for example `8080`. |
   | `JWT_SECRET` | A random signing secret of at least 32 ASCII characters. |

   Run `openssl rand -hex 32` separately for each password and the JWT secret,
   then copy each generated value into its corresponding variable.
   Single-quote values containing `$` or `#` to preserve them literally.

   `.env` is ignored by Git and excluded from the Docker build context.
   Keep credentials only in this local file; the committed templates must remain empty.
   Compose reads `.env` automatically and reports an error if a required value is missing.

3. Build and start the application and database:

   ```bash
   docker compose up --build -d
   ```

   The Dockerfile builds the JAR with the Maven Wrapper, skips test compilation
   and execution, and runs the application as an unprivileged user on Java 17.
   Compose waits for MySQL to accept an authenticated connection before starting
   the application. Liquibase then applies the existing database migrations.

4. With `SPRING_LOCAL_PORT=8080`, open the
   [Swagger UI](http://localhost:8080/api/swagger-ui/index.html).
   The API base URL is `http://localhost:8080/api`. Replace `8080` with your chosen port.

View application logs:

```bash
docker compose logs -f app
```

Stop and remove the containers:

```bash
docker compose down
```

MySQL data remains in the `mysql-data` volume. Database names, users and passwords
from `.env` are applied when MySQL initializes an empty volume. Changing these
values later does not update an existing database or its credentials.

The project uses `MYSQLDB_*` names in `.env`. Compose maps them to the `MYSQL_*`
variables required by the [official MySQL image](https://hub.docker.com/_/mysql).
The application connects to `mysql:3306` inside the Compose network; the local
MySQL port is exposed only on `127.0.0.1` for host tools. The JDBC settings disable
TLS and allow public key retrieval for this local Docker setup.
