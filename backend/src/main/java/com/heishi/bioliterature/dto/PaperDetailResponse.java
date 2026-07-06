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
public class PaperDetailResponse {

    private Long id;
    private String title;
    private String abstractText;
    private String journal;
    private Integer publishYear;
    private String doi;
    private String pmid;
    private List<AuthorSummary> authors;
    private List<KeywordSummary> keywords;
    private List<TagSummary> tags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSummary {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordSummary {
        private Long id;
        private String keyword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagSummary {
        private Long id;
        private String tagName;
        private String color;
    }
}
