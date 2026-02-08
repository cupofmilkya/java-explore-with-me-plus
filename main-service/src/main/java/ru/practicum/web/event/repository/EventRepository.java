package ru.practicum.web.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.web.event.entity.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(Event.Status status);

    boolean existsByCategoryId(Long categoryId);
}