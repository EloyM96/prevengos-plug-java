package com.prevengos.plug.domain.events;

@FunctionalInterface
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
