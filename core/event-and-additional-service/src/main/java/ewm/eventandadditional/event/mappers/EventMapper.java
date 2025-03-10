package ewm.eventandadditional.event.mappers;

import ewm.eventandadditional.category.mapper.CategoryMapper;
import ewm.eventandadditional.category.mapper.User;
import ewm.eventandadditional.category.mapper.UserMapper;
import ewm.eventandadditional.category.model.Category;
import ewm.eventandadditional.event.model.Event;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.NewEventDto;
import ewm.interaction.dto.eventandadditional.event.UpdateEventAdminRequest;
import ewm.interaction.dto.eventandadditional.event.UpdateEventUserRequest;
import ewm.interaction.dto.user.UserShortDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, StateActionMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    Event toEvent(NewEventDto newEventDto, User initiator, Category category);

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "state", source = "updateEventUserRequest.stateAction")
    Event toUpdatedEvent(@MappingTarget Event event, UpdateEventUserRequest updateEventUserRequest, Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "state", source = "updateEventAdminRequest.stateAction")
    Event toUpdatedEvent(@MappingTarget Event event, UpdateEventAdminRequest updateEventAdminRequest, Category category);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator", source = "user")
    EventFullDto toEventFullDto(Event event, UserShortDto user);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator", source = "user")
    EventShortDto toEventShortDto(Event event, UserShortDto user);
}
