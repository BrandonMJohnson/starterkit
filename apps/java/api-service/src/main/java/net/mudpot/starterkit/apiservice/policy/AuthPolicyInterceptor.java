package net.mudpot.starterkit.apiservice.policy;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.apiservice.session.SessionResolver;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;
import net.mudpot.starterkit.commons.session.Session;

@Singleton
@InterceptorBean(AuthPolicy.class)
@ExecuteOn(TaskExecutors.BLOCKING)
public class AuthPolicyInterceptor implements MethodInterceptor<Object, Object> {
    private final SessionResolver sessionResolver;
    private final PolicyInputFactory policyInputFactory;
    private final PolicyEvaluator policyEvaluator;

    public AuthPolicyInterceptor(
        final SessionResolver sessionResolver,
        final PolicyInputFactory policyInputFactory,
        final PolicyEvaluator policyEvaluator
    ) {
        this.sessionResolver = sessionResolver;
        this.policyInputFactory = policyInputFactory;
        this.policyEvaluator = policyEvaluator;
    }

    @Override
    public Object intercept(final MethodInvocationContext<Object, Object> context) {
        final String action = context.stringValue(AuthPolicy.class).orElse("").trim();
        if (action.isBlank()) {
            return context.proceed();
        }

        final io.micronaut.http.HttpRequest<?> request = ServerRequestContext.currentRequest()
            .orElseThrow(() -> new HttpStatusException(HttpStatus.FORBIDDEN, "Policy denied: request-context-missing"));
        final Session session = sessionResolver.resolve(request);
        final PolicyEvaluationResult result = policyEvaluator.evaluate(
            new PolicyEvaluationRequest(action, policyInputFactory.build(request, session))
        );
        if (!result.allowed()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Policy denied: " + result.reason());
        }

        return context.proceed();
    }
}
