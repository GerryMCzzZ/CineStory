package com.cinestory.service;

import com.cinestory.model.entity.StyleTemplate;
import com.cinestory.repository.StyleTemplateRepository;
import com.cinestory.service.impl.StyleTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 风格模板服务测试
 */
@ExtendWith(MockitoExtension.class)
class StyleTemplateServiceTest {

    @Mock
    private StyleTemplateRepository repository;

    @InjectMocks
    private StyleTemplateServiceImpl styleTemplateService;

    private StyleTemplate testStyle;

    @BeforeEach
    void setUp() {
        testStyle = new StyleTemplate();
        testStyle.setId(1L);
        testStyle.setName("日式动漫");
        testStyle.setCategory("Anime");
        testStyle.setIsSystem(true);
    }

    @Test
    void testGetAllStyles() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<StyleTemplate> page = new PageImpl<>(List.of(testStyle));

        when(repository.findAll(pageRequest)).thenReturn(page);

        Page<StyleTemplate> result = styleTemplateService.getAllStyles(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("日式动漫", result.getContent().get(0).getName());
    }

    @Test
    void testGetSystemStyles() {
        List<StyleTemplate> systemStyles = Arrays.asList(testStyle);
        when(repository.findByIsSystemTrueOrderByCategoryAscNameAsc()).thenReturn(systemStyles);

        List<StyleTemplate> result = styleTemplateService.getSystemStyles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsSystem());
    }

    @Test
    void testGetCustomStyles() {
        StyleTemplate customStyle = new StyleTemplate();
        customStyle.setId(2L);
        customStyle.setName("自定义风格");
        customStyle.setIsSystem(false);

        List<StyleTemplate> customStyles = Arrays.asList(customStyle);
        when(repository.findByIsSystemFalseOrderByCreatedAtDesc()).thenReturn(customStyles);

        List<StyleTemplate> result = styleTemplateService.getCustomStyles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsSystem());
    }

    @Test
    void testGetById() {
        when(repository.findById(1L)).thenReturn(Optional.of(testStyle));

        StyleTemplate result = styleTemplateService.getById(1L);

        assertNotNull(result);
        assertEquals("日式动漫", result.getName());
    }

    @Test
    void testGetByIdNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.cinestory.exception.ResourceNotFoundException.class, () -> {
            styleTemplateService.getById(999L);
        });
    }

    @Test
    void testGetCategories() {
        List<String> categories = Arrays.asList("Anime", "3D", "Chinese");
        when(repository.findAllCategories()).thenReturn(categories);

        List<String> result = styleTemplateService.getCategories();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Anime"));
    }

    @Test
    void testDeleteSystemStyle() {
        when(repository.findById(1L)).thenReturn(Optional.of(testStyle));

        assertThrows(IllegalStateException.class, () -> {
            styleTemplateService.delete(1L);
        });

        verify(repository, never()).deleteById(any());
    }

    @Test
    void testDeleteCustomStyle() {
        StyleTemplate customStyle = new StyleTemplate();
        customStyle.setId(2L);
        customStyle.setIsSystem(false);

        when(repository.findById(2L)).thenReturn(Optional.of(customStyle));

        assertDoesNotThrow(() -> {
            styleTemplateService.delete(2L);
        });

        verify(repository).deleteById(2L);
    }
}
