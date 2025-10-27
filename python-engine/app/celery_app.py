"""Instancia de Celery para el motor Prevengos."""

from celery import Celery

from .config import get_settings

settings = get_settings()

celery = Celery(
    settings.app_name,
    broker=settings.broker_url,
    backend=settings.result_backend,
)

celery.autodiscover_tasks(packages=["app.tasks"])
