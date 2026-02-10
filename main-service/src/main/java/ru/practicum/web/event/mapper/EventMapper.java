package ru.practicum.web.event.mapper;

import ru.practicum.web.admin.dto.CategoryDto;
import ru.practicum.web.admin.dto.UserShortDto;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.dto.EventShortDto;
import ru.practicum.web.event.entity.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());

        if (event.getEventDate() != null) {
            dto.setEventDate(event.getEventDate().format(FORMATTER));
        }

        if (event.getStatus() != null) {
            dto.setState(event.getStatus().name());
        }

        dto.setPaid(event.getPaid() != null ? event.getPaid() : false);
        dto.setParticipantLimit(event.getParticipantLimit() != null ? event.getParticipantLimit() : 0);
        dto.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);

        if (event.getCreatedOn() != null) {
            dto.setCreatedOn(event.getCreatedOn().format(FORMATTER));
        }

        if (event.getPublishedOn() != null) {
            dto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        }

        if (event.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(event.getCategory().getId());
            categoryDto.setName(event.getCategory().getName());
            dto.setCategory(categoryDto);
        }

        if (event.getInitiator() != null) {
            UserShortDto userDto = new UserShortDto();
            userDto.setId(event.getInitiator().getId());
            userDto.setName(event.getInitiator().getName());
            dto.setInitiator(userDto);
        }

        try {
            var viewsField = event.getClass().getDeclaredField("views");
            viewsField.setAccessible(true);
            Object viewsValue = viewsField.get(event);
            if (viewsValue instanceof Integer) {
                dto.setViews(((Integer) viewsValue).longValue());
            } else if (viewsValue instanceof Long) {
                dto.setViews((Long) viewsValue);
            } else {
                dto.setViews(0L);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            dto.setViews(0L);
        }

        dto.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L);

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
        event.setDescription(dto.getDescription());

        if (dto.getEventDate() != null && !dto.getEventDate().isEmpty()) {
            try {
                event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
            } catch (Exception e) {
                try {
                    event.setEventDate(LocalDateTime.parse(dto.getEventDate()));
                } catch (Exception e2) {
                    throw new IllegalArgumentException("Invalid date format: " + dto.getEventDate());
                }
            }
        }

        if (dto.getState() != null) {
            try {
                event.setStatus(Event.Status.valueOf(dto.getState()));
            } catch (IllegalArgumentException e) {
                event.setStatus(Event.Status.PENDING);
            }
        } else {
            event.setStatus(Event.Status.PENDING);
        }

        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        if (dto.getCreatedOn() != null && !dto.getCreatedOn().isEmpty()) {
            try {
                event.setCreatedOn(LocalDateTime.parse(dto.getCreatedOn(), FORMATTER));
            } catch (Exception e) {
                // Игнорируем ошибку парсинга
            }
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

        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }

        if (dto.getEventDate() != null && !dto.getEventDate().isEmpty()) {
            try {
                event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
            } catch (Exception e) {
                // Игнорируем ошибку парсинга
            }
        }

        if (dto.getState() != null) {
            try {
                event.setStatus(Event.Status.valueOf(dto.getState()));
            } catch (IllegalArgumentException e) {
                // Игнорируем некорректный статус
            }
        }

        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }

        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
    }

    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());

        if (event.getEventDate() != null) {
            dto.setEventDate(event.getEventDate().format(FORMATTER));
        }

        dto.setPaid(event.getPaid() != null ? event.getPaid() : false);

        if (event.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(event.getCategory().getId());
            categoryDto.setName(event.getCategory().getName());
            dto.setCategory(categoryDto);
        }

        if (event.getInitiator() != null) {
            UserShortDto userDto = new UserShortDto();
            userDto.setId(event.getInitiator().getId());
            userDto.setName(event.getInitiator().getName());
            dto.setInitiator(userDto);
        }

        try {
            var viewsField = event.getClass().getDeclaredField("views");
            viewsField.setAccessible(true);
            Object viewsValue = viewsField.get(event);
            if (viewsValue instanceof Integer) {
                dto.setViews(((Integer) viewsValue).longValue());
            } else if (viewsValue instanceof Long) {
                dto.setViews((Long) viewsValue);
            } else {
                dto.setViews(0L);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            dto.setViews(0L);
        }

        dto.setConfirmedRequests(event.getConfirmedRequests() != null ?
                event.getConfirmedRequests() : 0L);

        return dto;
    }
}