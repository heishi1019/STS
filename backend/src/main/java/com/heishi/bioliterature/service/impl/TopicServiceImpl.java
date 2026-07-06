package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heishi.bioliterature.entity.Topic;
import com.heishi.bioliterature.mapper.TopicMapper;
import com.heishi.bioliterature.service.TopicService;
import org.springframework.stereotype.Service;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
}