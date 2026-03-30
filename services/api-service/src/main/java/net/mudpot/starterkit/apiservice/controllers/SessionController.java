package net.mudpot.starterkit.apiservice.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import net.mudpot.starterkit.apiservice.session.SessionResponse;
import net.mudpot.starterkit.apiservice.session.SessionService;
import net.mudpot.starterkit.commons.session.Session;

@Controller("/api/session")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Get
    public SessionResponse currentSession(final Session session) {
        return sessionService.toResponse(session);
    }
}
