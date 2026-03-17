package dev.rest.practice.product.dto;

import dev.rest.practice.product.entity.Product;
import lombok.Builder;

@Builder
public record ProductResDto(
        Long id,
        String name,
        String description,
        Integer price,
        Integer stock,
        String category,
        Long userId
        ) {

    public static ProductResDto from(Product product) {
        return ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                 .userId(product.getUser().getId()) // 실제 Entity에 연관관계 매핑 시 적용
                .build();
    }
}