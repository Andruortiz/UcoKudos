package co.edu.uco.ucokudos.service.input.dto.request;

import java.util.UUID;

public class UpdatePhoneRequest {

    private UUID userId;
    private String phone;

    public UUID getUserId() { return userId; }
    public void setUserId(final UUID userId) { this.userId = userId; }
    public String getPhone() { return phone; }
    public void setPhone(final String phone) { this.phone = phone; }
}
