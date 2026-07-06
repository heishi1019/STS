package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heishi.bioliterature.entity.PaperTag;
import com.heishi.bioliterature.mapper.PaperTagMapper;
import com.heishi.bioliterature.service.PaperTagService;
import org.springframework.stereotype.Service;

@Service
public class PaperTagServiceImpl extends ServiceImpl<PaperTagMapper, PaperTag> implements PaperTagService {
}