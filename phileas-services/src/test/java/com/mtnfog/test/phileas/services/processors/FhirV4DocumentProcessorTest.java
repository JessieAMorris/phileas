package com.mtnfog.test.phileas.services.processors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mtnfog.phileas.model.profile.Crypto;
import com.mtnfog.phileas.model.profile.FilterProfile;
import com.mtnfog.phileas.model.profile.Structured;
import com.mtnfog.phileas.model.profile.fhir4.FhirItem;
import com.mtnfog.phileas.model.profile.fhir4.FhirR4;
import com.mtnfog.phileas.model.responses.FilterResponse;
import com.mtnfog.phileas.model.services.MetricsService;
import com.mtnfog.phileas.services.processors.FhirV4DocumentProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

public class FhirV4DocumentProcessorTest {

    @Test
    public void test1() throws Exception {

        final MetricsService metricsService = Mockito.mock(MetricsService.class);

        final FhirV4DocumentProcessor documentProcessor = new FhirV4DocumentProcessor(metricsService);

        // FilterProfile filterProfile, String context, String documentId, String json
        final FilterProfile filterProfile = new FilterProfile();
        filterProfile.setCrypto(new Crypto("key", "iv"));

        final Structured structured = new Structured();

        final FhirR4 fhirR4 = new FhirR4();
        fhirR4.setFhirItems(Arrays.asList(new FhirItem("patient.name.family", "DELETE"), new FhirItem("patient.name.given", "DELETE")));
        //fhirR4.setFhirItems(Arrays.asList(new FhirItem("patient.address.city", "DELETE")));
        structured.setFhirR4(fhirR4);

        filterProfile.setStructured(structured);

        prettyPrintJson(filterProfile);

        final String json = IOUtils.toString(this.getClass().getResourceAsStream("/fhir4/bundle-example.json"), Charset.defaultCharset());

        final FilterResponse filterResponse = documentProcessor.process(filterProfile, "context", "documentId", json);

        prettyPrintJson(filterResponse.getFilteredText());

        // Parse the returned FHIR json.
        final FhirContext ctx = FhirContext.forR4();
        final IParser parser = ctx.newJsonParser();
        final Bundle bundle = parser.parseResource(Bundle.class, filterResponse.getFilteredText());

        for(final Bundle.BundleEntryComponent bundleEntryComponent : bundle.getEntry()) {

            if(bundleEntryComponent.getResource() instanceof Patient) {

                final Patient patient = (Patient) bundleEntryComponent.getResource();
                for(HumanName humanName : patient.getName()) {

                    System.out.println("Manipulated human name:");
                    prettyPrintJson(humanName);

                    System.out.println("Filtered human family name: " + humanName.getFamily());
                    System.out.println("Filtered human given name: " + humanName.getGiven().size());

                    Assert.assertEquals("", humanName.getFamily());
                    Assert.assertEquals(true, humanName.getGiven().isEmpty());

                }

            }

        }

    }

    private void prettyPrintJson(Object object) {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(object));
        
    }

    private void prettyPrintJson(String uglyJSONString) {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JsonParser jp = new JsonParser();
        final JsonElement je = jp.parse(uglyJSONString);
        System.out.println(gson.toJson(je));

    }

}
