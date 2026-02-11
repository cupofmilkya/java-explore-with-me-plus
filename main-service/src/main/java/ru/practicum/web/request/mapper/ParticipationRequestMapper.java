package ru.practicum.web.request.mapper;

import ru.practicum.web.request.dto.ParticipationRequestDto;
import ru.practicum.web.request.entity.ParticipationRequest;

import java.time.format.DateTimeFormatter;

public class ParticipationRequestMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated() != null ? request.getCreated().format(FORMATTER) : null)
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .build();
    }
}