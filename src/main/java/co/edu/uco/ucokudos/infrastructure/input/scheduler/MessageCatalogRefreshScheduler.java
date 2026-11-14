package co.edu.uco.ucokudos.infrastructure.input.scheduler;

import co.edu.uco.ucokudos.infrastructure.output.message.RemoteMessageSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("!'${kudos.messages.url:}'.isEmpty()")
public class MessageCatalogRefreshScheduler {

    private final RemoteMessageSource messageSource;

    public MessageCatalogRefreshScheduler(final RemoteMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${kudos.messages.refresh-interval}")
    public void run() {
        messageSource.refresh();
    }
}
