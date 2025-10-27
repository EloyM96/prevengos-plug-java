"""Centralised application settings."""
from functools import lru_cache

from pydantic import BaseSettings


class Settings(BaseSettings):
    """Application configuration values."""

    app_name: str = "Prevengos Plug"
    version: str = "0.1.0"
    environment: str = "development"
    database_url: str = "postgresql+asyncpg://user:password@postgres:5432/prevengos"
    redis_url: str = "redis://redis:6379/0"

    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    """Return a cached instance of the app settings."""

    return Settings()


settings = get_settings()
