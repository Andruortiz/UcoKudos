package co.edu.uco.ucokudos.infrastructure.input.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * shortcut: local development only, no real login. Every request is authenticated as the user in the
 * X-Dev-User header (or kudos.dev.user-id). Replace with Microsoft Entra (ADR-0003) before any shared environment.
 */
@Configuration
@EnableWebSecurity
@Profile("local")
public class LocalSecurityConfiguration {

    @Bean
    SecurityFilterChain localFilterChain(final HttpSecurity http,
                                         @Value("${kudos.dev.user-id}") final String defaultUserId) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new DevUserFilter(defaultUserId), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    static class DevUserFilter extends OncePerRequestFilter {

        private final String defaultUserId;

        DevUserFilter(final String defaultUserId) {
            this.defaultUserId = defaultUserId;
        }

        @Override
        protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain chain) throws ServletException, IOException {
            final String header = request.getHeader("X-Dev-User");
            final String userId = header == null || header.isBlank() ? defaultUserId : header.trim();
            try {
                UUID.fromString(userId);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.NO_AUTHORITIES));
            } catch (final IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-Dev-User must be a UUID");
                return;
            }
            chain.doFilter(request, response);
        }
    }
}
