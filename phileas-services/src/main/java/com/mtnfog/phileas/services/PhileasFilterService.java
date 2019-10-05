package com.mtnfog.phileas.services;

import com.mtnfog.phileas.ai.PyTorchFilter;
import com.mtnfog.phileas.metrics.PhileasMetricsService;
import com.mtnfog.phileas.model.enums.FilterType;
import com.mtnfog.phileas.model.exceptions.InvalidFilterProfile;
import com.mtnfog.phileas.model.filter.Filter;
import com.mtnfog.phileas.model.filter.dynamic.DynamicFilter;
import com.mtnfog.phileas.model.filter.rules.RulesFilter;
import com.mtnfog.phileas.model.filter.rules.dictionary.LuceneDictionaryFilter;
import com.mtnfog.phileas.model.objects.Span;
import com.mtnfog.phileas.model.profile.FilterProfile;
import com.mtnfog.phileas.model.profile.filters.Identifier;
import com.mtnfog.phileas.model.responses.FilterResponse;
import com.mtnfog.phileas.model.services.*;
import com.mtnfog.phileas.services.anonymization.*;
import com.mtnfog.phileas.services.filters.custom.PhoneNumberRulesFilter;
import com.mtnfog.phileas.services.filters.regex.*;
import com.mtnfog.phileas.store.MongoDBStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class PhileasFilterService implements FilterService, Serializable {

	private static final long serialVersionUID = 6998388861197152049L;

	private static final Logger LOGGER = LogManager.getLogger(PhileasFilterService.class);

    private MetricsService metricsService;
    private Map<String, DescriptiveStatistics> stats;
    private Store store;
    private List<PostFilter> postFilters;
    private Map<String, FilterProfile> filterProfiles;
    private Map<String, List<Filter>> filters;

    public PhileasFilterService(Properties applicationProperties, List<FilterProfileService> filterProfileServices, AnonymizationCacheService anonymizationCacheService) throws IOException {

        LOGGER.info("Initializing Phileas engine.");

        this.stats = new HashMap<>();
        this.postFilters = new LinkedList<>();
        this.filterProfiles = new HashMap<>();
        this.filters = new HashMap<>();

        // Configure metrics.
        this.metricsService = new PhileasMetricsService(applicationProperties);

        // Configure store.
        final boolean storeEnabled = StringUtils.equalsIgnoreCase(applicationProperties.getProperty("store.enabled", "false"), "true");
        if(storeEnabled) {
            final String storeUri = applicationProperties.getProperty("store.mongodb.uri", "mongodb://localhost:27017/philter");
            this.store = new MongoDBStore(storeUri);
        }

        // Path to the indexes directory.
        final String indexDirectory = applicationProperties.getProperty("indexes.directory", System.getProperty("user.dir") + "/indexes/");

        // Endpoint of the philter-ner API.
        final String philterNerEndpoint = applicationProperties.getProperty("ner.endpoint", "http://localhost:18080/");

        // Load all of the filter profiles into memory from each filter profile service.
        for(FilterProfileService filterProfileService : filterProfileServices) {
            filterProfiles.putAll(filterProfileService.getAll());
        }

        for(FilterProfile filterProfile : filterProfiles.values()) {

            final List<Filter> enabledFilters = new LinkedList<>();

            // Rules filters.

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.AGE)) {
                enabledFilters.add(new AgeFilter(filterProfile.getIdentifiers().getAge().getAgeFilterStrategies(), new AgeAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.CREDIT_CARD)) {
                enabledFilters.add(new CreditCardFilter(filterProfile.getIdentifiers().getCreditCard().getCreditCardFilterStrategies(), new CreditCardAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.DATE)) {
                enabledFilters.add(new DateFilter(filterProfile.getIdentifiers().getDate().getDateFilterStrategies(), new DateAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.EMAIL_ADDRESS)) {
                enabledFilters.add(new EmailAddressFilter(filterProfile.getIdentifiers().getEmailAddress().getEmailAddressFilterStrategies(), new EmailAddressAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.IP_ADDRESS)) {
                enabledFilters.add(new IpAddressFilter(filterProfile.getIdentifiers().getIpAddress().getIpAddressFilterStrategies(), new IpAddressAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.PHONE_NUMBER_EXTENSION)) {
                enabledFilters.add(new PhoneNumberExtensionFilter(filterProfile.getIdentifiers().getPhoneNumberExtension().getPhoneNumberExtensionFilterStrategies(), new AlphanumericAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.PHONE_NUMBER)) {
                enabledFilters.add(new PhoneNumberRulesFilter(filterProfile.getIdentifiers().getPhoneNumber().getPhoneNumberFilterStrategies(), new AlphanumericAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.SSN)) {
                enabledFilters.add(new SsnFilter(filterProfile.getIdentifiers().getSsn().getSsnFilterStrategies(), new AlphanumericAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.STATE_ABBREVIATION)) {
                enabledFilters.add(new StateAbbreviationFilter(filterProfile.getIdentifiers().getStateAbbreviation().getStateAbbreviationsFilterStrategies(), new StateAbbreviationAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.URL)) {
                enabledFilters.add(new UrlFilter(filterProfile.getIdentifiers().getUrl().getUrlFilterStrategies(), new UrlAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.VIN)) {
                enabledFilters.add(new VinFilter(filterProfile.getIdentifiers().getVin().getVinFilterStrategies(), new VinAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.ZIP_CODE)) {
                enabledFilters.add(new ZipCodeFilter(filterProfile.getIdentifiers().getZipCode().getZipCodeFilterStrategies(), new ZipCodeAnonymizationService(anonymizationCacheService)));
            }

            // Lucene dictionary filters.

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.LOCATION_CITY)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.LOCATION_CITY, filterProfile.getIdentifiers().getCity().getCityFilterStrategies(), indexDirectory + "cities", LuceneDictionaryFilter.CITIES_DISTANCES, new CityAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.LOCATION_COUNTY)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.LOCATION_COUNTY, filterProfile.getIdentifiers().getCounty().getCountyFilterStrategies(), indexDirectory + "states", LuceneDictionaryFilter.COUNTIES_DISTANCES, new CountyAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.LOCATION_STATE)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.LOCATION_STATE, filterProfile.getIdentifiers().getState().getStateFilterStrategies(), indexDirectory + "states", LuceneDictionaryFilter.STATES_DISTANCES, new StateAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.HOSPITAL)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.HOSPITAL, filterProfile.getIdentifiers().getHospital().getHospitalFilterStrategies(), indexDirectory + "hospitals", LuceneDictionaryFilter.HOSPITALS_DISTANCES, new HospitalAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.HOSPITAL_ABBREVIATION)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.HOSPITAL_ABBREVIATION, filterProfile.getIdentifiers().getHospitalAbbreviation().getHospitalAbbreviationFilterStrategies(), indexDirectory + "hospital-abbreviations", LuceneDictionaryFilter.HOSPITAL_ABBREVIATIONS_DISTANCES, new HospitalAbbreviationAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.FIRST_NAME)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.FIRST_NAME, filterProfile.getIdentifiers().getFirstName().getFirstNameFilterStrategies(), indexDirectory + "names", LuceneDictionaryFilter.FIRST_NAME_DISTANCES, new PersonsAnonymizationService(anonymizationCacheService)));
            }

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.SURNAME)) {
                enabledFilters.add(new LuceneDictionaryFilter(FilterType.SURNAME, filterProfile.getIdentifiers().getSurname().getSurnameFilterStrategies(), indexDirectory + "surnames", LuceneDictionaryFilter.SURNAME_DISTANCES, new SurnameAnonymizationService(anonymizationCacheService)));
            }

            // Enable ID filter last since it is a pretty generic pattern that might also match SSN, et. al.

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.IDENTIFIER)) {

                final List<Identifier> identifiers = filterProfile.getIdentifiers().getIdentifiers();
                for(Identifier identifier : identifiers) {
                    enabledFilters.add(new IdentifierFilter(identifier.getName(), identifier.getPattern(), identifier.getIdentifierFilterStrategies(), new AlphanumericAnonymizationService(anonymizationCacheService)));
                }

            }

            // PyTorch filters.

            if(filterProfile.getIdentifiers().isFilterEnabled(FilterType.NER_ENTITY)) {
                enabledFilters.add(new PyTorchFilter(philterNerEndpoint, FilterType.NER_ENTITY, filterProfile.getIdentifiers().getNer().getNerStrategies(), "PER", stats, metricsService, new PersonsAnonymizationService(anonymizationCacheService)));
                enabledFilters.add(new PyTorchFilter(philterNerEndpoint, FilterType.NER_ENTITY, filterProfile.getIdentifiers().getNer().getNerStrategies(), "LOC", stats, metricsService, new LocationsAnonymizationService(anonymizationCacheService)));
            }

            filters.put(filterProfile.getName(), enabledFilters);

        }

        // Configure post filters.
        // PHL-1: Allow for multi-word tokens.
        /*final boolean posTagPostFilterEnabled = StringUtils.equalsIgnoreCase(applicationProperties.getProperty("post.filter.pos.enabled", "true"), "true");
        if(posTagPostFilterEnabled) {
            final InputStream is = PhileasFilterService.class.getClassLoader().getResourceAsStream("en-pos-perceptron.bin");
            postFilters.add(new PartOfSpeechFalsePositiveFilter(is));
        }*/

    }

    @Override
    public List<Span> replacements(String documentId) {

        return store.getByDocumentId(documentId);

    }

    @Override
    public FilterResponse filter(String filterProfileName, String context, String input) throws InvalidFilterProfile, IOException {

        if(!filterProfiles.containsKey(filterProfileName)) {
            throw new InvalidFilterProfile("The filter profile does not exist.");
        }

        // Get the enabled filters for this filter profile.
        final List<Filter> enabledFilters = filters.get(filterProfileName);

        // Get the filter profile.
        final FilterProfile filterProfile = filterProfiles.get(filterProfileName);

        // The list that will contain the spans containing PHI/PII.
        List<Span> spans = new LinkedList<>();

        // Generate a random document ID.
        // TODO: Move the ID generator so it is not specific to Mongo.
        final String documentId = MongoDBStore.generateId();

        // Execute the rules filters.
        for(Filter f : enabledFilters) {

            spans.addAll(f.filter(filterProfile, context, documentId, input));

        }

        // Drop overlapping spans.
        spans = Span.dropOverlappingSpans(spans);

        // Sort the spans based on the confidence.
        spans.sort(Comparator.comparing(Span::getConfidence));

        // Perform post-filtering for false positives.
        for(PostFilter postFilter : postFilters) {
            spans = postFilter.filter(input, spans);
        }

        // The spans that will be persisted. Has to be a deep copy because the shift
        // below will change the indexes.
        final List<Span> appliedSpans = spans.stream().map(d -> d.copy()).collect(toList());

        // Used to manipulate the text.
        final StringBuffer buffer = new StringBuffer(input);

        // Go character by character through the input.
        for(int i = 0; i < input.length(); i++) {

            // Is index i the start of a span?
            final Span span = Span.doesIndexStartSpan(i, spans);

            if(span != null) {
                
                // Get the replacement. This might be the token itself or an anonymized version.
                final String replacement = span.getReplacement();

                final int spanLength = span.getCharacterEnd() - span.getCharacterStart();
                final int replacementLength = replacement.length();

                if(spanLength != replacementLength) {

                    // We need to adjust the characterStart and characterEnd for the remaining spans.
                    // A negative value means shift left.
                    // A positive value means shift right.
                    final int shift = (spanLength - replacementLength) * -1;

                    // Shift the remaining spans by the shift value.
                    spans = Span.shiftSpans(shift, span, spans);

                }

                // We can now do the replacement.
                buffer.replace(span.getCharacterStart(), span.getCharacterEnd(), replacement);

                // Jump ahead outside of this span.
                i = span.getCharacterEnd();

            }

        }

        metricsService.incrementProcessed();

        // Store the applied spans in the database.
        if(store != null) {
            store.insert(appliedSpans);
        }

        return new FilterResponse(buffer.toString(), context, documentId);

    }

}
