package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.entity.Topic;
import com.heishi.bioliterature.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public Result<List<Topic>> list() {
        return Result.success("success", topicService.list(
                Wrappers.<Topic>lambdaQuery().orderByAsc(Topic::getName)));
    }

    @PostMapping
    public Result<Topic> create(@RequestBody Topic topic) {
        String validation = validate(topic);
        if (validation != null) {
            return Result.error(400, validation);
        }
        topic.setId(null);
        topic.setCreatedAt(null);
        topic.setUpdatedAt(null);
        try {
            topicService.save(topic);
            return Result.success("专题创建成功", topicService.getById(topic.getId()));
        } catch (DataIntegrityViolationException exception) {
            return Result.error(409, "专题名称或 slug 已存在");
        }
    }

    @PutMapping("/{id}")
    public Result<Topic> update(@PathVariable Long id, @RequestBody Topic topic) {
        String validation = validate(topic);
        if (validation != null) {
            return Result.error(400, validation);
        }
        if (topicService.getById(id) == null) {
            return Result.error(404, "专题不存在");
        }
        topic.setId(id);
        topic.setCreatedAt(null);
        topic.setUpdatedAt(null);
        try {
            topicService.update(Wrappers.<Topic>lambdaUpdate()
                    .eq(Topic::getId, id)
                    .set(Topic::getName, topic.getName())
                    .set(Topic::getSlug, topic.getSlug())
                    .set(Topic::getDescription, topic.getDescription())
                    .set(Topic::getSearchQuery, topic.getSearchQuery()));
            return Result.success("专题更新成功", topicService.getById(id));
        } catch (DataIntegrityViolationException exception) {
            return Result.error(409, "专题名称或 slug 已存在");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return topicService.removeById(id)
                ? Result.success("专题删除成功", null)
                : Result.error(404, "专题不存在");
    }

    private String validate(Topic topic) {
        if (topic == null || !StringUtils.hasText(topic.getName())) {
            return "专题名称不能为空";
        }
        if (!StringUtils.hasText(topic.getSlug())) {
            return "专题 slug 不能为空";
        }
        topic.setName(topic.getName().trim());
        topic.setSlug(topic.getSlug().trim());
        if (!StringUtils.hasText(topic.getDescription())) {
            topic.setDescription(null);
        }
        if (!StringUtils.hasText(topic.getSearchQuery())) {
            topic.setSearchQuery(null);
        }
        return null;
    }
}
