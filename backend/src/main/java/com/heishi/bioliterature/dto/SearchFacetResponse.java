package com.heishi.bioliterature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFacetResponse {
    private List<Option> diseases;
    private List<Option> genes;
    private List<Option> drugs;
    private List<Option> symptoms;
    private List<Option> keywords;
    private List<Option> tags;
    private List<Option> topics;
    private List<String> journals;
    private List<Integer> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private Long id;
        private String name;
    }
}
