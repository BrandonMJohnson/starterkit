# Polyglot Monorepo (Nx + Gradle + Node) — Implementation Specification

## Purpose

Define a polyglot monorepo structure that:

- Uses Nx as the repo-wide orchestrator
- Uses Gradle as the Java build system
- Uses pnpm as the Node workspace manager
- Keeps clear separation of concerns between ecosystems
- Avoids ambiguity about repo identity
- Supports incremental builds, caching, and CI optimization

This document is intended to be consumed by an AI agent to generate the repository.

---

## Core Design Principles

### 1. Single Orchestrator

Nx is the only repo-level orchestrator.

- All cross-project commands go through Nx
- CI uses nx affected
- Nx owns dependency graph and caching

---

### 2. Native Build Tools Remain Native

- Gradle builds Java
- pnpm builds Node

Nx wraps these tools, it does not replace them.

---

### 3. Clear Repo Identity

The repo root must visually read as an Nx workspace.

Gradle must not appear at the root.

---

### 4. Isolated Java Build System

All Gradle control-plane files must live under:

java-build/

Never place Gradle root files at the repo root.

---

### 5. Shared Layout for Apps and Libraries

All code — regardless of language — must live under:

apps/
libs/

---

### 6. Cross-Language Boundaries via Contracts

Java and Node must not share source code.

Use contracts:

- OpenAPI
- JSON Schema
- Protobuf

Located under:

contracts/

---

## Repository Structure

repo/
├─ nx.json
├─ package.json
├─ pnpm-workspace.yaml
├─ tsconfig.base.json
├─ README.md
│
├─ apps/
│  ├─ node/
│  │  ├─ gateway/
│  │  └─ admin-ui/
│  │
│  └─ java/
│     ├─ customer-api/
│     └─ billing-api/
│
├─ libs/
│  ├─ node/
│  │  ├─ shared-types/
│  │  └─ ui-components/
│  │
│  └─ java/
│     ├─ domain-model/
│     └─ shared-testkit/
│
├─ contracts/
│  ├─ openapi/
│  ├─ protobuf/
│  └─ jsonschema/
│
├─ generated/
│  ├─ java/
│  └─ node/
│
├─ java-build/
│  ├─ settings.gradle.kts
│  ├─ build.gradle.kts
│  ├─ gradle.properties
│  ├─ gradlew
│  ├─ gradlew.bat
│  ├─ gradle/
│  │  └─ libs.versions.toml
│  └─ build-logic/
│     ├─ settings.gradle.kts
│     ├─ build.gradle.kts
│     └─ src/main/kotlin/
│
├─ tools/
│  ├─ scripts/
│  └─ generators/
│
├─ infra/
│  ├─ docker/
│  ├─ helm/
│  └─ local/
│
└─ docs/
├─ adr/
└─ architecture/

---

## Responsibilities

### Nx (root)

- Task orchestration
- Dependency graph
- Caching
- CI entrypoint

---

### Gradle (java-build)

- Java compilation
- Dependency resolution
- Testing
- Packaging
- Convention plugins

---

### pnpm (root)

- Node dependency resolution
- Workspace linking
- Node execution

---

## Root Configuration

### package.json

{
"name": "platform-monorepo",
"private": true,
"packageManager": "pnpm@10.0.0",
"scripts": {
"build": "nx run-many -t build",
"test": "nx run-many -t test",
"lint": "nx run-many -t lint",
"affected:build": "nx affected -t build",
"affected:test": "nx affected -t test",
"graph": "nx graph"
},
"devDependencies": {
"nx": "^22.0.0",
"@nx/workspace": "^22.0.0"
}
}

---

### pnpm-workspace.yaml

packages:
- "apps/node/*"
- "libs/node/*"

---

### nx.json

{
"$schema": "./node_modules/nx/schemas/nx-schema.json",
"targetDefaults": {
"build": { "cache": true },
"test": { "cache": true },
"lint": { "cache": true }
}
}

---

## Gradle Configuration

### java-build/settings.gradle.kts

rootProject.name = "platform-java-build"

pluginManagement {
includeBuild("build-logic")
repositories {
gradlePluginPortal()
mavenCentral()
}
}

dependencyResolutionManagement {
repositories {
mavenCentral()
}
}

include(":apps:java:customer-api")
project(":apps:java:customer-api").projectDir = file("../apps/java/customer-api")

include(":apps:java:billing-api")
project(":apps:java:billing-api").projectDir = file("../apps/java/billing-api")

include(":libs:java:domain-model")
project(":libs:java:domain-model").projectDir = file("../libs/java/domain-model")

include(":libs:java:shared-testkit")
project(":libs:java:shared-testkit").projectDir = file("../libs/java/shared-testkit")

---

### java-build/build.gradle.kts

plugins {
base
}

allprojects {
group = "com.example.platform"
version = "1.0.0-SNAPSHOT"
}

---

## Nx Project Configuration (Java)

apps/java/customer-api/project.json

{
"name": "customer-api",
"root": "apps/java/customer-api",
"sourceRoot": "apps/java/customer-api/src",
"projectType": "application",
"targets": {
"build": {
"command": "./java-build/gradlew :apps:java:customer-api:build",
"cwd": "{workspaceRoot}"
},
"test": {
"command": "./java-build/gradlew :apps:java:customer-api:test",
"cwd": "{workspaceRoot}"
}
}
}

---

## Nx Project Configuration (Node)

apps/node/gateway/project.json

{
"name": "gateway",
"root": "apps/node/gateway",
"sourceRoot": "apps/node/gateway/src",
"projectType": "application",
"targets": {
"build": {
"command": "pnpm --filter @platform/gateway build"
},
"test": {
"command": "pnpm --filter @platform/gateway test"
}
}
}

---

## Contracts

contracts/
├─ openapi/
├─ protobuf/
└─ jsonschema/

Rules:

- Contracts are the ONLY cross-language boundary
- Generated code goes to generated/
- Never import Java into Node or Node into Java

---

## Dependency Direction

apps → libs → contracts

Never reverse this.

---

## Developer Workflow

Install:

pnpm install

Build everything:

nx run-many -t build

Test affected:

nx affected -t test

Run Java directly:

./java-build/gradlew :apps:java:customer-api:run

Run Node directly:

pnpm --filter @platform/gateway dev

---

## CI Model

Pipeline should:

1. Install dependencies
2. Run nx affected -t build,test
3. Cache Nx outputs
4. Optionally run Gradle or pnpm directly if needed

---

## Rules for Implementation Agent

1. Do not place Gradle files at repo root
2. All Java projects must be included via java-build/settings.gradle.kts
3. All Node projects must be in pnpm workspace
4. Every project must have a project.json for Nx
5. Use contracts for all cross-language sharing
6. Ensure nx commands work from repo root
7. Ensure ./java-build/gradlew works independently
8. Ensure pnpm workspace works independently

---

## Outcome

This structure ensures:

- Clear repo identity (Nx)
- Clean separation of ecosystems
- Scalable CI using affected builds
- No ambiguity about tooling ownership