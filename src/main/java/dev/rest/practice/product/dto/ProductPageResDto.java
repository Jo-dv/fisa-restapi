package dev.rest.practice.product.dto;

import java.util.List;
import java.util.Map;

public record ProductPageResDto(
        List<ProductResDto> products,
        Map<String, CustomLink> _links
) {
    public record CustomLink(
            String rel,
            String href
    ) {}
}