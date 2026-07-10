package com.heishi.bioliterature.controller;

import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.dto.PubMedCollectRequest;
import com.heishi.bioliterature.dto.PubMedCollectResponse;
import com.heishi.bioliterature.service.PubMedCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl/pubmed")
@RequiredArgsConstructor
public class PubMedCollectController {

    private final PubMedCollectService pubMedCollectService;

    @PostMapping
    public Result<PubMedCollectResponse> collect(@RequestBody PubMedCollectRequest request) {
        String validation = validate(request);
        if (validation != null) {
            return Result.error(400, validation);
        }
        try {
            PubMedCollectResponse response = pubMedCollectService.collect(request);
            String message = response.getFailedCount() != null && response.getFailedCount() > 0
                    ? "PubMed 采集完成，但存在失败记录"
                    : "PubMed 采集完成";
            return Result.success(message, response);
        } catch (IllegalStateException exception) {
            return Result.error(500, exception.getMessage());
        }
    }

    private String validate(PubMedCollectRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            return "检索关键词不能为空";
        }
        if (request.getRetmax() == null) {
            request.setRetmax(10);
        }
        if (request.getRetmax() < 1 || request.getRetmax() > 100) {
            return "最大采集数量必须在 1 到 100 之间";
        }
        request.setQuery(request.getQuery().trim());
        return null;
    }
}
