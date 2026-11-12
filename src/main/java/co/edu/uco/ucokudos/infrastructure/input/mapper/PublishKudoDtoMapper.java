package co.edu.uco.ucokudos.infrastructure.input.mapper;

import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.service.input.dto.request.PublishKudoRequest;
import co.edu.uco.ucokudos.service.input.dto.response.NotificationResponse;
import co.edu.uco.ucokudos.service.input.dto.response.PublishKudoResponse;
import co.edu.uco.ucokudos.service.usecase.PublishKudoCommand;
import co.edu.uco.ucokudos.service.usecase.PublishKudoResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PublishKudoDtoMapper {

    PublishKudoCommand toCommand(PublishKudoRequest request, UUID authorId, List<Attachment> attachments,
                                 String idempotencyKey);

    @Mapping(target = "id", source = "kudo.id")
    @Mapping(target = "status", source = "kudo.status")
    @Mapping(target = "createdAt", source = "kudo.createdAt")
    PublishKudoResponse toResponse(PublishKudoResult result);

    NotificationResponse toNotificationResponse(Notification notification);
}
