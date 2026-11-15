package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;
import co.edu.uco.ucokudos.service.input.port.GetProfilePort;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import java.util.UUID;

public class GetProfileUseCase implements GetProfilePort {

    private final UserRepository users;

    public GetProfileUseCase(final UserRepository users) {
        this.users = users;
    }

    @Override
    public ProfileResponse execute(final UUID userId) {
        return ProfileMapper.toResponse(users.findById(userId)
                .orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND)));
    }
}
