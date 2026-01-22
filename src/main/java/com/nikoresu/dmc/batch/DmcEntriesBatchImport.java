package com.nikoresu.dmc.batch;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.nikoresu.dmc.data.DmcEntryRepository;
import com.nikoresu.dmc.domain.DmcEntry;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class DmcEntriesBatchImport {

    @Inject
    DmcEntryRepository repository;

    @ConfigProperty(name = "couchbase.dmc.csvDataFile", defaultValue = "")
    Optional<String> csvFileName;

    void onStart(@Observes StartupEvent event) {
        log.info("Starting DMC data loading");

        try {
            if (csvFileName.isEmpty()) {
                log.info("No DMC data file given. Skipping import...");
                return;
            }

            loadDmcEntriesFromCSV();
            log.info("DMC data loaded to Couchbase successfully");
        } catch (Exception ex) {
            log.error("Failed to load DMC data into Couchbase", ex);
        }
    }

    private void loadDmcEntriesFromCSV() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(csvFileName.get())) {

            if (inputStream == null) {
                log.warn("CSV file not found in resources, skipping data load");
                return;
            }

            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();

            MappingIterator<Map<String, String>> iterator = csvMapper
                    .readerFor(Map.class)
                    .with(schema)
                    .readValues(inputStream);

            int count = 0, updated = 0, created = 0;

            while (iterator.hasNext()) {
                Map<String, String> row = iterator.next();

                try {
                    DmcEntry entry = parseRowToEntry(row);

                    // Check if entry already exists
                    boolean exists = repository.exists(entry.getNumber());

                    // Save or update
                    repository.saveWithTimestamps(entry.getNumber(), entry);

                    if (exists) {
                        updated++;
                    } else {
                        created++;
                    }

                    count++;

                } catch (Exception e) {
                    log.error("Error processing CSV row: {}", row, e);
                }
            }

            log.info("Processed {} DMC entries: {} created, {} updated", count, created, updated);
        }
    }

    private DmcEntry parseRowToEntry(Map<String, String> row) {
        String number = row.get("number");
        String name = row.get("name");
        String redStr = row.get("red");
        String greenStr = row.get("green");
        String blueStr = row.get("blue");
        String hex = row.get("hex");
        String similarStr = row.get("similar");

        // Parse name into baseName and variation
        String baseName;
        String variation = "";

        if (name != null && name.contains(" - ")) {
            String[] parts = name.split(" - ", 2);
            baseName = parts[0].trim();
            variation = parts[1].trim();
        } else {
            baseName = name != null ? name.trim() : "";
        }

        // Parse RGB values
        int red = parseColorValue(redStr);
        int green = parseColorValue(greenStr);
        int blue = parseColorValue(blueStr);

        // Normalize hex code
        String normalizedHex = normalizeHex(hex);

        // Parse similar codes
        List<String> similars = parseSimilarCodes(similarStr);

        return DmcEntry.builder()
                .number(number)
                .baseName(baseName)
                .variation(variation)
                .red(red)
                .green(green)
                .blue(blue)
                .hex(normalizedHex)
                .similars(similars)
                .build();
    }

    private int parseColorValue(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        try {
            // Handle decimal values like "240.0"
            double doubleValue = Double.parseDouble(value.trim());
            return (int) Math.round(doubleValue);
        } catch (NumberFormatException e) {
            log.warn("Invalid color value: {}, defaulting to 0", value);
            return 0;
        }
    }

    private String normalizeHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return "#000000";
        }

        // Remove any existing # symbol and whitespace
        String cleaned = hex.trim().replace("#", "");

        // Ensure it's uppercase
        String normalized = cleaned.toUpperCase();

        // Pad with zeros if needed (should be 6 characters)
        if (normalized.length() < 6) {
            normalized = String.format("%6s", normalized).replace(' ', '0');
        }

        // Add # prefix
        return "#" + normalized;
    }

    private List<String> parseSimilarCodes(String similarStr) {
        List<String> similars = new ArrayList<>();

        if (similarStr == null || similarStr.isEmpty()) {
            return similars;
        }

        // Remove brackets and quotes: "['3774', '772']" -> 3774, 772
        String cleaned = similarStr
                .replace("[", "")
                .replace("]", "")
                .replace("'", "")
                .replace("\"", "")
                .trim();

        // Split by comma and trim each value
        String[] codes = cleaned.split(",");
        for (String code : codes) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) {
                similars.add(trimmed);
            }
        }

        return similars;
    }

}
