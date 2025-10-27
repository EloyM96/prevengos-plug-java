"""Declarative workflow scaffolding."""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Awaitable, Callable


StepFn = Callable[[dict], Awaitable[dict]]


@dataclass
class Workflow:
    """Declarative workflow definition."""

    name: str
    steps: list[StepFn] = field(default_factory=list)

    async def run(self, context: dict) -> dict:
        """Execute the workflow with the provided context."""
        state = context
        for step in self.steps:
            state = await step(state)
        return state

    def add_step(self, step: StepFn) -> "Workflow":
        """Append a new step to the workflow."""
        self.steps.append(step)
        return self
