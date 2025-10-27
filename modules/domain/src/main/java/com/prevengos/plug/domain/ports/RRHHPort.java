package com.prevengos.plug.domain.ports;

import java.time.Instant;

public interface RRHHPort {
    void exportarPlantillaTrabajadores(Instant desde);
}
