package com.mtnfog.test.phileas.model.profile.filters.strategies.ai;

import com.mtnfog.phileas.model.profile.filters.strategies.AbstractFilterStrategy;
import com.mtnfog.phileas.model.profile.filters.strategies.ai.NerFilterStrategy;
import com.mtnfog.phileas.model.profile.filters.strategies.custom.CustomDictionaryFilterStrategy;
import com.mtnfog.phileas.model.profile.filters.strategies.rules.SsnFilterStrategy;
import com.mtnfog.phileas.model.services.AnonymizationCacheService;
import com.mtnfog.phileas.model.services.AnonymizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class NerFilterStrategyTest {

    private static final Logger LOGGER = LogManager.getLogger(NerFilterStrategyTest.class);

    @Test
    public void replacement1() throws IOException {

        final AnonymizationService anonymizationService = Mockito.mock(AnonymizationService.class);

        final NerFilterStrategy strategy = new NerFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.STATIC_REPLACE);
        strategy.setStaticReplacement("static-value");

        final String replacement = strategy.getReplacement("name", "context", "docId", "token", anonymizationService);

        Assert.assertEquals("static-value", replacement);

    }

    @Test
    public void replacement2() throws IOException {

        final AnonymizationService anonymizationService = Mockito.mock(AnonymizationService.class);

        final NerFilterStrategy strategy = new NerFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.REDACT);
        strategy.setRedactionFormat("REDACTION-%t");

        final String replacement = strategy.getReplacement("name", "context", "docId", "token", anonymizationService);

        Assert.assertEquals("REDACTION-entity", replacement);

    }

    @Test
    public void replacement3() throws IOException {

        final AnonymizationService anonymizationService = Mockito.mock(AnonymizationService.class);
        final AnonymizationCacheService anonymizationCacheService = Mockito.mock(AnonymizationCacheService.class);

        when(anonymizationCacheService.get("context", "token")).thenReturn("random");
        when(anonymizationService.getAnonymizationCacheService()).thenReturn(anonymizationCacheService);

        final NerFilterStrategy strategy = new NerFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.RANDOM_REPLACE);

        final String replacement = strategy.getReplacement("name", "context", "docId", "token", anonymizationService);

        Assert.assertNotEquals("random", replacement);

    }

    @Test
    public void evaluateCondition1() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence > 0.25", attributes);

        Assert.assertTrue(conditionSatisfied);

    }

    @Test
    public void evaluateCondition2() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.TYPE, "PER");

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "type == PER", attributes);

        Assert.assertTrue(conditionSatisfied);

    }

    @Test
    public void evaluateCondition3() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.TYPE, "LOC");

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "type == PER", attributes);

        Assert.assertFalse(conditionSatisfied);

    }

    @Test
    public void evaluateCondition4() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence == 0.5", attributes);

        Assert.assertTrue(conditionSatisfied);

    }

    @Test
    public void evaluateCondition5() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.6);

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence != 0.5", attributes);

        Assert.assertTrue(conditionSatisfied);

    }

    @Test
    public void evaluateCondition6() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence != 0.5", attributes);

        Assert.assertFalse(conditionSatisfied);

    }

    @Test
    public void evaluateCondition7() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);
        attributes.put(NerFilterStrategy.TYPE, "LOC");

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence != 0.5 and type == PER", attributes);

        Assert.assertFalse(conditionSatisfied);

    }

    @Test
    public void evaluateCondition8() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);
        attributes.put(NerFilterStrategy.TYPE, "PER");

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence != 0.5 and type != LOC", attributes);

        Assert.assertFalse(conditionSatisfied);

    }

    @Test
    public void evaluateCondition9() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);
        attributes.put(NerFilterStrategy.TYPE, "PER");

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence > 0.4 and type == PER", attributes);

        Assert.assertTrue(conditionSatisfied);

    }

    @Test
    public void evaluateCondition10() {

        final NerFilterStrategy strategy = new NerFilterStrategy();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NerFilterStrategy.CONFIDENCE, 0.5);
        attributes.put(NerFilterStrategy.TYPE, "PER");

        final boolean conditionSatisfied = strategy.evaluateCondition("context", "documentId", "John Smith", "confidence < 0.4 and type == PER", attributes);

        Assert.assertFalse(conditionSatisfied);

    }

}
