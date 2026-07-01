package com.eightfold.candidate_transformer_v2.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import com.eightfold.candidate_transformer_v2.model.Candidate;

public class CsvParser {

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build();

    public List<Candidate> parseAll(MultipartFile csvFile) throws IOException {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));

             CSVParser csvParser = new CSVParser(reader, CSV_FORMAT)) {

            List<Candidate> candidates = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                candidates.add(mapRecord(record));
            }

            return candidates;
        }
    }

    public Candidate parseFirstCandidate(MultipartFile csvFile) throws IOException {

        List<Candidate> candidates = parseAll(csvFile);

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(0);
    }

    private Candidate mapRecord(CSVRecord record) {

        return Candidate.builder()
                .fullName(getValue(record, "fullName"))
                .emails(List.of(getValue(record, "email")))
                .phones(List.of(getValue(record, "phone")))
                .location(getValue(record, "location"))
                .headline(getValue(record, "headline"))
                .build();
    }

    private String getValue(CSVRecord record, String column) {

        try {
            return record.get(column);
        } catch (Exception e) {
            return "";
        }
    }
}