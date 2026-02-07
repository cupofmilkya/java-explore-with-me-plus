package ru.practicum.stats.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Простейший DTO категории для заглушечных эндпоинтов в CI.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStubDto {
    private Long id;
    private String name;
}
