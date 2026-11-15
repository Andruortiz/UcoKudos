package co.edu.uco.ucokudos.service.input.dto.request;

import co.edu.uco.ucokudos.domain.model.Role;

public class SubmitRegistrationRequest {

    private String name;
    private String email;
    private String phone;
    private Role role;

    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(final String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(final String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(final Role role) { this.role = role; }
}
