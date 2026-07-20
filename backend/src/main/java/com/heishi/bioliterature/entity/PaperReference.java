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
@TableName("paper_reference")
public class PaperReference {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long citedPaperId;
    private String citedPmid;
    private String citedDoi;
    private String citationText;
    private LocalDateTime createdAt;
}
