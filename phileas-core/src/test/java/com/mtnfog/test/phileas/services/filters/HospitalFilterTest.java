package com.mtnfog.test.phileas.services.filters;

import com.mtnfog.phileas.model.enums.FilterType;
import com.mtnfog.phileas.model.enums.SensitivityLevel;
import com.mtnfog.phileas.model.filter.rules.dictionary.LuceneDictionaryFilter;
import com.mtnfog.phileas.model.objects.Span;
import com.mtnfog.phileas.model.profile.Crypto;
import com.mtnfog.phileas.model.profile.filters.strategies.dynamic.HospitalFilterStrategy;
import com.mtnfog.phileas.model.services.AlertService;
import com.mtnfog.phileas.model.services.AnonymizationService;
import com.mtnfog.phileas.services.anonymization.HospitalAnonymizationService;
import com.mtnfog.phileas.services.anonymization.cache.LocalAnonymizationCacheService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HospitalFilterTest extends AbstractFilterTest {

    private static final Logger LOGGER = LogManager.getLogger(HospitalFilterTest.class);

    private String INDEX_DIRECTORY = getIndexDirectory("hospitals");

    private AlertService alertService = Mockito.mock(AlertService.class);

    @Before
    public void before() {
        INDEX_DIRECTORY = System.getProperty( "os.name" ).contains( "indow" ) ? INDEX_DIRECTORY.substring(1) : INDEX_DIRECTORY;
        LOGGER.info("Using index directory {}", INDEX_DIRECTORY);
    }

    @Test
    public void filter1() throws Exception {

        AnonymizationService anonymizationService = new HospitalAnonymizationService(new LocalAnonymizationCacheService());

        final List<HospitalFilterStrategy> strategies = Arrays.asList(new HospitalFilterStrategy());
        final LuceneDictionaryFilter filter = new LuceneDictionaryFilter(FilterType.HOSPITAL, strategies, INDEX_DIRECTORY, SensitivityLevel.LOW, anonymizationService, alertService, Collections.emptySet(), new Crypto(), windowSize);

        List<Span> spans = filter.filter(getFilterProfile(SensitivityLevel.LOW), "context", "documentid","Went to Wyoming Medical Center");
        Assert.assertEquals(1, spans.size());
        Assert.assertEquals("wyoming medical center", spans.get(0).getText());

    }

}
