package com.mtnfog.test.phileas.model.profile.filters.strategies.rules;

import com.mtnfog.phileas.model.profile.filters.strategies.AbstractFilterStrategy;
import com.mtnfog.phileas.model.profile.filters.strategies.rules.PassportNumberFilterStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PassportNumberFilterStrategyTest {

    private static final Logger LOGGER = LogManager.getLogger(PassportNumberFilterStrategyTest.class);

    private AbstractFilterStrategy getFilterStrategy() {
        return new PassportNumberFilterStrategy();
    }

    @Test
    public void evaluateCondition1() throws IOException {

        final AbstractFilterStrategy strategy = getFilterStrategy();

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "986001231", "classification == \"US\"", 1.0, "US");

        Assert.assertTrue(conditionSatisfied);

    }

}
