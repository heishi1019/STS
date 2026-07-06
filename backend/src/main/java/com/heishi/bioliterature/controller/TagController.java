package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.entity.Tag;
import com.heishi.bioliterature.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagService.list(
                Wrappers.<Tag>lambdaQuery().orderByAsc(Tag::getName)));
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Tag tag) {
        String validation = validate(tag);
        if (validation != null) {
            return Result.error(400, validation);
        }
        tag.setId(null);
        tag.setCreatedAt(null);
        tag.setUpdatedAt(null);
        try {
            tagService.save(tag);
            return Result.success("标签创建成功", tagService.getById(tag.getId()));
        } catch (DuplicateKeyException exception) {
            return Result.error(409, "标签名称已存在");
        }
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable Long id, @RequestBody Tag tag) {
        String validation = validate(tag);
        if (validation != null) {
            return Result.error(400, validation);
        }
        if (tagService.getById(id) == null) {
            return Result.error(404, "标签不存在");
        }
        tag.setId(id);
        tag.setCreatedAt(null);
        tag.setUpdatedAt(null);
        try {
            tagService.updateById(tag);
            return Result.success("标签更新成功", tagService.getById(id));
        } catch (DuplicateKeyException exception) {
            return Result.error(409, "标签名称已存在");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return tagService.removeById(id)
                ? Result.success("标签删除成功", null)
                : Result.error(404, "标签不存在");
    }

    private String validate(Tag tag) {
        if (tag == null || !StringUtils.hasText(tag.getName())) {
            return "标签名称不能为空";
        }
        if (StringUtils.hasText(tag.getColor())
                && !tag.getColor().matches("^#[0-9A-Fa-f]{6}$")) {
            return "标签颜色必须是 #RRGGBB 格式";
        }
        tag.setName(tag.getName().trim());
        return null;
    }
}