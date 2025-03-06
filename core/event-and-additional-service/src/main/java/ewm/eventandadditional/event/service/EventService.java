package ewm.eventandadditional.event.service;

import ewm.interaction.dto.eventandadditional.event.EventFullDto;

public interface EventService {
    EventFullDto getBy(Long eventId, Long userId);

    EventFullDto getBy(long eventId);

}
