package co.edu.uco.ucokudos.infrastructure.input.scheduler;

import co.edu.uco.ucokudos.UcoKudosApplication;
import co.edu.uco.ucokudos.infrastructure.output.message.RemoteMessageSource;
import co.edu.uco.ucokudos.service.input.dto.response.DispatchNotificationsResponse;
import co.edu.uco.ucokudos.service.input.port.DispatchNotificationsPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulersTest {

    @Test
    void theDispatchSchedulerRunsTheDispatchUseCaseOnEveryTick() {
        final AtomicInteger runs = new AtomicInteger();
        final DispatchNotificationsPort port = () -> {
            runs.incrementAndGet();
            return new DispatchNotificationsResponse();
        };
        final NotificationDispatchScheduler scheduler = new NotificationDispatchScheduler(port);

        scheduler.run();
        scheduler.run();

        assertEquals(2, runs.get());
    }

    @Test
    void theCatalogSchedulerRefreshesTheRemoteCatalogOnEveryTick() {
        final AtomicInteger refreshes = new AtomicInteger();
        final RemoteMessageSource source = new RemoteMessageSource("http://localhost:9/mensajes.json") {
            @Override
            public boolean refresh() {
                refreshes.incrementAndGet();
                return true;
            }
        };

        new MessageCatalogRefreshScheduler(source).run();

        assertEquals(1, refreshes.get());
    }

    /**
     * Which schedulers exist in an application started with these properties. They go as command-line arguments
     * because builder properties have the lowest priority and application.properties would override them.
     */
    private static Set<String> schedulersWith(final String... properties) {
        final String[] arguments = Arrays.stream(properties).map(property -> "--" + property).toArray(String[]::new);
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(UcoKudosApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run(arguments)) {
            final Set<String> found = new HashSet<>();
            if (!context.getBeansOfType(NotificationDispatchScheduler.class).isEmpty()) {
                found.add("dispatch");
            }
            if (!context.getBeansOfType(MessageCatalogRefreshScheduler.class).isEmpty()) {
                found.add("catalog");
            }
            return found;
        }
    }

    @Test
    void bothSchedulersAreOffUnlessTheyAreConfigured() {
        assertEquals(Set.of(), schedulersWith());
    }

    @Test
    void theyTurnOnWithTheirOwnConfiguration() {
        assertEquals(Set.of("dispatch"), schedulersWith("kudos.dispatch.enabled=true"));
        // an unreachable catalog is safe: a failed refresh only logs a warning and keeps the local texts
        assertEquals(Set.of("catalog"), schedulersWith("kudos.messages.url=http://localhost:9/mensajes.json"));
    }
}
