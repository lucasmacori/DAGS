# DAGS

DAGS packages three services into one deployment image:

- `dags` front-end (TanStack Start)
- `dags-api` back-end (Spring Boot)
- PostgreSQL 17 with `pgvector`

The front-end is preconfigured to talk to the back-end inside the same container, and the back-end is preconfigured to talk to the bundled PostgreSQL instance. The default deployment path is a single `docker run` command.

## Architecture

- Front-end HTTP server: `3000`
- Back-end API: `8080`
- PostgreSQL: `5432`
- Persistent database volume: `/var/lib/postgresql/data`

At runtime:

1. PostgreSQL starts with a persistent data directory.
2. The container ensures the configured database and user exist.
3. The Spring Boot app starts and runs Flyway migrations.
4. The front-end starts and proxies its server-side API calls to the local back-end.

## Build The Image

From the repository root:

```bash
docker build -t dags-all-in-one .
```

## Run The Container

The simplest local deployment:

```bash
docker run -d \
  --name dags \
  -p 3000:3000 \
  -p 8080:8080 \
  -p 5432:5432 \
  -v dags_data:/var/lib/postgresql/data \
  -e SPRING_AI_OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  dags-all-in-one
```

Then open:

- Front-end: `http://localhost:3000`
- Back-end: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

Notes:

- `SPRING_AI_OLLAMA_BASE_URL` is the main required external dependency if you want chat and translation generation to work.
- On Linux, replace `host.docker.internal` with an address reachable from the container.
- Keep the volume mount if you want PostgreSQL data to survive container recreation.

## Optional Docker Compose

```yaml
services:
  dags:
    image: dags-all-in-one
    build: .
    ports:
      - "3000:3000"
      - "8080:8080"
      - "5432:5432"
    volumes:
      - dags_data:/var/lib/postgresql/data
    environment:
      SPRING_AI_OLLAMA_BASE_URL: http://host.docker.internal:11434

volumes:
  dags_data:
```

## Runtime Configuration

### Deployment Variables

These variables control the bundled services in the image.

| Variable | Default | Purpose |
| --- | --- | --- |
| `FRONTEND_HOST` | `0.0.0.0` | Host binding for the front-end server |
| `FRONTEND_PORT` | `3000` | Port used by the front-end server |
| `BACKEND_PORT` | `8080` | Port used by the Spring Boot API |
| `POSTGRES_PORT` | `5432` | Port used by PostgreSQL |
| `POSTGRES_LISTEN_ADDRESSES` | `0.0.0.0` | PostgreSQL listen addresses |
| `PGDATA` | `/var/lib/postgresql/data` | PostgreSQL data directory |
| `POSTGRES_DB` | `dags` | PostgreSQL database name |
| `POSTGRES_USER` | `dags` | PostgreSQL application user |
| `POSTGRES_PASSWORD` | `dags` | PostgreSQL application password |

### Front-End Variables

These are read by the TanStack Start server process.

| Variable | Default In Container | Purpose |
| --- | --- | --- |
| `AI_TOOLS_API_BASE_URL` | `http://127.0.0.1:${BACKEND_PORT}/api/v1` | Base URL used by the front-end server to call the back-end |
| `AI_TOOLS_API_USERNAME` | `API_BASIC_AUTH_USERNAME` | Username used by the front-end proxy when calling the back-end |
| `AI_TOOLS_API_PASSWORD` | `API_BASIC_AUTH_PASSWORD` | Password used by the front-end proxy when calling the back-end |
| `HOST` | `FRONTEND_HOST` | Alternate host binding override for Nitro/TanStack Start |
| `PORT` | `FRONTEND_PORT` | Alternate port override for Nitro/TanStack Start |
| `NITRO_HOST` | `HOST` | Nitro-specific host override |
| `NITRO_PORT` | `PORT` | Nitro-specific port override |

You usually do not need to set any of these manually in the all-in-one container, since the image links the front-end to the local back-end automatically.

### Back-End Variables

These variables configure the Spring Boot app and are the main knobs for behavior changes.

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `BACKEND_PORT` | Spring Boot HTTP port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://127.0.0.1:${POSTGRES_PORT}/${POSTGRES_DB}` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `POSTGRES_USER` | JDBC username |
| `SPRING_DATASOURCE_PASSWORD` | `POSTGRES_PASSWORD` | JDBC password |
| `API_BASIC_AUTH_USERNAME` | `ai` | HTTP Basic auth username for the API |
| `API_BASIC_AUTH_PASSWORD` | `completelylocal` | HTTP Basic auth password for the API |
| `CHAT_DEFAULT_MODEL` | `SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL` or `gemma4:e4b` | Default chat model |
| `CHAT_PROMPT_SYSTEM` | built-in prompt | Main system prompt for chat |
| `CHAT_MEMORY_PROVIDER` | `postgres` in container | Chat memory storage provider |
| `CHAT_MEMORY_MAX_MESSAGES` | `20` | Max messages retained in chat memory window |
| `CHAT_DOCUMENTS_PROVIDER` | `pgvector` in container | Chat document storage provider |
| `CHAT_DOCUMENTS_MAX_CHARACTERS` | `200000` | Max extracted characters stored per document |
| `CHAT_DOCUMENTS_MAX_FILE_SIZE_BYTES` | `10485760` | Max upload size per document |
| `SPRING_AI_OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama base URL |
| `SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL` | `gemma4:e4b` | Ollama chat model |
| `SPRING_AI_OLLAMA_EMBEDDING_OPTIONS_MODEL` | `nomic-embed-text` | Ollama embedding model |
| `TRANSLATION_PROMPT_SYSTEM` | built-in prompt | Translation system prompt |
| `SPRING_PROFILES_ACTIVE` | unset | Optional Spring profile override |

### Practical Examples

Run with a different PostgreSQL password and API credentials:

```bash
docker run -d \
  --name dags \
  -p 3000:3000 \
  -v dags_data:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD=supersecret \
  -e API_BASIC_AUTH_USERNAME=admin \
  -e API_BASIC_AUTH_PASSWORD=adminsecret \
  -e SPRING_AI_OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  dags-all-in-one
```

Run with custom chat model settings:

```bash
docker run -d \
  --name dags \
  -p 3000:3000 \
  -v dags_data:/var/lib/postgresql/data \
  -e SPRING_AI_OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  -e SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL=gemma4:e2b \
  -e SPRING_AI_OLLAMA_EMBEDDING_OPTIONS_MODEL=nomic-embed-text \
  -e CHAT_MEMORY_MAX_MESSAGES=50 \
  dags-all-in-one
```

## Development

Local development is still split by app:

```bash
pnpm dev:ui
pnpm dev:api
```

The front-end workspace app is in `apps/dags`.
The back-end Spring Boot app is in `apps/dags-api`.
