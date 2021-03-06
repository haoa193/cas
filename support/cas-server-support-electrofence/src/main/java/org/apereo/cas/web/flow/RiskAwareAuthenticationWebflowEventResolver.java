package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasRiskBasedAuthenticationEvaluationStartedEvent;
import org.apereo.cas.support.events.CasRiskBasedAuthenticationMitigationStartedEvent;
import org.apereo.cas.support.events.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.CasRiskyAuthenticationMitigatedEvent;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskAwareAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    private final AuthenticationRiskEvaluator authenticationRiskEvaluator;
    private final AuthenticationRiskMitigator authenticationRiskMitigator;
    private final double threshold;

    public RiskAwareAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                       final CentralAuthenticationService centralAuthenticationService, final ServicesManager servicesManager,
                                                       final TicketRegistrySupport ticketRegistrySupport, final CookieGenerator warnCookieGenerator,
                                                       final List<AuthenticationRequestServiceSelectionStrategy> authenticationSelectionStrategies,
                                                       final MultifactorAuthenticationProviderSelector selector,
                                                       final AuthenticationRiskEvaluator authenticationRiskEvaluator,
                                                       final AuthenticationRiskMitigator authenticationRiskMitigator,
                                                       final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
        this.authenticationRiskEvaluator = authenticationRiskEvaluator;
        this.authenticationRiskMitigator = authenticationRiskMitigator;
        threshold = casProperties.getAuthn().getAdaptive().getRisk().getThreshold();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        return handlePossibleSuspiciousAttempt(request, authentication, service);
    }

    /**
     * Handle possible suspicious attempt.
     *
     * @param request        the request
     * @param authentication the authentication
     * @param service        the service
     * @return the set
     */
    protected Set<Event> handlePossibleSuspiciousAttempt(final HttpServletRequest request, final Authentication authentication,
                                                         final RegisteredService service) {

        this.eventPublisher.publishEvent(new CasRiskBasedAuthenticationEvaluationStartedEvent(this, authentication, service));
        
        logger.debug("Evaluating possible suspicious authentication attempt for {}", authentication.getPrincipal());
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);

        if (score.isRiskGreaterThan(threshold)) {
            this.eventPublisher.publishEvent(new CasRiskyAuthenticationDetectedEvent(this, authentication, service, score));

            logger.debug("Calculated risk score {} for authentication request by {} is above the risk threshold {}.",
                    score.getScore(),
                    authentication.getPrincipal(),
                    threshold);

            this.eventPublisher.publishEvent(new CasRiskBasedAuthenticationMitigationStartedEvent(this, authentication, service, score));
            final AuthenticationRiskContingencyResponse res = authenticationRiskMitigator.mitigate(authentication, service, score, request);
            this.eventPublisher.publishEvent(new CasRiskyAuthenticationMitigatedEvent(this, authentication, service, res));
            
            return Collections.singleton(res.getResult());
        }

        logger.debug("Authentication request for {} is below the risk threshold", authentication.getPrincipal());
        return null;
    }
}
