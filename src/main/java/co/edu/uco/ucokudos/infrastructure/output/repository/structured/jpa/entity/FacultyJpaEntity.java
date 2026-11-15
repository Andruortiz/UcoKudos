package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "faculty",
        uniqueConstraints = @UniqueConstraint(name = "uk_faculty_name", columnNames = "name"))
public class FacultyJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "dean_id")
    private UUID deanId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeanId() {
        return deanId;
    }

    public void setDeanId(UUID deanId) {
        this.deanId = deanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
