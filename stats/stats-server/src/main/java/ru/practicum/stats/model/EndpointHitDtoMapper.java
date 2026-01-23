package ru.practicum.stats.model;

import ru.practicum.dto.EndpointHitDto;

public class EndpointHitDtoMapper {

    public static EndpointHitDto toDto(EndpointHit endpointHit) {
        if (endpointHit == null) {
            return null;
        }

        return EndpointHitDto.builder()
                .id(endpointHit.getId())
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }

        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}