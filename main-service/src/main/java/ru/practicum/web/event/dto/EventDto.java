package ru.practicum.web.event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventDto {
    private Long id;
    private String title;
    private String annotation;
    private LocalDateTime eventDate;
    private String status;
}