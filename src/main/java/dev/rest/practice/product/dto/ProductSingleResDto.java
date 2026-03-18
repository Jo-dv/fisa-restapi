package dev.rest.practice.product.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;

@JsonPropertyOrder({"id", "name", "description", "price", "stock", "category", "userId", "_links"})
public record ProductSingleResDto(
        Long id,
        String name,
        String description,
        Integer price,
        Integer stock,
        String category,
        Long userId,
        Map<String, Object> _links
) {}