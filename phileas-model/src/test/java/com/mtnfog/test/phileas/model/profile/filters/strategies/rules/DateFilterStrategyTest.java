package com.mtnfog.test.phileas.model.profile.filters.strategies.rules;

import com.mtnfog.phileas.model.profile.filters.strategies.AbstractFilterStrategy;
import com.mtnfog.phileas.model.profile.filters.strategies.rules.DateFilterStrategy;
import com.mtnfog.test.phileas.model.profile.filters.strategies.AbstractFilterStrategyTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateFilterStrategyTest extends AbstractFilterStrategyTest {

    private static final Logger LOGGER = LogManager.getLogger(DateFilterStrategyTest.class);

    @Override
    public AbstractFilterStrategy getFilterStrategy() {
        return new DateFilterStrategy();
    }

}
