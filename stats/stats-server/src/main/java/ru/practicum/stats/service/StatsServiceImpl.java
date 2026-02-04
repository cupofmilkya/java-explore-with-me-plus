package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
        // Простейшая защита от сохранения пустых записей
        if (dto == null) return;
        EndpointHit hit = EndpointHitDtoMapper.toEntity(dto);
        repository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            boolean unique
    ) {
        return unique
                ? repository.findUniqueStats(start, end)
                : repository.findStats(start, end);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, boolean unique, List<String> uris) {
        boolean filterByUris = !CollectionUtils.isEmpty(uris);
        if (unique) {
            return filterByUris
                    ? repository.findUniqueStatsByUris(start, end, uris)
                    : repository.findUniqueStats(start, end);
        } else {
            return filterByUris
                    ? repository.findStatsByUris(start, end, uris)
                    : repository.findStats(start, end);
        }
    }
}