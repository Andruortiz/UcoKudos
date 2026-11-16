package co.edu.uco.ucokudos.infrastructure.input.controller;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessRequestThrottle {

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final int MAX_KEYS = 10_000;

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private final Clock clock;

    public AccessRequestThrottle(final Clock clock) {
        this.clock = clock;
    }

    public boolean allow(final String key) {
        final Instant now = clock.instant();
        if (requests.size() > MAX_KEYS) {
            requests.clear();
        }
        final Deque<Instant> times = requests.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && times.peekFirst().isBefore(now.minus(WINDOW))) {
                times.pollFirst();
            }
            if (times.size() >= MAX_REQUESTS) {
                return false;
            }
            times.addLast(now);
            return true;
        }
    }
}
