package com.mtnfog.phileas.services.anonymization;

import com.mtnfog.phileas.model.services.AnonymizationCacheService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Pattern;

public class DriversLicenseAnonymizationService extends AbstractAnonymizationService {

    private static final Logger LOGGER = LogManager.getLogger(DriversLicenseAnonymizationService.class);

    // TODO: Don't duplicate these from DateFilter.
    private static final Pattern DATE_YYYYMMDD_REGEX = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}\\b");
    private static final Pattern DATE_MMDDYYYY_REGEX = Pattern.compile("\\b\\d{2}-\\d{2}-\\d{4}\\b");
    private static final Pattern DATE_MDYYYY_REGEX = Pattern.compile("\\b\\d{1,2}-\\d{1,2}-\\d{2,4}\\b");
    private static final Pattern DATE_MONTH_REGEX = Pattern.compile("(?i)(\\b\\d{1,2}\\D{0,3})?\\b(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|(Nov|Dec)(?:ember)?)\\D?(\\d{1,2}(\\D?(st|nd|rd|th))?\\D?)(\\D?((19[7-9]\\d|20\\d{2})|\\d{2}))?\\b", Pattern.CASE_INSENSITIVE);

    private Random random;

    public DriversLicenseAnonymizationService(AnonymizationCacheService anonymizationCacheService) {
        super(anonymizationCacheService);
        this.random = new Random();
    }

    @Override
    public String anonymize(String token) {

        LocalDate localDate = getRandomDate();

        // Generate a date in the same format as the token.

        if(token.matches(DATE_YYYYMMDD_REGEX.pattern())) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return localDate.format(formatter);

        } else if(token.matches(DATE_MMDDYYYY_REGEX.pattern())) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
            return localDate.format(formatter);

        } else if(token.matches(DATE_MDYYYY_REGEX.pattern())) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M-d-yyyy");
            return localDate.format(formatter);

        } else if(token.matches(DATE_MONTH_REGEX.pattern())) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            return localDate.format(formatter);

        } else {

            LOGGER.warn("Date {} matched no pattern.", token);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return localDate.format(formatter);

        }

    }

    private LocalDate getRandomDate() {

        int minDay = (int) LocalDate.of(1900, 1, 1).toEpochDay();
        int maxDay = (int) LocalDate.of(Calendar.getInstance().get(Calendar.YEAR), 1, 1).toEpochDay();
        int randomDay = minDay + random.nextInt(maxDay - minDay);

        return LocalDate.ofEpochDay(randomDay);

    }

}
