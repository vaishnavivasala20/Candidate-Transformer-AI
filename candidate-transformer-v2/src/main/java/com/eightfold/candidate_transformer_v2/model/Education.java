package com.eightfold.candidate_transformer_v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Education {

    private String institution;

    private String degree;

    private String field;

    private Integer endYear;
}