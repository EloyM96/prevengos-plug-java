"""Endpoints relacionados con notificaciones."""

from fastapi import APIRouter

from ..schemas import NotificationRequest, NotificationResponse
from ..tasks.notifications import enqueue_notification

router = APIRouter(prefix="/notify", tags=["notifications"])


@router.post("/send", response_model=NotificationResponse)
async def send_notification(request: NotificationRequest) -> NotificationResponse:
    """Encola una notificación para ser entregada de forma asíncrona."""

    task = enqueue_notification.delay(request.dict())
    return NotificationResponse(task_id=task.id, status="queued")
