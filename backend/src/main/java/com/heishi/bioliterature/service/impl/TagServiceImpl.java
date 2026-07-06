package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heishi.bioliterature.entity.Tag;
import com.heishi.bioliterature.mapper.TagMapper;
import com.heishi.bioliterature.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}