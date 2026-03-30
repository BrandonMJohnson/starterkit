package net.mudpot.starterkit.apiservice.session;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.session.Session;

import java.util.Optional;

@Singleton
public class SessionArgumentBinder implements TypedRequestArgumentBinder<Session> {
    private final SessionResolver sessionResolver;

    public SessionArgumentBinder(final SessionResolver sessionResolver) {
        this.sessionResolver = sessionResolver;
    }

    @Override
    public Argument<Session> argumentType() {
        return Argument.of(Session.class);
    }

    @Override
    public ArgumentBinder.BindingResult<Session> bind(final ArgumentConversionContext<Session> context, final HttpRequest<?> source) {
        return () -> Optional.of(sessionResolver.resolve(source));
    }
}
