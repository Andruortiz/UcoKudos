package co.edu.uco.ucokudos.service.input.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SubmitRegistrationRequest {

    private String name;
    private String email;

    @JsonIgnore
    private String directoryId;

    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(final String email) { this.email = email; }
    public String getDirectoryId() { return directoryId; }
    public void setDirectoryId(final String directoryId) { this.directoryId = directoryId; }
}
