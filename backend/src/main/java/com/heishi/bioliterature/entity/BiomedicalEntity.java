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
@TableName("biomedical_entity")
public class BiomedicalEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String entityName;
    private String entityType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
