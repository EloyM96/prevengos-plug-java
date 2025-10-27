package com.prevengos.plug.hubbackend.repository;

import com.prevengos.plug.hubbackend.domain.SyncEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface SyncEventRepository extends JpaRepository<SyncEvent, Long> {

    @Query("""
            SELECT e FROM SyncEvent e
            WHERE (:syncToken IS NULL OR e.syncToken > :syncToken)
              AND (:since IS NULL OR e.occurredAt >= :since)
            """)
    Page<SyncEvent> findNextEvents(@Param("syncToken") Long syncToken,
                                   @Param("since") OffsetDateTime since,
                                   Pageable pageable);
}
