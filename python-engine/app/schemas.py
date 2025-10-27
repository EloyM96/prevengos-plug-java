"""Esquemas Pydantic compartidos por la API y las tareas."""

from typing import Any, Dict

from pydantic import BaseModel, Field


class NotificationRequest(BaseModel):
    canal: str = Field(..., description="Canal de entrega (email|sms|whatsapp)")
    destino: str
    plantilla: str
    datos: Dict[str, Any] = Field(default_factory=dict)


class NotificationResponse(BaseModel):
    task_id: str
    status: str
