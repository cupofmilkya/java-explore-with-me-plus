package ru.practicum.web.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private boolean pinned;

    @ElementCollection
    @CollectionTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"))
    @Column(name = "event_id")
    private List<Long> events;
}