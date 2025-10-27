# Prevengos Plug MVP Scaffold

This repository contains the initial project structure for the Prevengos Plug MVP. It aligns the team on a modular FastAPI monolith paired with a Next.js frontend and shared infrastructure services.

## Structure
- `app/` – FastAPI application package with configuration, database access, domain modules and supporting subsystems.
  - `config/` – Pydantic-driven settings.
  - `db/` – SQLAlchemy async session management.
  - `modules/` – Domain modules and API routers.
  - `etl/` – ETL pipeline contracts.
  - `rules/` – Business rules engine primitives.
  - `notifications/` – Notification channel abstractions.
  - `tasks/` – Redis/RQ task queue integration.
  - `workflows/` – Declarative workflow scaffolding.
- `frontend/` – Next.js UI workspace.
- `migrations/` – Alembic migrations.
- `Dockerfile`, `docker-compose.yml` – Containerisation assets for local development and deployment.

## Getting Started
1. Create and activate a virtual environment.
2. Install dependencies with `pip install -r requirements.txt`.
3. Launch the stack via `docker-compose up --build`.
4. Access the API documentation at `http://localhost:8000/docs`.

## Next Steps
- Flesh out domain modules, routers and schemas following JSON-based contracts.
- Implement CLI or HTTP extensions for interns using shared modules.
- Configure linting, testing and CI pipelines.
