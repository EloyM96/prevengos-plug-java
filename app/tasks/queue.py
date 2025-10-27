"""Task queue integration using Redis and RQ."""
from redis import Redis
from rq import Queue

from app.config.settings import settings


redis_connection = Redis.from_url(settings.redis_url)
queue = Queue("default", connection=redis_connection)


def enqueue(task_name: str, *args, **kwargs) -> None:
    """Enqueue a task by name."""
    queue.enqueue(task_name, *args, **kwargs)
