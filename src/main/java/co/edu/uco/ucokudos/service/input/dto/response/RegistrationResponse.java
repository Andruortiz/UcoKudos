package co.edu.uco.ucokudos.service.input.dto.response;

import co.edu.uco.ucokudos.domain.model.RegistrationStatus;

import java.util.UUID;

public record RegistrationResponse(UUID id, RegistrationStatus status) {
}
