package ru.practicum.stats.web;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Примитивное in-memory хранилище для заглушечных контроллеров main-сервиса,
 * встроенных в stats-server для прохождения Postman-тестов в CI.
 */
@Component
public class InMemoryStubStore {

    private final List<UserStubDto> users = Collections.synchronizedList(new ArrayList<>());
    private final List<EventStubDto> events = Collections.synchronizedList(new ArrayList<>());
    private final List<CategoryStubDto> categories = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong userSeq = new AtomicLong(1);
    private final AtomicLong eventSeq = new AtomicLong(1);
    private final AtomicLong categorySeq = new AtomicLong(1);

    @PostConstruct
    public void seed() {
        // Предзаполним пару событий, чтобы GET /events имел данные
        createEvent(EventStubDto.builder().title("Sample Event 1").annotation("Demo 1").build());
        createEvent(EventStubDto.builder().title("Sample Event 2").annotation("Demo 2").build());
    }

    public UserStubDto createUser(UserStubDto dto) {
        UserStubDto created = UserStubDto.builder()
                .id(userSeq.getAndIncrement())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
        users.add(created);
        return created;
    }

    public List<UserStubDto> getUsers() {
        return new ArrayList<>(users);
    }

    public EventStubDto createEvent(EventStubDto dto) {
        EventStubDto created = EventStubDto.builder()
                .id(eventSeq.getAndIncrement())
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .build();
        events.add(created);
        return created;
    }

    public List<EventStubDto> getEvents() {
        return new ArrayList<>(events);
    }

    public Optional<EventStubDto> getEventById(long id) {
        return events.stream().filter(e -> e.getId() == id).findFirst();
    }

    public CategoryStubDto createCategory(CategoryStubDto dto) {
        CategoryStubDto created = CategoryStubDto.builder()
                .id(categorySeq.getAndIncrement())
                .name(dto.getName())
                .build();
        categories.add(created);
        return created;
    }

    public List<CategoryStubDto> getCategories() {
        return new ArrayList<>(categories);
    }
}