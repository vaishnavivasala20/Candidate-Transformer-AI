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
public class Project {

    private String projectName;

    @Builder.Default
    private List<String> technologiesUsed = new ArrayList<>();

    private String description;
}
