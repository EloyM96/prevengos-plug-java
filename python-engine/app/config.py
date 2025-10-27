"""Configuración compartida para FastAPI y Celery."""

from pydantic import BaseSettings, Field


class Settings(BaseSettings):
    app_name: str = Field(default="prevengos-python-engine")
    broker_url: str = Field(default="redis://localhost:6379/0")
    result_backend: str = Field(default="redis://localhost:6379/1")

    class Config:
        env_prefix = "PREVENGOS_"
        env_file = ".env"
        env_file_encoding = "utf-8"


def get_settings() -> Settings:
    """Retorna la configuración cargada desde variables de entorno."""

    return Settings()
