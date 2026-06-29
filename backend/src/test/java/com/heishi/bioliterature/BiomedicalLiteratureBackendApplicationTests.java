package com.heishi.bioliterature;

import com.heishi.bioliterature.controller.DatabaseHealthController;
import com.heishi.bioliterature.controller.HealthController;
import com.heishi.bioliterature.mapper.PaperMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}