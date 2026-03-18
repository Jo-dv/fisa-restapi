package dev.rest.practice.product.controller;

import dev.rest.practice.product.dto.ProductPageResDto;
import dev.rest.practice.product.dto.ProductReqDto;
import dev.rest.practice.product.dto.ProductResDto;
import dev.rest.practice.product.dto.ProductSingleResDto;
import dev.rest.practice.product.service.ProductService;
import dev.rest.practice.user.entity.User;
import dev.rest.practice.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    // 1. 상품 등록
    @PostMapping
    public ResponseEntity<EntityModel<ProductResDto>> createProduct(@Valid @RequestBody ProductReqDto reqDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ProductResDto resDto = productService.createProduct(reqDto, username);

        // HATEOAS EntityModel 생성
        EntityModel<ProductResDto> entityModel = EntityModel.of(resDto);

        // 기본 URL 추출 (http://localhost:8080/api/products)
        WebMvcLinkBuilder baseLink = linkTo(ProductController.class);
        String basePath = baseLink.toString();

        // 1. self
        entityModel.add(linkTo(methodOn(ProductController.class).getProductById(resDto.id())).withSelfRel());

        // 2. profile
        entityModel.add(Link.of("/swagger-ui/index.html", "profile"));

        // 3. list-products (templated: true 및 type: GET 처리)
        entityModel.add(Link.of(UriTemplate.of(basePath + "?page=0&size=10{&category}"), "list-products").withType("GET"));

        // 4. update-product
        entityModel.add(linkTo(methodOn(ProductController.class).updateProduct(resDto.id(), null)).withRel("update-product").withType("PUT"));

        // 5. delete-product
        entityModel.add(linkTo(methodOn(ProductController.class).deleteProduct(resDto.id())).withRel("delete-product").withType("DELETE"));

        // 201 Created URI 생성
        URI location = linkTo(methodOn(ProductController.class).getProductById(resDto.id())).toUri();

        return ResponseEntity.created(location).body(entityModel);
    }

    // 2. 전체 상품 조회
    @GetMapping
    public ResponseEntity<ProductPageResDto> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 파라미터로 받은 page와 size를 이용해 Pageable(PageRequest) 직접 생성
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductResDto> productPage = productService.getProductsPage(category, pageable);

        Map<String, ProductPageResDto.CustomLink> links = new LinkedHashMap<>();

        String categoryParam = (category != null && !category.isBlank()) ? "&category=" + category : "";

        // 1. profile
        links.put("profile", new ProductPageResDto.CustomLink("self", "/swagger-ui/index.html"));

        // 2. self
        String selfUri = String.format("/api/products?page=%d&size=%d%s",
                productPage.getNumber(), productPage.getSize(), categoryParam);
        links.put("self", new ProductPageResDto.CustomLink("self", selfUri));

        // 3. next
        if (productPage.hasNext()) {
            String nextUri = String.format("/api/products?page=%d&size=%d%s",
                    productPage.getNumber() + 1, productPage.getSize(), categoryParam);
            links.put("next", new ProductPageResDto.CustomLink("self", nextUri));
        }

        // 4. prev
        if (productPage.hasPrevious()) {
            String prevUri = String.format("/api/products?page=%d&size=%d%s",
                    productPage.getNumber() - 1, productPage.getSize(), categoryParam);
            links.put("prev", new ProductPageResDto.CustomLink("self", prevUri));
        }

        ProductPageResDto response = new ProductPageResDto(productPage.getContent(), links);
        return ResponseEntity.ok(response);
    }

    // 3. 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductSingleResDto> getProductById(@PathVariable Long id) {
        ProductResDto resDto = productService.getProductById(id);

        // 1. 인증 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        boolean isOwner = false;
        if (isAuthenticated) {
            // 현재 인증된 사용자의 username으로 DB에서 User 객체를 찾아 ID를 비교
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null && currentUser.getId().equals(resDto.userId())) {
                isOwner = true;
            }
        }

        // 2. 기본 URI 추출
        WebMvcLinkBuilder baseLink = linkTo(ProductController.class);
        String selfUri = baseLink.toString() + "/" + id;

        // 3. 링크 맵 동적 구성
        Map<String, Object> links = new LinkedHashMap<>();

        // 기본 링크 (self, profile)
        links.put("self", Map.of("href", selfUri));
        links.put("profile", Map.of("href", "/swagger-ui/index.html"));

        // 인증된 사용자일 경우 추가 링크
        if (isAuthenticated) {
            // 재고가 있을 경우 order 링크 추가
            if (resDto.stock() != null && resDto.stock() > 0) {
                links.put("order", Map.of(
                        "href", "/api/orders",
                        "type", "POST"
                ));
            }

            // 자신이 등록한 상품일 경우 update, delete 링크 추가
            if (isOwner) {
                links.put("update-product", Map.of(
                        "href", selfUri,
                        "type", "PUT"
                ));
                links.put("delete-product", Map.of(
                        "href", selfUri,
                        "type", "DELETE"
                ));
            }
        }

        // 4. DTO 매핑 (_links가 맨 마지막에 배치됨)
        ProductSingleResDto response = new ProductSingleResDto(
                resDto.id(),
                resDto.name(),
                resDto.description(),
                resDto.price(),
                resDto.stock(),
                resDto.category(),
                resDto.userId(),
                links
        );

        return ResponseEntity.ok(response);
    }

    // 4. 상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<ProductSingleResDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductReqDto reqDto) {
        // 1. 현재 로그인한 사용자 식별자 추출
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 서비스 호출 (username 전달)
        ProductResDto resDto = productService.updateProduct(id, reqDto, username);

        // 3. HATEOAS 링크 구성
        WebMvcLinkBuilder baseLink = linkTo(ProductController.class);
        String selfUri = baseLink.toString() + "/" + resDto.id();

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", Map.of("href", selfUri));
        links.put("profile", Map.of("href", "/swagger-ui/index.html"));
        links.put("list-products", Map.of("href", baseLink.toString() + "?page=0&size=10{&category}", "templated", true, "type", "GET"));
        links.put("delete-product", Map.of("href", selfUri, "type", "DELETE"));

        ProductSingleResDto response = new ProductSingleResDto(
                resDto.id(),
                resDto.name(),
                resDto.description(),
                resDto.price(),
                resDto.stock(),
                resDto.category(),
                resDto.userId(),
                links
        );

        return ResponseEntity.ok(response);
    }

    // 5. 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}