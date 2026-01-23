package ru.practicum.stats.repository;

import ru.practicum.stats.model.EndpointHit;
import ru.practicum.dto.ViewStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(
            h.app,
            h.uri,
            COUNT(h.id)
        )
        FROM EndpointHit h
        WHERE h.timestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h.id) DESC
    """)
    List<ViewStatsDto> findStats(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(
            h.app,
            h.uri,
            COUNT(DISTINCT h.ip)
        )
        FROM EndpointHit h
        WHERE h.timestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY COUNT(DISTINCT h.ip) DESC
    """)
    List<ViewStatsDto> findUniqueStats(
            LocalDateTime start,
            LocalDateTime end
    );
}