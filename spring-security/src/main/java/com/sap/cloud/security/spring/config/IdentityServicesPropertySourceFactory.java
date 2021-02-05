package com.sap.cloud.security.spring.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.sap.cloud.security.config.Environment;
import com.sap.cloud.security.config.Environments;
import com.sap.cloud.security.config.cf.CFEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;


/**
 * Part of Auto Configuration {@link com.sap.cloud.security.spring.autoconfig.HybridIdentityServicesAutoConfiguration}
 *
 * <h2>Example Usage</h2>
 *
 * <pre class="code">
 * declared on a class:
 *
 * &#64;Configuration
 * &#64;PropertySource(factory = IdentityServicesPropertySourceFactory.class, value = { "" })
 *
 * declared on attribute:
 *
 * &#64;Value("${xsuaa.url:}")
 * </pre>
 *
 */
public class IdentityServicesPropertySourceFactory implements PropertySourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(IdentityServicesPropertySourceFactory.class);

    protected static final String PREFIX = "sap.security.services";
    protected static final String XSUAA_PREFIX = "sap.security.services.xsuaa.";
    protected static final String IAS_PREFIX = "sap.security.services.identity.";

    private static final List<String> XSUAA_ATTRIBUTES = Arrays
            .asList(new String[] { "clientid", "clientsecret", "identityzoneid",
                    "sburl", "tenantid", "tenantmode", "uaadomain", "url", "verificationkey", "xsappname",
                    "certificate",
                    "key" });

    private static final List<String> IAS_ATTRIBUTES = Arrays
            .asList(new String[] { "clientid", "clientsecret", "domain", "url"});

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Environment environment = CFEnvironment.getInstance();
        if (resource != null && resource.getResource().getFilename() != null
                && !resource.getResource().getFilename().isEmpty()) {
            environment = Environments.getCurrent(resource.getResource().getInputStream());
        }
        Properties properties = new Properties();
        boolean multipleXsuaaServicesBound = environment.getNumberOfXsuaaConfigurations() > 1;

        if (environment.getXsuaaConfiguration() != null) {
            String xsuaaPrefix = multipleXsuaaServicesBound ? PREFIX + ".xsuaa[0]." : XSUAA_PREFIX;
            for (String key : XSUAA_ATTRIBUTES) {
                if (environment.getXsuaaConfiguration().hasProperty(key)) {
                    properties.put(xsuaaPrefix + key, environment.getXsuaaConfiguration().getProperty(key));
                }
            }
        }
        if (multipleXsuaaServicesBound) {
            for (String key : XSUAA_ATTRIBUTES) {
                if (environment.getXsuaaConfigurationForTokenExchange().hasProperty(key)) {
                    properties.put(PREFIX + ".xsuaa[1]." + key, environment.getXsuaaConfigurationForTokenExchange().getProperty(key));
                }
            }
        }
        if (environment.getIasConfiguration() != null) {
            for (String key : IAS_ATTRIBUTES) {
                if (environment.getIasConfiguration().hasProperty(key)) {
                    properties.put(IAS_PREFIX + key, environment.getIasConfiguration().getProperty(key));
                }
            }
        }
        logger.info("Parsed {} properties from identity services.", properties.size());
        return new PropertiesPropertySource(PREFIX, properties);
    }


}