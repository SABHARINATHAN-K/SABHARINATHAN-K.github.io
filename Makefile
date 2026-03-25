SHELL := /bin/bash

.PHONY: up down restart status logs backend frontend

up:
	./dev.sh up

down:
	./dev.sh down

restart:
	./dev.sh restart

status:
	./dev.sh status

logs:
	./dev.sh logs

backend:
	./scripts/backend.sh

frontend:
	./scripts/frontend.sh
