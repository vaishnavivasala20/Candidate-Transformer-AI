package com.eightfold.candidate_transformer_v2.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    private String candidateId;

    private String fullName;

    @Builder.Default
    private List<String> emails = new ArrayList<>();

    @Builder.Default
    private List<String> phones = new ArrayList<>();

    private String location;

    private String headline;

    private Integer yearsExperience;

    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @Builder.Default
    private List<Experience> experience = new ArrayList<>();

    @Builder.Default
    private List<Education> education = new ArrayList<>();

    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    @Builder.Default
    private List<Provenance> provenance = new ArrayList<>();

    private String summary;

    private Double overallConfidence;

    private RecruiterInsights recruiterInsights;
}