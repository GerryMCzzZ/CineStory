package com.cinestory.model.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API 响应 DTO 测试
 */
class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.success(data);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(data, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessResponseWithMessage() {
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.success("Operation successful", data);

        assertEquals(200, response.getCode());
        assertEquals("Operation successful", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong");

        assertEquals(500, response.getCode());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testErrorResponseWithCode() {
        ApiResponse<Void> response = ApiResponse.error(404, "Not found");

        assertEquals(404, response.getCode());
        assertEquals("Not found", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testPageResponse() {
        var items = List.of("item1", "item2", "item3");
        PageResponse<String> pageResponse = PageResponse.of(items, 10L, 0, 3);

        assertEquals(items, pageResponse.getItems());
        assertEquals(10L, pageResponse.getTotal());
        assertEquals(0, pageResponse.getPage());
        assertEquals(3, pageResponse.getSize());
        assertEquals(4, pageResponse.getTotalPages()); // (10 + 3 - 1) / 3 = 4
    }

    @Test
    void testPageResponseCalculation() {
        var items = List.of("item1", "item2");
        PageResponse<String> pageResponse = PageResponse.of(items, 5L, 1, 2);

        assertEquals(3, pageResponse.getTotalPages()); // (5 + 2 - 1) / 2 = 3
    }

    @Test
    void testPageResponseEmpty() {
        var items = List.<String>of();
        PageResponse<String> pageResponse = PageResponse.of(items, 0L, 0, 10);

        assertEquals(0, pageResponse.getItems().size());
        assertEquals(0L, pageResponse.getTotal());
        assertEquals(0, pageResponse.getTotalPages());
    }
}
