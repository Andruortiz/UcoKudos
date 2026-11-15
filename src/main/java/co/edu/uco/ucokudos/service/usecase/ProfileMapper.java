package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;

final class ProfileMapper {

    private ProfileMapper() {
    }

    static ProfileResponse toResponse(final User user) {
        final ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setPhoneVerified(user.isPhoneVerified());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        return response;
    }
}
