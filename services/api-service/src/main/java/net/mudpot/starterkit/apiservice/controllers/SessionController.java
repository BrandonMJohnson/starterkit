package net.mudpot.starterkit.apiservice.controllers;

import io.micronaut.context.BeanProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import net.mudpot.starterkit.apiservice.session.AnonymousSession;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionContext;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionResolver;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionResponse;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionService;

@Controller("/api/session")
public class SessionController {
    private final BeanProvider<AnonymousSessionContext> anonymousSessionContext;
    private final AnonymousSessionService anonymousSessionService;

    public SessionController(
        final BeanProvider<AnonymousSessionContext> anonymousSessionContext,
        final AnonymousSessionService anonymousSessionService
    ) {
        this.anonymousSessionContext = anonymousSessionContext;
        this.anonymousSessionService = anonymousSessionService;
    }

    @Get
    public MutableHttpResponse<AnonymousSessionResponse> currentSession() {
        final AnonymousSession session = anonymousSessionContext.get().session();
        return HttpResponse.ok(anonymousSessionService.toResponse(session));
    }
}
