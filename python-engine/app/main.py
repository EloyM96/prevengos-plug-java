"""Aplicación FastAPI de entrada para el motor Prevengos."""

from fastapi import FastAPI

from .config import get_settings
from .routes import notifications

settings = get_settings()
app = FastAPI(title=settings.app_name)
app.include_router(notifications.router)


@app.get("/health")
async def healthcheck() -> dict[str, str]:
    """Endpoint de *healthcheck* sencillo para orquestadores."""

    return {"status": "ok"}
