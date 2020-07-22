package com.mtnfog.test.phileas.services.filters;

import com.mtnfog.phileas.model.enums.FilterType;
import com.mtnfog.phileas.model.enums.SensitivityLevel;
import com.mtnfog.phileas.model.filter.rules.dictionary.LuceneDictionaryFilter;
import com.mtnfog.phileas.model.objects.FilterResult;
import com.mtnfog.phileas.model.objects.Span;
import com.mtnfog.phileas.model.profile.Crypto;
import com.mtnfog.phileas.model.profile.filters.strategies.dynamic.CountyFilterStrategy;
import com.mtnfog.phileas.model.services.AlertService;
import com.mtnfog.phileas.model.services.AnonymizationService;
import com.mtnfog.phileas.services.anonymization.CountyAnonymizationService;
import com.mtnfog.phileas.services.anonymization.cache.LocalAnonymizationCacheService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CountyFilterTest extends AbstractFilterTest {

    private static final Logger LOGGER = LogManager.getLogger(CountyFilterTest.class);

    private String INDEX_DIRECTORY = getIndexDirectory("counties");

    private AlertService alertService = Mockito.mock(AlertService.class);

    @BeforeEach
    public void before() {
        INDEX_DIRECTORY = System.getProperty( "os.name" ).contains( "indow" ) ? INDEX_DIRECTORY.substring(1) : INDEX_DIRECTORY;
        LOGGER.info("Using index directory {}", INDEX_DIRECTORY);
    }

    @Test
    public void filterCountiesLow() throws Exception {

        AnonymizationService anonymizationService = new CountyAnonymizationService(new LocalAnonymizationCacheService());

        final List<CountyFilterStrategy> strategies = Arrays.asList(new CountyFilterStrategy());
        final LuceneDictionaryFilter filter = new LuceneDictionaryFilter(FilterType.LOCATION_COUNTY, strategies, INDEX_DIRECTORY, SensitivityLevel.LOW, anonymizationService, alertService, Collections.emptySet(), Collections.emptyList(), new Crypto(), windowSize);

        FilterResult filterResult = filter.filter(getFilterProfile(SensitivityLevel.LOW), "context", "documentid", 0,"Lived in Fyette");

        showSpans(filterResult.getSpans());

        Assertions.assertEquals(0, filterResult.getSpans().size());

    }

    @Test
    public void filterCountiesMedium() throws Exception {

        AnonymizationService anonymizationService = new CountyAnonymizationService(new LocalAnonymizationCacheService());

        final List<CountyFilterStrategy> strategies = Arrays.asList(new CountyFilterStrategy());
        final LuceneDictionaryFilter filter = new LuceneDictionaryFilter(FilterType.LOCATION_COUNTY, strategies, INDEX_DIRECTORY, SensitivityLevel.MEDIUM, anonymizationService, alertService, Collections.emptySet(), Collections.emptyList(), new Crypto(), windowSize);

        FilterResult filterResult = filter.filter(getFilterProfile(SensitivityLevel.MEDIUM), "context", "documentid", 0,"Lived in Fyette");

        showSpans(filterResult.getSpans());

        Assertions.assertEquals(1, filterResult.getSpans().size());
        Assertions.assertEquals("fyette", filterResult.getSpans().get(0).getText());

    }

    @Test
    public void filterCountiesHigh() throws Exception {

        AnonymizationService anonymizationService = new CountyAnonymizationService(new LocalAnonymizationCacheService());

        final List<CountyFilterStrategy> strategies = Arrays.asList(new CountyFilterStrategy());
        final LuceneDictionaryFilter filter = new LuceneDictionaryFilter(FilterType.LOCATION_COUNTY, strategies, INDEX_DIRECTORY, SensitivityLevel.HIGH, anonymizationService, alertService, Collections.emptySet(), Collections.emptyList(), new Crypto(), windowSize);

        FilterResult filterResult = filter.filter(getFilterProfile(SensitivityLevel.HIGH), "context", "documentid", 0,"Lived in Fyette");

        showSpans(filterResult.getSpans());

        Assertions.assertEquals(3, filterResult.getSpans().size());

    }

}
