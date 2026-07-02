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
@TableName("author")
public class Author {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fullName;
    private String lastName;
    private String foreName;
    private String initials;
    private String collectiveName;
    private String normalizedName;
    private String orcid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}