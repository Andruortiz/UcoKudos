package co.edu.uco.ucokudos.service.input.dto.response;

import co.edu.uco.ucokudos.domain.model.Role;

import java.util.UUID;

public class RecipientResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }
}
