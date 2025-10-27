# Prevengos Plug MVP Architecture

## Core Backend
- **Framework**: Modular FastAPI monolith exposing HTTP interfaces.
- **Module Boundaries**: Domain functionality is organised in `app/modules`, with supporting subsystems for ETL, rules, notifications and declarative workflows.
- **Data Access**: SQLAlchemy async engine configured through `app/db/session.py`.
- **Task Queue**: Redis-backed RQ workers defined under `app/tasks` to run asynchronous jobs.
- **External Integrations**: All adapters communicate via JSON contracts to keep coupling low.

## Frontend
- **Technology**: Next.js application scaffolded in `frontend/` with contracts aligned to the backend JSON APIs.

## Extensibility
- **Becario Extensions**: Additional interfaces may be implemented as CLI commands or HTTP endpoints that re-use the core domain modules and shared infrastructure.

## Operations
- **Docker**: Container images defined via `Dockerfile`; multi-service orchestration via `docker-compose.yml` including PostgreSQL, Redis and RQ worker.
- **Migrations**: Database migration scripts will live under `migrations/`.
