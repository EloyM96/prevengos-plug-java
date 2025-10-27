"""Rule evaluation engine placeholder."""
from collections.abc import Iterable
from typing import Protocol, runtime_checkable


@runtime_checkable
class Rule(Protocol):
    """Domain rule contract."""

    name: str

    async def evaluate(self, payload: dict) -> bool:
        """Evaluate rule against provided payload."""

async def run_rules(rules: Iterable[Rule], payload: dict) -> list[tuple[str, bool]]:
    """Run rule set against payload returning results."""
    results: list[tuple[str, bool]] = []
    for rule in rules:
        results.append((rule.name, await rule.evaluate(payload)))
    return results
