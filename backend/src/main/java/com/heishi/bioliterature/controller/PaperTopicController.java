package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.entity.Topic;
import com.heishi.bioliterature.entity.TopicPaper;
import com.heishi.bioliterature.service.PaperService;
import com.heishi.bioliterature.service.TopicPaperService;
import com.heishi.bioliterature.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/papers/{paperId}/topics")
@RequiredArgsConstructor
public class PaperTopicController {

    private final PaperService paperService;
    private final TopicService topicService;
    private final TopicPaperService topicPaperService;

    @GetMapping
    public Result<List<Topic>> list(@PathVariable Long paperId) {
        if (paperService.getById(paperId) == null) {
            return Result.error(404, "文献不存在");
        }
        List<Long> topicIds = topicPaperService.list(Wrappers.<TopicPaper>lambdaQuery()
                        .eq(TopicPaper::getPaperId, paperId))
                .stream()
                .map(TopicPaper::getTopicId)
                .toList();
        if (topicIds.isEmpty()) {
            return Result.success("success", List.of());
        }
        return Result.success("success", topicService.list(Wrappers.<Topic>lambdaQuery()
                .in(Topic::getId, topicIds)
                .orderByAsc(Topic::getName)));
    }

    @PostMapping("/{topicId}")
    @Transactional
    public Result<Topic> add(@PathVariable Long paperId, @PathVariable Long topicId) {
        if (paperService.getById(paperId) == null) {
            return Result.error(404, "文献不存在");
        }
        Topic topic = topicService.getById(topicId);
        if (topic == null) {
            return Result.error(404, "专题不存在");
        }
        long existing = topicPaperService.count(Wrappers.<TopicPaper>lambdaQuery()
                .eq(TopicPaper::getPaperId, paperId)
                .eq(TopicPaper::getTopicId, topicId));
        if (existing > 0) {
            return Result.success("文献已加入该专题", topic);
        }
        try {
            topicPaperService.save(TopicPaper.builder()
                    .paperId(paperId)
                    .topicId(topicId)
                    .addedBy("manual")
                    .build());
            return Result.success("文献加入专题成功", topic);
        } catch (DataIntegrityViolationException exception) {
            return Result.success("文献已加入该专题", topic);
        }
    }

    @DeleteMapping("/{topicId}")
    @Transactional
    public Result<Void> remove(@PathVariable Long paperId, @PathVariable Long topicId) {
        if (paperService.getById(paperId) == null) {
            return Result.error(404, "文献不存在");
        }
        if (topicService.getById(topicId) == null) {
            return Result.error(404, "专题不存在");
        }
        boolean removed = topicPaperService.remove(Wrappers.<TopicPaper>lambdaQuery()
                .eq(TopicPaper::getPaperId, paperId)
                .eq(TopicPaper::getTopicId, topicId));
        return removed
                ? Result.success("文献移出专题成功", null)
                : Result.error(404, "文献专题关联不存在");
    }
}
