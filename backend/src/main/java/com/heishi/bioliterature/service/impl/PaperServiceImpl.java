package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heishi.bioliterature.dto.PaperDetailResponse;
import com.heishi.bioliterature.entity.Paper;
import com.heishi.bioliterature.mapper.PaperMapper;
import com.heishi.bioliterature.service.PaperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Override
    @Transactional(readOnly = true)
    public IPage<Paper> searchPage(
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
            Long topicId) {
        return baseMapper.searchPapers(
                Page.of(page, size),
                trim(title),
                trim(author),
                trim(keyword),
                trim(journal),
                trim(doi),
                trim(pmid),
                year,
                trim(dataSource),
                tagId,
                topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaperDetailResponse getDetailById(Long id) {
        Paper paper = getById(id);
        if (paper == null) {
            return null;
        }

        return PaperDetailResponse.builder()
                .id(paper.getId())
                .title(paper.getTitle())
                .abstractText(paper.getAbstractText())
                .journal(paper.getJournal())
                .publishYear(paper.getPublicationYear())
                .doi(paper.getDoi())
                .pmid(paper.getPmid())
                .authors(baseMapper.selectAuthorsByPaperId(id))
                .keywords(baseMapper.selectKeywordsByPaperId(id))
                .tags(baseMapper.selectTagsByPaperId(id))
                .build();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
