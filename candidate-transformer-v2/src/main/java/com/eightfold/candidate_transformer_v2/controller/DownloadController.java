package com.eightfold.candidate_transformer_v2.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto;
import com.eightfold.candidate_transformer_v2.dto.RecruiterJsonBuilder;
import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.servlet.http.HttpSession;

/**
 * Serves the recruiter-friendly candidate JSON report.
 * The exported JSON uses a clean, human-readable structure — not the raw
 * internal Java object — so recruiters can open it in any text editor
 * and immediately understand the candidate profile.
 */
@RestController
public class DownloadController {

    private static final RecruiterJsonBuilder JSON_BUILDER = new RecruiterJsonBuilder();

    private static final ObjectMapper MAPPER = buildMapper();

    @GetMapping("/download/json")
    public ResponseEntity<byte[]> downloadJson(HttpSession session) throws Exception {

        Candidate candidate = (Candidate) session.getAttribute("candidate");

        if (candidate == null) {
            return ResponseEntity.badRequest()
                    .body("No candidate profile available. Please upload a resume first.".getBytes());
        }

        // Build recruiter-friendly report DTO
        CandidateReportDto report = JSON_BUILDER.build(candidate);

        byte[] json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(report);

        String filename = buildFilename(candidate.getFullName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    // -----------------------------------------------------------------------

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /** Builds a recruiter-friendly filename: "John_Doe_Candidate_Profile.json". */
    private String buildFilename(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Candidate_Profile.json";
        String safe = fullName.trim()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_");
        return safe + "_Candidate_Profile.json";
    }
}
