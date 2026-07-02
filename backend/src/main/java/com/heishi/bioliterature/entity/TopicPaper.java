package com.heishi.bioliterature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("topic_paper")
public class TopicPaper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long topicId;
    private Long paperId;
    private String addedBy;
    private BigDecimal relevanceScore;
    private LocalDateTime createdAt;
}