package com.eightfold.candidate_transformer_v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provenance {

    private String field;

    private String source;

    private String method;

    private Double confidence;
}