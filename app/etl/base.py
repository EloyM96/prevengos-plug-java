"""ETL pipeline scaffolding."""

from collections.abc import Awaitable, Callable, Iterable
from typing import Protocol, runtime_checkable


@runtime_checkable
class Extractor(Protocol):
    """Protocol for extractor components."""

    async def extract(self) -> Iterable[dict]:
        """Retrieve data from a source."""


@runtime_checkable
class Transformer(Protocol):
    """Protocol for transformer components."""

    async def transform(self, records: Iterable[dict]) -> Iterable[dict]:
        """Transform input records."""


@runtime_checkable
class Loader(Protocol):
    """Protocol for loader components."""

    async def load(self, records: Iterable[dict]) -> None:
        """Persist transformed records."""


def build_pipeline(
    extractor: Extractor,
    transformer: Transformer,
    loader: Loader,
) -> Callable[[], Awaitable[None]]:
    """Build an executable ETL pipeline."""

    async def pipeline() -> None:
        records = await extractor.extract()
        transformed = await transformer.transform(records)
        await loader.load(transformed)

    return pipeline
