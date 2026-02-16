package ru.practicum.statsclient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация StatsClient на основе RestTemplate.
 * Важно: параметры дат должны быть URL-кодированы, формат строго "yyyy-MM-dd HH:mm:ss".
 */
public class RestStatsClient implements StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestStatsClient(RestTemplate restTemplate, String baseUrl) {
        Assert.notNull(restTemplate, "restTemplate must not be null");
        Assert.hasText(baseUrl, "baseUrl must not be blank");
        this.restTemplate = restTemplate;
        // Удаляем завершающий слэш, чтобы избежать двойных слэшей при сборке URL
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
    }

    @Override
    public void hit(EndpointHitDto dto) {
        Assert.notNull(dto, "EndpointHitDto must not be null");
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/hit")
                .build()
                .toUri();
        try {
            restTemplate.postForLocation(uri, dto);
        } catch (RestClientException ex) {
            throw new StatsClientException("Ошибка вызова POST /hit: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Assert.notNull(start, "start must not be null");
        Assert.notNull(end, "end must not be null");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String u : uris) {
                builder.queryParam("uris", u);
            }
        }

        URI uri = builder.build(true).toUri(); // true -> кодировать компоненты URI

        try {
            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {}
            );

            List<ViewStatsDto> body = response.getBody();
            return body != null ? body : new ArrayList<>();
        } catch (RestClientException ex) {
            throw new StatsClientException("Ошибка вызова GET /stats: " + ex.getMessage(), ex);
        }
    }
}
