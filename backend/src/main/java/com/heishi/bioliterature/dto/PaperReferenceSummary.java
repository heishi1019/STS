package com.heishi.bioliterature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperReferenceSummary {
    private Long id;
    private Long citedPaperId;
    private String citedPmid;
    private String citedDoi;
    private String citationText;
    private String citedTitle;
}
