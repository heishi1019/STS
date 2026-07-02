package com.heishi.bioliterature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("crawl_task")
public class CrawlTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String dataSource;
    private String queryText;
    private String requestParams;
    private String taskStatus;
    private String cursorToken;
    private Long totalCount;
    private Long processedCount;
    private Long successCount;
    private Long failedCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}