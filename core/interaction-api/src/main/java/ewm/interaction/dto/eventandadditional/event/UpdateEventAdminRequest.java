package ewm.interaction.dto.eventandadditional.event;

import ewm.interaction.dto.eventandadditional.validation.EventDateInOneHour;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest extends UpdateEventRequest {
    AdminStateAction stateAction;
    @EventDateInOneHour
    LocalDateTime eventDate;
}
