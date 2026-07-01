package com.eightfold.candidate_transformer_v2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterInsights {

    private List<String> strengths;

    private List<String> missingSkills;

    private String recommendedRole;

    private String recommendation;

    private int profileCompleteness;

    private double matchScore;

}