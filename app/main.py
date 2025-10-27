"""Application entrypoint for the Prevengos Plug MVP."""
from fastapi import FastAPI

from app.config.settings import settings
from app.modules.example.router import router as example_router


def create_app() -> FastAPI:
    """Build and configure the FastAPI application instance."""
    application = FastAPI(
        title=settings.app_name,
        version=settings.version,
        docs_url="/docs",
        redoc_url="/redoc",
    )

    # Register routers from domain modules
    application.include_router(example_router)

    return application


app = create_app()
