"""Database session management primitives."""
from contextlib import asynccontextmanager

from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.config.settings import settings


def get_engine():
    """Create the SQLAlchemy async engine."""
    return create_async_engine(settings.database_url, echo=False, future=True)


async_engine = get_engine()
SessionFactory = async_sessionmaker(bind=async_engine, expire_on_commit=False, class_=AsyncSession)


@asynccontextmanager
async def get_session() -> AsyncSession:
    """Provide a transactional scope around a series of operations."""
    async with SessionFactory() as session:
        yield session
