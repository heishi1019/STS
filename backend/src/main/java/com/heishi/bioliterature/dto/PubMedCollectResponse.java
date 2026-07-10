package com.heishi.bioliterature.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PubMedCollectResponse {
    private Long taskId;
    private String query;
    private String status;
    private Long totalResults;
    private Integer requestedCount;
    private Integer fetchedCount;
    private Integer successWriteCount;
    private Integer insertedCount;
    private Integer updatedCount;
    private Integer skippedDuplicateCount;
    private Integer failedCount;
    private String message;
    private String errorMessage;
}
