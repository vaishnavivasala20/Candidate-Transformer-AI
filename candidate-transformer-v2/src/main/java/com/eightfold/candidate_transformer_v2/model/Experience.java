package com.eightfold.candidate_transformer_v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experience {

    private String company;

    private String title;

    private String startDate;

    private String endDate;

    private String summary;
}