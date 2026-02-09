package ru.practicum.web.event.mapper;

import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;

public class EventMapper {

    public static EventDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setEventDate(event.getEventDate());

        if (event.getStatus() != null) {
            dto.setStatus(event.getStatus().name());
        }

        try {
            var viewsField = event.getClass().getDeclaredField("views");
            viewsField.setAccessible(true);
            Object viewsValue = viewsField.get(event);
            if (viewsValue instanceof Integer) {
                dto.setViews(((Integer) viewsValue).longValue());
            } else if (viewsValue instanceof Long) {
                dto.setViews((Long) viewsValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            dto.setViews(0L);
        }

        return dto;
    }

    public static Event toEntity(EventDto dto) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setEventDate(dto.getEventDate());

        if (dto.getStatus() != null) {
            try {
                event.setStatus(Event.Status.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                event.setStatus(Event.Status.PENDING);
            }
        } else {
            event.setStatus(Event.Status.PENDING);
        }

        return event;
    }

    public static void updateEntityFromDto(Event event, EventDto dto) {
        if (event == null || dto == null) {
            return;
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }

        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }

        if (dto.getStatus() != null) {
            try {
                event.setStatus(Event.Status.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                // Игнорируем некорректный статус
            }
        }
    }
}