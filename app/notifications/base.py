"""Notification dispatch abstractions."""
from enum import Enum
from typing import Protocol, runtime_checkable


class Channel(str, Enum):
    """Supported notification channels."""

    EMAIL = "email"
    SMS = "sms"
    WEBHOOK = "webhook"


@runtime_checkable
class Notifier(Protocol):
    """Notifier contract for pluggable adapters."""

    channel: Channel

    async def send(self, recipient: str, payload: dict) -> None:
        """Dispatch a message to the recipient."""
        raise NotImplementedError
