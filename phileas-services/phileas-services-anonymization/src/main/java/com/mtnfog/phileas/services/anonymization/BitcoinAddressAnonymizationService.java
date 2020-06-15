package com.mtnfog.phileas.services.anonymization;

import com.mtnfog.phileas.model.services.AnonymizationCacheService;
import org.apache.commons.lang3.RandomStringUtils;

public class BitcoinAddressAnonymizationService extends AbstractAnonymizationService {

    public BitcoinAddressAnonymizationService(AnonymizationCacheService anonymizationCacheService) {
        super(anonymizationCacheService);
    }

    @Override
    public String anonymize(String token) {

        // See PHL-117: Just generating a random alphanumeric string.
        return RandomStringUtils.randomAlphanumeric(32);

    }

}
