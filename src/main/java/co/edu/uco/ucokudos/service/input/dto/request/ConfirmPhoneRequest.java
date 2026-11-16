package co.edu.uco.ucokudos.service.input.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public class ConfirmPhoneRequest {

    private String code;

    @JsonIgnore
    private UUID userId;

    public String getCode() { return code; }
    public void setCode(final String code) { this.code = code; }
    public UUID getUserId() { return userId; }
    public void setUserId(final UUID userId) { this.userId = userId; }
}
