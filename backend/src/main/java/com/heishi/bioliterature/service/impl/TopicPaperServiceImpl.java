package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heishi.bioliterature.entity.TopicPaper;
import com.heishi.bioliterature.mapper.TopicPaperMapper;
import com.heishi.bioliterature.service.TopicPaperService;
import org.springframework.stereotype.Service;

@Service
public class TopicPaperServiceImpl extends ServiceImpl<TopicPaperMapper, TopicPaper> implements TopicPaperService {
}