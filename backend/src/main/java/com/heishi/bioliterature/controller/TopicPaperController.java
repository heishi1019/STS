package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.entity.Paper;
import com.heishi.bioliterature.entity.TopicPaper;
import com.heishi.bioliterature.service.PaperService;
import com.heishi.bioliterature.service.TopicPaperService;
import com.heishi.bioliterature.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics/{topicId}/papers")
@RequiredArgsConstructor
public class TopicPaperController {

    private final TopicService topicService;
    private final PaperService paperService;
    private final TopicPaperService topicPaperService;

    @PostMapping("/{paperId}")
    public Result<Paper> add(@PathVariable Long topicId, @PathVariable Long paperId) {
        if (topicService.getById(topicId) == null) {
            return Result.error(404, "专题不存在");
        }
        Paper paper = paperService.getById(paperId);
        if (paper == null) {
            return Result.error(404, "文献不存在");
        }
        long existing = topicPaperService.count(Wrappers.<TopicPaper>lambdaQuery()
                .eq(TopicPaper::getTopicId, topicId)
                .eq(TopicPaper::getPaperId, paperId));
        if (existing > 0) {
            return Result.success("文献已加入该专题", paper);
        }
        try {
            topicPaperService.save(TopicPaper.builder()
                    .topicId(topicId)
                    .paperId(paperId)
                    .addedBy("manual")
                    .build());
            return Result.success("文献加入专题成功", paper);
        } catch (DuplicateKeyException exception) {
            return Result.success("文献已加入该专题", paper);
        }
    }

    @DeleteMapping("/{paperId}")
    public Result<Void> remove(@PathVariable Long topicId, @PathVariable Long paperId) {
        boolean removed = topicPaperService.remove(Wrappers.<TopicPaper>lambdaQuery()
                .eq(TopicPaper::getTopicId, topicId)
                .eq(TopicPaper::getPaperId, paperId));
        return removed
                ? Result.success("文献移出专题成功", null)
                : Result.error(404, "专题文献关联不存在");
    }

    @GetMapping
    public Result<List<Paper>> list(@PathVariable Long topicId) {
        if (topicService.getById(topicId) == null) {
            return Result.error(404, "专题不存在");
        }
        List<Long> paperIds = topicPaperService.list(Wrappers.<TopicPaper>lambdaQuery()
                        .eq(TopicPaper::getTopicId, topicId))
                .stream()
                .map(TopicPaper::getPaperId)
                .toList();
        if (paperIds.isEmpty()) {
            return Result.success(List.of());
        }
        return Result.success(paperService.list(Wrappers.<Paper>lambdaQuery()
                .in(Paper::getId, paperIds)
                .orderByDesc(Paper::getPublicationYear)
                .orderByDesc(Paper::getId)));
    }
}