# DAGS Core Development Workflow

This skill provides essential context and workflow guidelines for the DAGS codebase. DAGS is a private AI workspace designed as a self-hosted full-stack application within an Nx monorepo.

## Project Structure

1. **Frontend (`apps/dags`)**:
   - Built with **TanStack Start** and **React**.
   - Handles the chat interface, document uploads, and rendering markdown (with Math support via Katex).
   - Proxies server-side API calls to the back-end using JWT-backed secure cookies.
   - Runs on port `3000` by default.

2. **Backend (`apps/dags-api`)**:
   - Built with **Spring Boot** (Java).
   - Provides REST APIs (`/api/v1/auth/*`, `/api/v1/chat/*`, etc.).
   - Integrates with local LLM models using **Spring AI** and **Ollama**.
   - Runs on port `8080` by default.

3. **Database**:
   - **PostgreSQL 17** with `pgvector` extension.
   - Handles chat memory, user accounts, and document embedding storage.
   - Uses **Flyway** for database migrations (handled by the Spring Boot app on startup).

## Common Tasks & Commands

- **Run Frontend Dev Server**: `pnpm dev:ui`
- **Run Backend Dev Server**: `pnpm dev:api`
- **Build All**: `pnpm build`
- **Test All**: `pnpm test`

## Docker Deployment

The application is packaged into a single all-in-one Docker container.
- **Build the image**: `docker build -t dags-all-in-one .`
- **Run locally**:
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

## Authentication Flow

Authentication is end-to-end using tokens.
- Registration is **disabled by default**. Set `AUTH_REGISTRATION_ENABLED=true` to enable creating accounts.
- Access tokens (5 min lifespan) and refresh tokens (3 days) are stored in `HttpOnly` cookies.
- To test registration locally, start the backend with the environment variable `AUTH_REGISTRATION_ENABLED=true`.

When modifying code:
- Ensure React code follows modern hooks and TanStack Start routing conventions.
- Ensure Spring Boot code follows standard Java REST Controller/Service/Repository patterns.