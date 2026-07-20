package com.heishi.bioliterature.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heishi.bioliterature.dto.SearchFacetResponse;
import com.heishi.bioliterature.dto.SearchPaperResponse;
import com.heishi.bioliterature.mapper.SearchMapper;
import com.heishi.bioliterature.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchMapper searchMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<SearchPaperResponse> searchPapers(
            long page,
            long size,
            String q,
            List<Long> diseaseIds,
            List<Long> geneIds,
            List<Long> drugIds,
            List<Long> symptomIds,
            List<Long> keywordIds,
            Long tagId,
            Long topicId,
            String journal,
            Integer yearFrom,
            Integer yearTo,
            Boolean hasPdf) {
        IPage<SearchPaperResponse> result = searchMapper.searchPapers(
                Page.of(page, size),
                trim(q),
                emptyToNull(diseaseIds),
                emptyToNull(geneIds),
                emptyToNull(drugIds),
                emptyToNull(symptomIds),
                emptyToNull(keywordIds),
                tagId,
                topicId,
                trim(journal),
                yearFrom,
                yearTo,
                hasPdf);

        result.getRecords().forEach(paper -> {
            Long paperId = paper.getId();
            paper.setEntities(searchMapper.selectEntitiesByPaperId(paperId));
            paper.setTags(searchMapper.selectTagsByPaperId(paperId));
            paper.setTopics(searchMapper.selectTopicsByPaperId(paperId));
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchFacetResponse getFacets() {
        return SearchFacetResponse.builder()
                .diseases(searchMapper.selectEntityOptions("DISEASE"))
                .genes(searchMapper.selectEntityOptions("GENE"))
                .drugs(searchMapper.selectEntityOptions("DRUG"))
                .symptoms(searchMapper.selectEntityOptions("SYMPTOM"))
                .keywords(searchMapper.selectKeywordOptions())
                .tags(searchMapper.selectTagOptions())
                .topics(searchMapper.selectTopicOptions())
                .journals(searchMapper.selectJournals())
                .years(searchMapper.selectYears())
                .build();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private List<Long> emptyToNull(List<Long> ids) {
        return ids == null || ids.isEmpty() ? null : ids;
    }
}
