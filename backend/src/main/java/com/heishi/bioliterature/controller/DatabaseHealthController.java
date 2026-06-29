package com.heishi.bioliterature.controller;

import com.heishi.bioliterature.mapper.PaperMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class DatabaseHealthController {

    private final PaperMapper paperMapper;

    @GetMapping("/database")
    public DatabaseHealthResponse database() {
        long paperCount = paperMapper.selectCount(null);
        return new DatabaseHealthResponse("UP", "biomed_literature", paperCount);
    }

    public record DatabaseHealthResponse(String status, String database, long paperCount) {
    }
}