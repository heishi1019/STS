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
@TableName("paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String abstractText;
    private String journal;
    private Integer publicationYear;
    private String doi;
    private String pmid;
    private String pmcid;
    private String dataSource;
    private String pdfUrl;
    private String fullTextUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}