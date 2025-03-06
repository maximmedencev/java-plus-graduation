package ewm.eventandadditional.event.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.client.StatRestClientImpl;
import ewm.dto.ViewStatsDto;
import ewm.eventandadditional.category.model.QCategory;
import ewm.eventandadditional.event.mappers.EventMapper;
import ewm.eventandadditional.event.model.Event;
import ewm.eventandadditional.event.model.QEvent;
import ewm.eventandadditional.event.repository.EventRepository;
import ewm.eventandadditional.event.service.PublicEventService;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.EventState;
import ewm.interaction.dto.eventandadditional.event.PublicEventParam;
import ewm.interaction.dto.request.RequestStatus;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.feign.RequestFeignClient;
import ewm.interaction.feign.UserFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicEventServiceImpl implements PublicEventService {
    final EventRepository eventRepository;
    final RequestFeignClient requestFeignClient;
    final StatRestClientImpl statRestClient;
    final EventMapper eventMapper;
    final JPAQueryFactory jpaQueryFactory;
    final UserFeignClient userFeignClient;

    private static final int TIME_BEFORE = 10;

    @Override
    public List<EventShortDto> getAllBy(PublicEventParam eventParam, Pageable pageRequest) {
        BooleanBuilder eventQueryExpression = buildExpression(eventParam);
        List<EventShortDto> events = getEvents(pageRequest, eventQueryExpression);
        List<Long> eventIds = events.stream().map(EventShortDto::getId).toList();
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);

        Set<String> uris = events.stream()
                .map(event -> "/events/" + event.getId()).collect(Collectors.toSet());

        LocalDateTime start = events
                .stream()
                .min(Comparator.comparing(EventShortDto::getEventDate))
                .orElseThrow(() -> new NotFoundException("Даты не заданы"))
                .getEventDate();

        Map<String, Long> viewMap = statRestClient
                .stats(start, LocalDateTime.now(), uris.stream().toList(), false).stream()
                .collect(Collectors.groupingBy(ViewStatsDto::getUri, Collectors.summingLong(ViewStatsDto::getHits)));

        return events.stream().peek(shortDto -> {
            shortDto.setViews(viewMap.getOrDefault("/events/" + shortDto.getId(), 0L));
            shortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(shortDto.getId(), 0L));
        }).toList();
    }

    @Override
    public EventFullDto getBy(long eventId) {
        EventFullDto event = eventRepository.findById(eventId).map(eventMapper::toEventFullDto)
                .orElseThrow(() -> new NotFoundException("Мероприятие с Id =" + eventId + " не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие id = " + eventId + " не опубликовано");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusYears(TIME_BEFORE);

        statRestClient.stats(start, now, List.of("/events/" + eventId), true)
                .forEach(viewStatsDto -> event.setViews(viewStatsDto.getHits()));

        long confirmedRequests = requestFeignClient.countAllByEventIdAndStatusIs(eventId,
                RequestStatus.CONFIRMED.toString());
        event.setConfirmedRequests(confirmedRequests);
        return event;
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        return requestFeignClient.getConfirmedRequestMap(eventIds);
    }

    private List<EventShortDto> getEvents(Pageable pageRequest, BooleanBuilder eventQueryExpression) {
        List<Event> events = jpaQueryFactory
                .selectFrom(QEvent.event)
                .leftJoin(QEvent.event.category, QCategory.category)
                .fetchJoin()
                .where(eventQueryExpression)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .stream()
                .toList();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        Map<Long, UserShortDto> initiators = userFeignClient.userMapBy(initiatorIds);

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, initiators.get(event.getInitiatorId())))
                .toList();
    }

    private BooleanBuilder buildExpression(PublicEventParam eventParam) {
        BooleanBuilder eventQueryExpression = new BooleanBuilder();

        eventQueryExpression.and(QEvent.event.state.eq(EventState.PUBLISHED));
        Optional.ofNullable(eventParam.getRangeStart())
                .ifPresent(rangeStart -> eventQueryExpression.and(QEvent.event.eventDate.after(rangeStart)));
        Optional.ofNullable(eventParam.getRangeEnd())
                .ifPresent(rangeEnd -> eventQueryExpression.and(QEvent.event.eventDate.before(eventParam.getRangeEnd())));
        Optional.ofNullable(eventParam.getPaid())
                .ifPresent(paid -> eventQueryExpression.and(QEvent.event.paid.eq(paid)));
        Optional.ofNullable(eventParam.getCategories())
                .filter(category -> !category.isEmpty())
                .ifPresent(category -> eventQueryExpression.and(QEvent.event.category.id.in(category)));
        Optional.ofNullable(eventParam.getText())
                .filter(text -> !text.isEmpty()).ifPresent(text -> {
                    eventQueryExpression.and(QEvent.event.annotation.containsIgnoreCase(text));
                    eventQueryExpression.or(QEvent.event.description.containsIgnoreCase(text));
                });
        return eventQueryExpression;
    }
}
