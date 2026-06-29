package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heishi.bioliterature.entity.Paper;
import com.heishi.bioliterature.mapper.PaperMapper;
import com.heishi.bioliterature.service.PaperService;
import org.springframework.stereotype.Service;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {
}