package ewm.eventandadditional.compilation.mappers;

import ewm.eventandadditional.compilation.model.Compilation;
import ewm.eventandadditional.event.mappers.EventMapper;
import ewm.eventandadditional.event.model.Event;
import ewm.interaction.dto.eventandadditional.compilation.CompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.NewCompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.UpdateCompilationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {

    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "compilationDto.title")
    @Mapping(target = "pinned", source = "compilationDto.pinned")
    @Mapping(target = "events", source = "events")
    Compilation toUpdateCompilation(
            @MappingTarget Compilation compilation,
            UpdateCompilationRequest compilationDto,
            List<Event> events
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation toCompilation(NewCompilationDto compilationDto, List<Event> events);
}
