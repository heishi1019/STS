package com.heishi.bioliterature.dto;

import lombok.Data;

@Data
public class PubMedCollectRequest {
    private String query;
    private Integer retmax;
}
