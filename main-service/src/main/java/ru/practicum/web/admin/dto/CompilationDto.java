package ru.practicum.web.admin.dto;

import lombok.*;
import ru.practicum.web.event.entity.Event;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<Event> events;
}