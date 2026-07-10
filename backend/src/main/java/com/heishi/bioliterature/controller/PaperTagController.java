package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.entity.PaperTag;
import com.heishi.bioliterature.entity.Tag;
import com.heishi.bioliterature.service.PaperService;
import com.heishi.bioliterature.service.PaperTagService;
import com.heishi.bioliterature.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/papers/{paperId}/tags")
@RequiredArgsConstructor
public class PaperTagController {

    private final PaperService paperService;
    private final TagService tagService;
    private final PaperTagService paperTagService;

    @PostMapping("/{tagId}")
    public Result<Tag> add(@PathVariable Long paperId, @PathVariable Long tagId) {
        if (paperService.getById(paperId) == null) {
            return Result.error(404, "文献不存在");
        }
        Tag tag = tagService.getById(tagId);
        if (tag == null) {
            return Result.error(404, "标签不存在");
        }
        long existing = paperTagService.count(Wrappers.<PaperTag>lambdaQuery()
                .eq(PaperTag::getPaperId, paperId)
                .eq(PaperTag::getTagId, tagId));
        if (existing > 0) {
            return Result.success("文献已包含该标签", tag);
        }
        try {
            paperTagService.save(PaperTag.builder()
                    .paperId(paperId)
                    .tagId(tagId)
                    .build());
            return Result.success("标签添加成功", tag);
        } catch (DataIntegrityViolationException exception) {
            return Result.success("文献已包含该标签", tag);
        }
    }

    @DeleteMapping("/{tagId}")
    public Result<Void> remove(@PathVariable Long paperId, @PathVariable Long tagId) {
        boolean removed = paperTagService.remove(Wrappers.<PaperTag>lambdaQuery()
                .eq(PaperTag::getPaperId, paperId)
                .eq(PaperTag::getTagId, tagId));
        return removed
                ? Result.success("文献标签移除成功", null)
                : Result.error(404, "文献标签关联不存在");
    }

    @GetMapping
    public Result<List<Tag>> list(@PathVariable Long paperId) {
        if (paperService.getById(paperId) == null) {
            return Result.error(404, "文献不存在");
        }
        List<Long> tagIds = paperTagService.list(Wrappers.<PaperTag>lambdaQuery()
                        .eq(PaperTag::getPaperId, paperId))
                .stream()
                .map(PaperTag::getTagId)
                .toList();
        if (tagIds.isEmpty()) {
            return Result.success("success", List.of());
        }
        return Result.success("success", tagService.list(Wrappers.<Tag>lambdaQuery()
                .in(Tag::getId, tagIds)
                .orderByAsc(Tag::getName)));
    }
}
