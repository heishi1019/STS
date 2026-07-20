package com.heishi.bioliterature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.heishi.bioliterature.dto.SearchFacetResponse;
import com.heishi.bioliterature.dto.SearchPaperResponse;

import java.util.List;

public interface SearchService {

    IPage<SearchPaperResponse> searchPapers(
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
            Boolean hasPdf);

    SearchFacetResponse getFacets();
}
