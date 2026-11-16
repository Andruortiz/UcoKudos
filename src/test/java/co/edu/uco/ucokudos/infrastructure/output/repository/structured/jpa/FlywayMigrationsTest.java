package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Applies the migrations to an empty PostgreSQL and lets Hibernate validate the result against the entities, so a
 * migration that drifts from the code fails here and not in production. It needs a real, empty database:
 * <pre>FLYWAY_CHECK_DB_URL=jdbc:postgresql://localhost:5432/kudos_check ./mvnw test -Dtest=FlywayMigrationsTest</pre>
 * Without that variable it is skipped, which is what happens in the CI (the other tests use H2).
 */
@EnabledIfEnvironmentVariable(named = "FLYWAY_CHECK_DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "spring.datasource.url=${FLYWAY_CHECK_DB_URL}",
        "spring.datasource.username=${FLYWAY_CHECK_DB_USER:kudos}",
        "spring.datasource.password=${FLYWAY_CHECK_DB_PASSWORD:kudos}",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.jpa.hibernate.ddl-auto=validate"})
class FlywayMigrationsTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void theMigrationsBuildASchemaThatMatchesTheEntities() {
        // reaching this point means Flyway applied every script and Hibernate validated the tables and columns
        final Integer applied = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);
        assertTrue(applied != null && applied >= 1);
    }

    @Test
    void everyEnvironmentGetsTheInitialCategories() {
        assertEquals(5, jdbc.queryForObject("select count(*) from category where active", Integer.class));
    }

    @Test
    void theAuditActionsIncludeThePhoneUpdate() {
        jdbc.update("insert into audit_record (id, occurred_at, actor_id, action, resource_type, resource_id, "
                + "correlation_id) values (gen_random_uuid(), now(), null, 'PHONE_UPDATED', 'USER', gen_random_uuid(), 'x')");

        assertEquals(1, jdbc.queryForObject(
                "select count(*) from audit_record where action = 'PHONE_UPDATED'", Integer.class));
    }
}
