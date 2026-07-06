package com.heishi.bioliterature.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heishi.bioliterature.dto.PaperDetailResponse;
import com.heishi.bioliterature.entity.Paper;

public interface PaperService extends IService<Paper> {

    PaperDetailResponse getDetailById(Long id);
}
