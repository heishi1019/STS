package com.heishi.bioliterature.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heishi.bioliterature.common.Result;
import com.heishi.bioliterature.entity.Paper;
import com.heishi.bioliterature.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

    private static final long MAX_PAGE_SIZE = 100;

    private final PaperService paperService;

    @GetMapping
    public Result<IPage<Paper>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String dataSource) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            return Result.error(400, "分页参数不合法：page >= 1，1 <= size <= 100");
        }

        LambdaQueryWrapper<Paper> query = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Paper::getTitle, keyword.trim())
                    .or()
                    .like(Paper::getAbstractText, keyword.trim()));
        }
        query.eq(year != null, Paper::getPublicationYear, year)
                .eq(StringUtils.hasText(dataSource), Paper::getDataSource,
                        StringUtils.hasText(dataSource) ? dataSource.trim() : null)
                .orderByDesc(Paper::getPublicationYear)
                .orderByDesc(Paper::getId);

        IPage<Paper> result = paperService.page(Page.of(page, size), query);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Paper> getById(@PathVariable Long id) {
        Paper paper = paperService.getById(id);
        return paper == null
                ? Result.error(404, "文献不存在")
                : Result.success(paper);
    }

    @PostMapping
    public Result<Paper> create(@RequestBody Paper paper) {
        if (paper == null || !StringUtils.hasText(paper.getTitle())) {
            return Result.error(400, "标题不能为空");
        }
        paper.setId(null);
        paper.setCreatedAt(null);
        paper.setUpdatedAt(null);
        if (!paperService.save(paper)) {
            return Result.error(500, "文献创建失败");
        }
        return Result.success("文献创建成功", paperService.getById(paper.getId()));
    }

    @PutMapping("/{id}")
    public Result<Paper> update(@PathVariable Long id, @RequestBody Paper paper) {
        if (paper == null || !StringUtils.hasText(paper.getTitle())) {
            return Result.error(400, "标题不能为空");
        }
        if (paperService.getById(id) == null) {
            return Result.error(404, "文献不存在");
        }
        paper.setId(id);
        paper.setCreatedAt(null);
        paper.setUpdatedAt(null);
        if (!paperService.updateById(paper)) {
            return Result.error(500, "文献更新失败");
        }
        return Result.success("文献更新成功", paperService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!paperService.removeById(id)) {
            return Result.error(404, "文献不存在");
        }
        return Result.success("文献删除成功", null);
    }
}