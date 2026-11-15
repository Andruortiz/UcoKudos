package co.edu.uco.ucokudos.service.input.dto.response;

import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.UserStatus;

import java.util.UUID;

public class ProfileResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private boolean phoneVerified;
    private Role role;
    private UserStatus status;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(final String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(final String phone) { this.phone = phone; }
    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(final boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    public Role getRole() { return role; }
    public void setRole(final Role role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(final UserStatus status) { this.status = status; }
}
