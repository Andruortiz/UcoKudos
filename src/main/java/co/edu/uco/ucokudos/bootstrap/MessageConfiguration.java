package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.infrastructure.output.message.RemoteMessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageConfiguration {

    @Bean
    RemoteMessageSource messageSource(@Value("${kudos.messages.url:}") final String url) {
        final ResourceBundleMessageSource bundle = new ResourceBundleMessageSource();
        bundle.setBasename("messages");
        bundle.setDefaultEncoding("UTF-8");
        bundle.setFallbackToSystemLocale(false);
        final RemoteMessageSource remote = new RemoteMessageSource(url);
        remote.setParentMessageSource(bundle);
        return remote;
    }
}
