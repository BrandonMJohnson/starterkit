package net.mudpot.starterkit.apiservice;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/api/session")
public class SessionController {
    private final AnonymousSessionService anonymousSessionService;

    public SessionController(final AnonymousSessionService anonymousSessionService) {
        this.anonymousSessionService = anonymousSessionService;
    }

    @Get
    public MutableHttpResponse<AnonymousSessionResponse> currentSession(final HttpRequest<?> request) {
        final AnonymousSession session = anonymousSessionService.ensureSession(request);
        return anonymousSessionService.attachCookieIfNeeded(
            HttpResponse.ok(anonymousSessionService.toResponse(session)),
            session
        );
    }
}
