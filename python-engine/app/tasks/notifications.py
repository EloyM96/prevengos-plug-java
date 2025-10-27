"""Tareas Celery relacionadas con notificaciones."""

from celery import shared_task
from celery.utils.log import get_task_logger

logger = get_task_logger(__name__)


@shared_task(name="notifications.enqueue")
def enqueue_notification(payload: dict) -> dict:
    """Simula la encolación de una notificación."""

    logger.info("Encolando notificación %s", payload)
    # Aquí se invocarían los adaptadores reales (WhatsApp, email, etc.)
    return {"status": "queued", "payload": payload}
