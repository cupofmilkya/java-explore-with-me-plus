package ru.practicum.web.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.web.event.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    Optional<Event> findByIdAndStatus(Long id, Event.Status status);

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT e FROM Event e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:statuses IS NULL OR e.status IN :statuses) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(cast(:start AS timestamp) IS NULL OR e.eventDate >= :start) AND " +
            "(cast(:end AS timestamp) IS NULL OR e.eventDate <= :end)")
    Page<Event> findEventsByAdminFilters(
            @Param("users") List<Long> users,
            @Param("statuses") List<Event.Status> statuses,
            @Param("categories") List<Long> categories,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}