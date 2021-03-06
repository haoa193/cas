package org.apereo.cas.support.saml;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlIdPConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link SamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        SamlIdPConfiguration.class,
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreValidationConfiguration.class,
        CasCoreConfiguration.class,
        CoreSamlConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreUtilConfiguration.class})
public class SamlIdPConfigurationTests {

    @Test
    public void verifyConfig() throws Exception {
    }
}
