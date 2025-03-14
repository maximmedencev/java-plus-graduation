package ewm.client.grpcclient;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import ru.practicum.grpc.stats.recommendation.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.RecommendationsMessages;

import java.util.Iterator;

@Slf4j
public class AnalyzerClient {
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub blockingStub;

    public AnalyzerClient(@GrpcClient("analyzer") RecommendationsControllerGrpc
            .RecommendationsControllerBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public void getSimilarEvents(long eventId, long userId, int maxResults) {
        RecommendationsMessages.SimilarEventsRequestProto similarRequest = RecommendationsMessages
                .SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        try {
            Iterator<RecommendationsMessages.RecommendedEventProto> responseIterator = blockingStub
                    .getSimilarEvents(similarRequest);

            while (responseIterator.hasNext()) {
                RecommendationsMessages.RecommendedEventProto event = responseIterator.next();
                log.info("Event ID: {}, Score: {}", event.getEventId(), event.getScore());
            }
        } catch (StatusRuntimeException e) {
            log.info("Неудачный вызов gRPC: {}", e.getStatus());
        }
    }

    public void getRecommendationsForUser(long userId, int maxResults) {
        RecommendationsMessages.UserPredictionsRequestProto userRequest = RecommendationsMessages
                .UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        try {
            Iterator<RecommendationsMessages.RecommendedEventProto> responseIterator = blockingStub
                    .getRecommendationsForUser(userRequest);

            while (responseIterator.hasNext()) {
                RecommendationsMessages.RecommendedEventProto event = responseIterator.next();
                log.info("Event ID: {}, Score: {}", event.getEventId(), event.getScore());
            }
        } catch (StatusRuntimeException e) {
            log.error("Неудачный вызов gRPC" + e.getStatus());
        }
    }

    public void getInteractionsCount(Iterable<Long> eventIds) {
        RecommendationsMessages.InteractionsCountRequestProto interactionsRequest = RecommendationsMessages
                .InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        try {
            Iterator<RecommendationsMessages.RecommendedEventProto> responseIterator = blockingStub
                    .getInteractionsCount(interactionsRequest);

            while (responseIterator.hasNext()) {
                RecommendationsMessages.RecommendedEventProto event = responseIterator.next();
                log.info("Event ID: {}, Score: {}", event.getEventId(), event.getScore());
            }
        } catch (StatusRuntimeException e) {
            log.info("Неудачный вызов gRPC: {}", e.getStatus());
        }
    }
}
