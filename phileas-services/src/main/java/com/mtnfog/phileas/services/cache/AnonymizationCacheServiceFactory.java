package com.mtnfog.phileas.services.cache;

import com.mtnfog.phileas.model.services.AnonymizationCacheService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Factory methods for getting an {@link AnonymizationCacheService}.
 */
public class AnonymizationCacheServiceFactory {

    private static final Logger LOGGER = LogManager.getLogger(AnonymizationCacheServiceFactory.class);

    private AnonymizationCacheServiceFactory() {
        // This is a utility class.
    }

    /**
     * Gets a configured {@link AnonymizationCacheService}.
     * @param properties Philter configuration {@link Properties}.
     * @return a configured {@link AnonymizationCacheService}.
     */
    public static AnonymizationCacheService getAnonymizationCacheService(Properties properties) {

        AnonymizationCacheService anonymizationCacheService = null;

        if(StringUtils.equalsIgnoreCase(properties.getProperty("anonymization.cache.service"), "redis")) {

            LOGGER.info("Configuring connection to Redis for anonymization cache service.");

            final String host = properties.getProperty("anonymization.cache.service.host");
            final int port = Integer.parseInt(properties.getProperty("anonymization.cache.service.host", "6379"));
            final String trustStore = properties.getProperty("anonymization.cache.service.truststore");

            try {

                anonymizationCacheService = new RedisAnonymizationCacheService(host, port, trustStore);

            } catch (Exception ex) {

                LOGGER.error("Unable to initialize the Redis cache client. A local anonymization cache service will be used instead.", ex);
                anonymizationCacheService = new LocalAnonymizationCacheService();

            }

        } else {

            LOGGER.info("Using local anonymization cache service.");

            anonymizationCacheService = new LocalAnonymizationCacheService();

        }

        return anonymizationCacheService;

    }

}
