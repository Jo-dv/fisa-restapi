package dev.rest.practice.product.entity;

import dev.rest.practice.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필수, 공백 불가
    @NotBlank(message = "상품명은 필수이며 공백일 수 없습니다.")
    @Pattern(regexp = "^\\S+$", message = "상품명 내에 공백 문자가 포함될 수 없습니다.") // 문자열 중간 공백까지 전면 허용하지 않을 경우 추가
    @Column(nullable = false)
    private String name;

    // 1000자 미만 (최대 999자)
    @Size(max = 999, message = "상품 설명은 1000자 미만이어야 합니다.")
    @Column(length = 1000)
    private String description;

    // 최소 0원 이상
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Column(nullable = false)
    private Integer price;

    // 재고는 최소 1개 이상
    @Min(value = 1, message = "재고는 최소 1개 이상이어야 합니다.")
    @Column(nullable = false)
    private Integer stock;

    // 필수, 공백 불가
    @NotBlank(message = "카테고리는 필수이며 공백일 수 없습니다.")
    @Pattern(regexp = "^\\S+$", message = "카테고리 내에 공백 문자가 포함될 수 없습니다.")
    @Column(nullable = false)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Product(String name, String description, Integer price, Integer stock, String category, User user) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.user = user;
    }

    public void update(String name, String description, Integer price, Integer stock, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
}