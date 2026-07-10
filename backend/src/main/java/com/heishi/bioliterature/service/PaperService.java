package com.heishi.bioliterature.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.heishi.bioliterature.dto.PaperDetailResponse;
import com.heishi.bioliterature.entity.Paper;

public interface PaperService extends IService<Paper> {

    IPage<Paper> searchPage(
            long page,
            long size,
            String title,
            String author,
            String keyword,
            String journal,
            String doi,
            String pmid,
            Integer year,
            String dataSource,
            Long tagId,
            Long topicId);

    PaperDetailResponse getDetailById(Long id);
}
