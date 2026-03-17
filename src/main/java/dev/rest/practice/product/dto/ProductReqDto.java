package dev.rest.practice.product.dto;

import dev.rest.practice.product.entity.Product;
import dev.rest.practice.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductReqDto(
        @Schema(description = "상품명", example = "갤럭시S24")
        @NotBlank(message = "상품명은 필수이며 공백일 수 없습니다.")
        @Pattern(regexp = "^\\S+$", message = "상품명 내에 공백 문자가 포함될 수 없습니다.")
        String name,

        @Schema(description = "상품 설명", example = "최신 스마트폰입니다.")
        @Size(max = 999, message = "상품 설명은 1000자 미만이어야 합니다.")
        String description,

        @Schema(description = "상품 가격", example = "1150000")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @Schema(description = "재고 수량", example = "100")
        @Min(value = 1, message = "재고는 최소 1개 이상이어야 합니다.")
        Integer stock,

        @Schema(description = "상품 카테고리", example = "전자제품")
        @NotBlank(message = "카테고리는 필수이며 공백일 수 없습니다.")
        @Pattern(regexp = "^\\S+$", message = "카테고리 내에 공백 문자가 포함될 수 없습니다.")
        String category
) {
    // DTO를 Entity로 변환
    public Product toEntity(User user) {
        return Product.builder()
                .name(this.name())
                .description(this.description())
                .price(this.price())
                .stock(this.stock())
                .category(this.category())
                .user(user)
                .build();
    }
}