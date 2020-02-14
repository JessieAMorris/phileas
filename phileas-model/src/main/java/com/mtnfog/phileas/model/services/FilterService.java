package com.mtnfog.phileas.model.services;

import com.mtnfog.phileas.model.objects.Span;
import com.mtnfog.phileas.model.responses.FilterResponse;

import java.io.IOException;
import java.util.List;

public interface FilterService {

    FilterResponse filter(String filterProfileName, String context, String input) throws Exception;

    List<Span> replacements(String documentId) throws IOException;

    void reloadFilterProfiles() throws IOException;

}
