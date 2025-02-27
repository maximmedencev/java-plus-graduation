package ewm.client;

import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StatRestClientImpl implements StatRestClient {
    private final StatsFeignClient statsFeignClient;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void addHit(EndpointHitDto hitDto) {
        try {
            statsFeignClient.hit(hitDto);
        } catch (Exception e) {
            log.info("Ошибка при обращении к эндпоинту /hit {}", e.getMessage(), e);
        }
    }

    public List<ViewStatsDto> stats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            return statsFeignClient.stats(start.format(formatter), end.format(formatter), uris, unique);
        } catch (Exception e) {
            log.info("Ошибка при запросе к эндпоинту /stats {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
