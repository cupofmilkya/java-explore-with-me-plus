package ru.practicum.publicApi.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.base.dao.EventRepository;
import ru.practicum.base.dto.event.EventFullDto;
import ru.practicum.base.dto.event.EventShortDto;
import ru.practicum.base.enums.State;
import ru.practicum.base.exceptions.ConflictException;
import ru.practicum.base.exceptions.NotFoundException;
import ru.practicum.base.mapper.EventMapper;
import ru.practicum.base.model.Event;
import ru.practicum.base.model.EventSearchCriteria;
import ru.practicum.base.utils.MyPageRequest;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.publicApi.dto.RequestParamForEvent;
import ru.practicum.statsclient.RestStatsClient;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicEventsServiceImpl implements PublicEventsService {
    private final EventRepository eventRepository;

    private final RestStatsClient statsClient;

    @Value("${service.name}")
    private String serviceName;


    @Transactional
    @Override
    public Set<EventShortDto> getAll(RequestParamForEvent param) {
        MyPageRequest pageable = createPageable(param.getSort(), param.getFrom(), param.getSize());
        EventSearchCriteria eventSearchCriteria = createCriteria(param);

        Set<EventShortDto> eventShorts = EventMapper.toEventShortDtoList(eventRepository
                .findAllWithFilters(pageable, eventSearchCriteria).toSet());

        log.info("Get events list size: {}", eventShorts.size());
        saveEndpointHit(param.getRequest());
        return eventShorts;
    }


    @Transactional
    @Override
    public EventFullDto get(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Event not found with id = %s", id)));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException(String.format("Event with id=%d is not published", id));
        }

        saveEndpointHit(request);
        log.info("Get event: {}", event.getId());
        event.setViews(event.getViews() + 1);
        eventRepository.flush();
        return EventMapper.toEventFullDto(event);
    }

    private void saveEndpointHit(HttpServletRequest request) {

        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app(serviceName)
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.hit(endpointHit);
    }

    private MyPageRequest createPageable(String sort, int from, int size) {
        MyPageRequest pageable = null;
        if (sort == null || sort.equalsIgnoreCase("EVENT_DATE")) {
            pageable = new MyPageRequest(from, size,
                    Sort.by(Sort.Direction.ASC, "event_date"));
        } else if (sort.equalsIgnoreCase("VIEWS")) {
            pageable = new MyPageRequest(from, size,
                    Sort.by(Sort.Direction.ASC, "views"));
        }
        return pageable;
    }

    private EventSearchCriteria createCriteria(RequestParamForEvent param) {
        return EventSearchCriteria.builder()
                .text(param.getText())
                .categories(param.getCategories())
                .rangeEnd(param.getRangeEnd())
                .rangeStart(param.getRangeStart())
                .paid(param.getPaid())
                .build();
    }
}
