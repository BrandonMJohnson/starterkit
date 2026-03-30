# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS repo-build

RUN apk add --no-cache bash nodejs npm \
    && npm install -g pnpm@10.0.0

WORKDIR /workspace

COPY package.json pnpm-workspace.yaml nx.json tsconfig.base.json ./
COPY java-build/gradlew java-build/gradlew.bat java-build/gradle.properties ./java-build/
COPY java-build/gradle ./java-build/gradle

COPY java-build/settings.gradle.kts java-build/build.gradle.kts ./java-build/

COPY libs/java/commons/build.gradle ./libs/java/commons/build.gradle
COPY libs/java/orchestration-clients/build.gradle ./libs/java/orchestration-clients/build.gradle
COPY libs/java/persistence/build.gradle ./libs/java/persistence/build.gradle
COPY apps/java/api-service/build.gradle ./apps/java/api-service/build.gradle
COPY apps/java/orchestration/build.gradle ./apps/java/orchestration/build.gradle
COPY apps/java/policy-service/build.gradle ./apps/java/policy-service/build.gradle
COPY apps/platform/ui-service/build.gradle ./apps/platform/ui-service/build.gradle
COPY apps/node/starterkit-ui/package.json ./apps/node/starterkit-ui/package.json
COPY apps/node/temporal-worker/package.json ./apps/node/temporal-worker/package.json
COPY pnpm-lock.yaml ./

RUN chmod +x java-build/gradlew

WORKDIR /workspace/java-build

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon help

WORKDIR /workspace

COPY apps ./apps
COPY libs ./libs
COPY contracts ./contracts
COPY generated ./generated
COPY tools ./tools

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/root/.local/share/pnpm/store \
    ./java-build/gradlew -p java-build --no-daemon build \
    :apps:java:api-service:installDist \
    :apps:java:orchestration:installDist \
    :apps:java:policy-service:installDist

FROM eclipse-temurin:21-jre-alpine AS api-service

WORKDIR /app

COPY --from=repo-build /workspace/apps/java/api-service/build/install/api-service/ /app/

EXPOSE 8080

CMD ["/app/bin/api-service"]

FROM eclipse-temurin:21-jre-alpine AS orchestration

WORKDIR /app

COPY --from=repo-build /workspace/apps/java/orchestration/build/install/orchestration/ /app/

EXPOSE 8081

CMD ["/app/bin/orchestration"]

FROM eclipse-temurin:21-jre-alpine AS policy-service

WORKDIR /app

COPY --from=repo-build /workspace/apps/java/policy-service/build/install/policy-service/ /app/

EXPOSE 8082

CMD ["/app/bin/policy-service"]

FROM nginx:1.27-alpine AS ui-service

RUN apk add --no-cache gettext \
    && rm /etc/nginx/conf.d/default.conf \
    && mkdir -p /etc/starterkit-ui /usr/share/nginx/html-baked /var/run/starterkit-ui-config /var/run/starterkit-ui-overlay /tmp/client_temp /tmp/proxy_temp /tmp/fastcgi_temp /tmp/uwsgi_temp /tmp/scgi_temp \
    && chown -R nginx:nginx /usr/share/nginx/html /usr/share/nginx/html-baked /etc/nginx /etc/starterkit-ui /var/cache/nginx /var/run/starterkit-ui-config /var/run/starterkit-ui-overlay /tmp

COPY --from=repo-build /workspace/apps/platform/ui-service/build/frontend-static/ /usr/share/nginx/html-baked/
COPY apps/platform/ui-service/nginx/nginx.conf /etc/nginx/nginx.conf
COPY apps/platform/ui-service/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY apps/platform/ui-service/nginx/local-proxy.conf.template /etc/starterkit-ui/local-proxy.conf.template
COPY apps/platform/ui-service/nginx/runtime-config.json.template /etc/starterkit-ui/runtime-config.json.template
COPY apps/platform/ui-service/nginx/entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

USER nginx

EXPOSE 8090

ENTRYPOINT ["/entrypoint.sh"]
