FROM node:22-alpine AS build

RUN corepack enable

WORKDIR /workspace

COPY package.json pnpm-workspace.yaml pnpm-lock.yaml nx.json tsconfig.base.json ./
COPY apps/node/starterkit-ui/package.json ./apps/node/starterkit-ui/package.json
COPY apps/node/temporal-worker/package.json ./apps/node/temporal-worker/package.json

RUN pnpm install --frozen-lockfile --filter @starterkit/node-temporal-worker...

COPY apps/node ./apps/node

RUN pnpm --filter @starterkit/node-temporal-worker build

FROM node:22-alpine AS runtime

RUN corepack enable

WORKDIR /workspace

COPY --from=build /workspace /workspace

ENV TEMPORAL_ADDRESS=127.0.0.1:7233
ENV TEMPORAL_NAMESPACE=default
ENV TEMPORAL_TASK_QUEUE=hello-from-nodejs-task-queue

CMD ["pnpm", "--filter", "@starterkit/node-temporal-worker", "worker"]
