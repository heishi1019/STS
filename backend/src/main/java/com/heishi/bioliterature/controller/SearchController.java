package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.dto.SearchFacetResponse;
import com.heishi.bioliterature.dto.SearchPaperResponse;
import com.heishi.bioliterature.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private static final long MAX_PAGE_SIZE = 100;

    private final SearchService searchService;

    @GetMapping("/papers")
    public Result<IPage<SearchPaperResponse>> searchPapers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Long> diseaseIds,
            @RequestParam(required = false) List<Long> geneIds,
            @RequestParam(required = false) List<Long> drugIds,
            @RequestParam(required = false) List<Long> symptomIds,
            @RequestParam(required = false) List<Long> keywordIds,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String journal,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Boolean hasPdf) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            return Result.error(400, "分页参数不合法：page >= 1，1 <= size <= 100");
        }
        if (yearFrom != null && yearTo != null && yearFrom > yearTo) {
            return Result.error(400, "开始年份不能大于结束年份");
        }

        return Result.success("success", searchService.searchPapers(
                page,
                size,
                q,
                diseaseIds,
                geneIds,
                drugIds,
                symptomIds,
                keywordIds,
                tagId,
                topicId,
                journal,
                yearFrom,
                yearTo,
                hasPdf));
    }

    @GetMapping("/facets")
    public Result<SearchFacetResponse> facets() {
        return Result.success("success", searchService.getFacets());
    }
}
