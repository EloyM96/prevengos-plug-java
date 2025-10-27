"""Example router to illustrate domain module integration."""
from fastapi import APIRouter

router = APIRouter(prefix="/example", tags=["example"])


@router.get("/status", summary="Health check for the example module")
async def example_status() -> dict[str, str]:
    """Return a static payload representing module readiness."""
    return {"status": "ok"}
