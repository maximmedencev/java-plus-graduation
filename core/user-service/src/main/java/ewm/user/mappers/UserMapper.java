package ewm.user.mappers;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import ewm.user.dto.UserShortDto;
import ewm.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toUser(NewUserRequest newUserRequest);

    User toUser(UserDto userDto);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);
}
