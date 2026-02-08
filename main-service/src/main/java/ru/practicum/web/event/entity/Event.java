package ru.practicum.web.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String annotation;

    private LocalDateTime eventDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING,
        PUBLISHED,
        CANCELED
    }
}