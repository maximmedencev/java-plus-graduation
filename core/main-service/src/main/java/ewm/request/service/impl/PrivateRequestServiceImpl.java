package ewm.request.service.impl;

import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.request.dto.EventRequestStatusUpdateRequest;
import ewm.request.dto.EventRequestStatusUpdateResult;
import ewm.request.dto.ParticipationRequestDto;
import ewm.request.mapper.RequestMapper;
import ewm.request.model.ParticipationRequest;
import ewm.request.model.RequestStatus;
import ewm.request.repository.RequestRepository;
import ewm.request.service.PrivateRequestService;
import ewm.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrivateRequestServiceImpl implements PrivateRequestService {
    final RequestRepository requestRepository;
    final EventRepository eventRepository;
    final UserRepository userRepository;
    final RequestMapper requestMapper;


    @Override
    public List<ParticipationRequestDto> getReceivedBy(long userId, long eventId) {
        return requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId).stream()
                .map(requestMapper::toParticipantRequestDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult update(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с Id = " + userId + " не найден"));
        Event event = eventRepository.findById(eventId).filter(event1 -> event1.getInitiator().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Событие с Id = " + eventId + " не найден"));

        List<Long> requestsIds = updateRequest.getRequestIds();
        long confirmedRequests = requestRepository.countAllByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED);
        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventIdIs(requestsIds, eventId);
        long limit = event.getParticipantLimit() - confirmedRequests;
        if (limit == 0) {
            throw new ConflictException("Количество подтвержденных запросов исчерпано: " + confirmedRequests);
        }
        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new IllegalArgumentException("Не все запросы были найдены. Ошибка при вводе Ids");
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> {
                while (limit-- > 0 && !requests.isEmpty()) {
                    ParticipationRequest request = requests.removeFirst();
                    if (request.getStatus().equals(RequestStatus.PENDING)) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        requestRepository.save(request);
                        confirmed.add(request);
                    }
                }
            }
            case REJECTED ->
                    requests.forEach(participationRequest -> participationRequest.setStatus(RequestStatus.REJECTED));
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed.stream().map(requestMapper::toParticipantRequestDto).toList());
        result.setRejectedRequests(requests.stream().map(requestMapper::toParticipantRequestDto).toList());
        return result;
    }
}
