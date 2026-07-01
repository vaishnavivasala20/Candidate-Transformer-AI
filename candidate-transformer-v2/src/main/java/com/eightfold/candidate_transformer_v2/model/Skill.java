package com.eightfold.candidate_transformer_v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    private String name;

    private Double confidence;

    private String source;
}