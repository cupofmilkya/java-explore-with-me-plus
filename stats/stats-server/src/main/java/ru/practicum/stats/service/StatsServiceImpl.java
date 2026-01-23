package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.EndpointHitDtoMapper;
import ru.practicum.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository repository;

    @Override
    public void saveHit(EndpointHitDto dto) {
        EndpointHit hit = EndpointHitDtoMapper.toEntity(dto);
        repository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            boolean unique
    ) {
        if (unique) {
            return repository.findUniqueStats(start, end);
        }
        return repository.findStats(start, end);
    }
}