package net.mudpot.starterkit.apiservice.session;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class SessionConfig {
    @Value("${starterkit.session.cookie-name}")
    private String cookieName;

    @Value("${starterkit.session.signing-secret}")
    private String signingSecret;

    @Value("${starterkit.session.cookie-secure}")
    private boolean cookieSecure;

    @Value("${starterkit.session.cookie-domain:}")
    private String cookieDomain;

    @Value("${starterkit.session.max-age-days}")
    private long maxAgeDays;

    public String cookieName() {
        return cookieName;
    }

    public String signingSecret() {
        return signingSecret;
    }

    public boolean cookieSecure() {
        return cookieSecure;
    }

    public String cookieDomain() {
        return cookieDomain;
    }

    public long maxAgeDays() {
        return maxAgeDays;
    }
}
