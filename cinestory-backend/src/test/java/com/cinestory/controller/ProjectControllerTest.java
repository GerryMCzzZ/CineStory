package com.cinestory.controller;

import com.cinestory.model.dto.request.CreateProjectRequest;
import com.cinestory.model.dto.request.UpdateProjectRequest;
import com.cinestory.model.entity.Project;
import com.cinestory.model.entity.ProjectStatus;
import com.cinestory.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目控制器测试
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    void testCreateProject() throws Exception {
        Project mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setName("测试项目");
        mockProject.setStatus(ProjectStatus.DRAFT);

        when(projectService.createProject(any())).thenReturn(mockProject);

        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("测试项目")
                .description("这是一个测试项目")
                .novelContent("这是小说内容")
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    void testCreateProjectValidation() throws Exception {
        // 缺少必填字段
        CreateProjectRequest request = new CreateProjectRequest();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProject() throws Exception {
        Project mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setName("测试项目");
        mockProject.setStatus(ProjectStatus.DRAFT);

        when(projectService.getProjectById(1L)).thenReturn(mockProject);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    void testGetProjectNotFound() throws Exception {
        when(projectService.getProjectById(999L))
                .thenThrow(new com.cinestory.exception.ResourceNotFoundException("Project not found"));

        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProject() throws Exception {
        Project mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setName("更新后的项目");
        mockProject.setStatus(ProjectStatus.DRAFT);

        when(projectService.updateProject(eq(1L), any())).thenReturn(mockProject);

        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("更新后的项目")
                .build();

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("更新后的项目"));
    }

    @Test
    void testDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }
}
