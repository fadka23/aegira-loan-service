SHELL := /bin/sh

COMPOSE ?= docker compose
APP_NAME ?= aegira-loan-service

SPRING_DATASOURCE_URL ?= jdbc:postgresql://localhost:5432/aegira_loan
SPRING_DATASOURCE_USERNAME ?= aegira
SPRING_DATASOURCE_PASSWORD ?= aegira_password
REDIS_HOST ?= localhost
REDIS_PORT ?= 6379
JWT_SECRET ?= change-this-secret
JWT_EXPIRATION ?= 3600000
LOAN_DATA_SOURCE_MODE ?= DATABASE
IDEMPOTENCY_ENABLED ?= true
IDEMPOTENCY_TTL_SECONDS ?= 86400

.PHONY: help run test clean build package infra-up infra-down infra-reset infra-restart infra-logs docker-up docker-down docker-reset docker-restart docker-logs ps

help:
	@echo "$(APP_NAME)"
	@echo ""
	@echo "App commands:"
	@echo "  make run             Run Spring Boot locally with Maven"
	@echo "  make test            Run unit tests"
	@echo "  make clean           Clean Maven build output"
	@echo "  make build           Run clean test"
	@echo "  make package         Build jar without tests"
	@echo ""
	@echo "Infrastructure only:"
	@echo "  make infra-up        Start Postgres and Redis only"
	@echo "  make infra-down      Stop Postgres and Redis"
	@echo "  make infra-reset     Remove Postgres and Redis containers"
	@echo "  make infra-restart   Restart Postgres and Redis"
	@echo "  make infra-logs      Tail Postgres and Redis logs"
	@echo ""
	@echo "Full Docker stack:"
	@echo "  make docker-up       Start app, Postgres, and Redis"
	@echo "  make docker-down     Stop full Docker stack"
	@echo "  make docker-reset    Remove full Docker stack containers"
	@echo "  make docker-restart  Restart full Docker stack"
	@echo "  make docker-logs     Tail full Docker stack logs"
	@echo "  make ps              Show Docker Compose services"

run:
	SPRING_DATASOURCE_URL="$(SPRING_DATASOURCE_URL)" \
	SPRING_DATASOURCE_USERNAME="$(SPRING_DATASOURCE_USERNAME)" \
	SPRING_DATASOURCE_PASSWORD="$(SPRING_DATASOURCE_PASSWORD)" \
	REDIS_HOST="$(REDIS_HOST)" \
	REDIS_PORT="$(REDIS_PORT)" \
	JWT_SECRET="$(JWT_SECRET)" \
	JWT_EXPIRATION="$(JWT_EXPIRATION)" \
	LOAN_DATA_SOURCE_MODE="$(LOAN_DATA_SOURCE_MODE)" \
	IDEMPOTENCY_ENABLED="$(IDEMPOTENCY_ENABLED)" \
	IDEMPOTENCY_TTL_SECONDS="$(IDEMPOTENCY_TTL_SECONDS)" \
	mvn spring-boot:run

test:
	mvn test

clean:
	mvn clean

build:
	mvn clean test

package:
	mvn clean package -DskipTests

infra-up:
	$(COMPOSE) up -d postgres redis

infra-down:
	$(COMPOSE) stop postgres redis

infra-reset:
	$(COMPOSE) rm -sf postgres redis

infra-restart: infra-down infra-up

infra-logs:
	$(COMPOSE) logs -f postgres redis

docker-up:
	$(COMPOSE) up --build

docker-down:
	$(COMPOSE) down

docker-reset:
	$(COMPOSE) down --remove-orphans

docker-restart: docker-down docker-up

docker-logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps
