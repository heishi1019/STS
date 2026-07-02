package com.heishi.bioliterature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("paper_keyword")
public class PaperKeyword {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long keywordId;
    @TableField("is_major_topic")
    private Boolean majorTopic;
    private LocalDateTime createdAt;
}