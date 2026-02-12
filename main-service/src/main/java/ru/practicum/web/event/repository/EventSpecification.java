package ru.practicum.web.event.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.web.event.entity.Event;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> publicEvents(
            Event.Status status,
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), status));

            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("annotation")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }

            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            if (Boolean.TRUE.equals(onlyAvailable)) {
                predicates.add(cb.or(
                        cb.equal(root.get("participantLimit"), 0),
                        cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}