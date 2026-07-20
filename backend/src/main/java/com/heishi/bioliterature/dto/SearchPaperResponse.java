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
public class SearchPaperResponse {
    private Long id;
    private String title;
    private String titleZh;
    private String abstractText;
    private String abstractZh;
    private String journal;
    private Integer publishYear;
    private String doi;
    private String pmid;
    private String pdfUrl;
    private String fullTextUrl;
    private List<BiomedicalEntitySummary> entities;
    private List<PaperDetailResponse.TagSummary> tags;
    private List<TopicSummary> topics;
}
