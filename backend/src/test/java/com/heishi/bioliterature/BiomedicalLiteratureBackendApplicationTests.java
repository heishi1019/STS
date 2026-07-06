package com.heishi.bioliterature;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heishi.bioliterature.controller.DatabaseHealthController;
import com.heishi.bioliterature.controller.HealthController;
import com.heishi.bioliterature.controller.PaperController;
import com.heishi.bioliterature.controller.PaperTagController;
import com.heishi.bioliterature.controller.TagController;
import com.heishi.bioliterature.controller.TopicController;
import com.heishi.bioliterature.controller.TopicPaperController;
import com.heishi.bioliterature.entity.Tag;
import com.heishi.bioliterature.mapper.PaperMapper;
import com.heishi.bioliterature.service.PaperService;
import com.heishi.bioliterature.service.PaperTagService;
import com.heishi.bioliterature.service.TagService;
import com.heishi.bioliterature.service.TopicPaperService;
import com.heishi.bioliterature.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BiomedicalLiteratureBackendApplicationTests {

    @Test
    void healthEndpointReturnsUp() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new HealthController())
                .build();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("biomedical-literature-backend"));
    }

    @Test
    void databaseHealthEndpointReturnsPaperCount() throws Exception {
        PaperMapper paperMapper = mock(PaperMapper.class);
        when(paperMapper.selectCount(isNull())).thenReturn(0L);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new DatabaseHealthController(paperMapper))
                .build();

        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("biomed_literature"))
                .andExpect(jsonPath("$.paperCount").value(0));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void paperPageEndpointReturnsUnifiedResult() throws Exception {
        PaperService paperService = mock(PaperService.class);
        Page emptyPage = Page.of(1, 20);
        when(paperService.page(any(Page.class), any(Wrapper.class))).thenReturn(emptyPage);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new PaperController(paperService))
                .build();

        mockMvc.perform(get("/api/papers").param("page", "1").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void missingPaperReturnsNotFoundResult() throws Exception {
        PaperService paperService = mock(PaperService.class);
        when(paperService.getById(999L)).thenReturn(null);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new PaperController(paperService))
                .build();

        mockMvc.perform(get("/api/papers/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("文献不存在"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void tagListEndpointReturnsTags() throws Exception {
        TagService tagService = mock(TagService.class);
        when(tagService.list(any(Wrapper.class))).thenReturn(List.of(
                Tag.builder().id(1L).name("重点").color("#3366FF").build()));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TagController(tagService))
                .build();

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("重点"))
                .andExpect(jsonPath("$.data[0].color").value("#3366FF"));
    }

    @Test
    void topicCreateRejectsMissingSlug() throws Exception {
        TopicService topicService = mock(TopicService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TopicController(topicService))
                .build();

        mockMvc.perform(post("/api/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"肿瘤免疫治疗\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("专题 slug 不能为空"));
    }

    @Test
    void paperTagEndpointRejectsMissingPaper() throws Exception {
        PaperService paperService = mock(PaperService.class);
        TagService tagService = mock(TagService.class);
        PaperTagService paperTagService = mock(PaperTagService.class);
        when(paperService.getById(999L)).thenReturn(null);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new PaperTagController(
                        paperService, tagService, paperTagService))
                .build();

        mockMvc.perform(post("/api/papers/999/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("文献不存在"));
    }

    @Test
    void topicPaperEndpointRejectsMissingTopic() throws Exception {
        TopicService topicService = mock(TopicService.class);
        PaperService paperService = mock(PaperService.class);
        TopicPaperService topicPaperService = mock(TopicPaperService.class);
        when(topicService.getById(999L)).thenReturn(null);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TopicPaperController(
                        topicService, paperService, topicPaperService))
                .build();

        mockMvc.perform(get("/api/topics/999/papers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("专题不存在"));
    }
}
