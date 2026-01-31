package ru.practicum.base.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.base.model.Event;
import ru.practicum.base.model.EventSearchCriteria;

public interface EventCriteriaRepository {
    Page<Event> findAllWithFilters(Pageable pageable, EventSearchCriteria eventSearchCriteria);
}
