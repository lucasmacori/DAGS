FROM node:22-bookworm-slim AS frontend-builder

WORKDIR /workspace

ENV NX_DAEMON=false

RUN corepack enable

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml nx.json ./
COPY apps/dags/package.json apps/dags/package.json
COPY apps/dags/project.json apps/dags/project.json

RUN pnpm install --frozen-lockfile

COPY . .

RUN pnpm nx run dags:build

FROM eclipse-temurin:25-jdk AS backend-builder

WORKDIR /workspace/apps/dags-api

COPY apps/dags-api/gradlew apps/dags-api/build.gradle apps/dags-api/settings.gradle ./
COPY apps/dags-api/gradle ./gradle
COPY apps/dags-api/src ./src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre AS java-runtime

FROM node:22-bookworm-slim AS node-runtime

FROM pgvector/pgvector:pg17-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates supervisor \
    && rm -rf /var/lib/apt/lists/*

COPY --from=java-runtime /opt/java/openjdk /opt/java/openjdk
COPY --from=node-runtime /usr/local /usr/local

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/java/openjdk/bin:/usr/local/bin:${PATH}"

WORKDIR /opt/dags

COPY --from=backend-builder /workspace/apps/dags-api/build/libs/*.jar /opt/dags/backend/app.jar
COPY --from=frontend-builder /workspace/apps/dags/.output /opt/dags/frontend/.output

COPY docker/dags-entrypoint.sh /usr/local/bin/dags-entrypoint.sh
COPY docker/dags-run-postgres.sh /usr/local/bin/dags-run-postgres.sh
COPY docker/dags-start-backend.sh /usr/local/bin/dags-start-backend.sh
COPY docker/dags-start-frontend.sh /usr/local/bin/dags-start-frontend.sh
COPY docker/supervisord.conf /etc/supervisor/conf.d/dags.conf

RUN chmod +x /usr/local/bin/dags-entrypoint.sh \
    /usr/local/bin/dags-run-postgres.sh \
    /usr/local/bin/dags-start-backend.sh \
    /usr/local/bin/dags-start-frontend.sh \
    && mkdir -p /var/log/supervisor /var/run/postgresql /var/lib/postgresql/data \
    && chown -R postgres:postgres /var/run/postgresql /var/lib/postgresql

ENV FRONTEND_HOST=0.0.0.0
ENV FRONTEND_PORT=3000
ENV BACKEND_PORT=8080
ENV POSTGRES_PORT=5432
ENV POSTGRES_LISTEN_ADDRESSES=0.0.0.0
ENV POSTGRES_DB=dags
ENV POSTGRES_USER=dags
ENV POSTGRES_PASSWORD=dags
ENV PGDATA=/var/lib/postgresql/data
ENV AUTH_REGISTRATION_ENABLED=false
ENV AUTH_ACCESS_TOKEN_TTL_SECONDS=300
ENV AUTH_REFRESH_TOKEN_TTL_SECONDS=259200
ENV AUTH_TOKEN_SECRET=change-me-in-production-change-me-in-production
ENV CHAT_MEMORY_PROVIDER=postgres
ENV CHAT_DOCUMENTS_PROVIDER=pgvector

VOLUME ["/var/lib/postgresql/data"]

EXPOSE 3000 8080 5432

ENTRYPOINT ["/usr/local/bin/dags-entrypoint.sh"]
