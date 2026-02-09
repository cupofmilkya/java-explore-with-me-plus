package ru.practicum.web.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.web.admin.dto.CategoryDto;
import ru.practicum.web.admin.dto.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private Boolean paid;
    private String eventDate;
    private UserShortDto initiator;
    private Long views;
    private Long confirmedRequests;
}