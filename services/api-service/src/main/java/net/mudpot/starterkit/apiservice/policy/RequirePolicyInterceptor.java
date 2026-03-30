package net.mudpot.starterkit.apiservice.policy;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.apiservice.session.AnonymousSession;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionContext;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;

@Singleton
@InterceptorBean(RequirePolicy.class)
@ExecuteOn(TaskExecutors.BLOCKING)
public class RequirePolicyInterceptor implements MethodInterceptor<Object, Object> {
    private final BeanProvider<AnonymousSessionContext> anonymousSessionContext;
    private final PolicyInputFactory policyInputFactory;
    private final PolicyEvaluator policyEvaluator;

    public RequirePolicyInterceptor(
        final BeanProvider<AnonymousSessionContext> anonymousSessionContext,
        final PolicyInputFactory policyInputFactory,
        final PolicyEvaluator policyEvaluator
    ) {
        this.anonymousSessionContext = anonymousSessionContext;
        this.policyInputFactory = policyInputFactory;
        this.policyEvaluator = policyEvaluator;
    }

    @Override
    public Object intercept(final MethodInvocationContext<Object, Object> context) {
        final String action = context.stringValue(RequirePolicy.class).orElse("").trim();
        if (action.isBlank()) {
            return context.proceed();
        }

        final io.micronaut.http.HttpRequest<?> request = ServerRequestContext.currentRequest()
            .orElseThrow(() -> new HttpStatusException(HttpStatus.FORBIDDEN, "Policy denied: request-context-missing"));
        final AnonymousSession session = anonymousSessionContext.get().session();
        final PolicyEvaluationResult result = policyEvaluator.evaluate(
            new PolicyEvaluationRequest(action, policyInputFactory.build(request, session))
        );
        if (!result.allowed()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Policy denied: " + result.reason());
        }

        return context.proceed();
    }
}
