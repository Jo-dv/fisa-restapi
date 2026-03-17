package dev.rest.practice.product.service;

import dev.rest.practice.product.dto.ProductReqDto;
import dev.rest.practice.product.dto.ProductResDto;
import dev.rest.practice.product.entity.Product;
import dev.rest.practice.product.repository.ProductRepository;
import dev.rest.practice.user.entity.User;
import dev.rest.practice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductResDto createProduct(ProductReqDto reqDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        Product savedProduct = productRepository.save(reqDto.toEntity(user));
        return ProductResDto.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResDto> getProductsPage(String category, Pageable pageable) {
        Page<Product> productPage;

        if (category != null && !category.isBlank()) {
            productPage = productRepository.findByCategory(category, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(ProductResDto::from);
    }

    @Transactional(readOnly = true)
    public ProductResDto getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return ProductResDto.from(product);
    }

    @Transactional
    public ProductResDto updateProduct(Long id, ProductReqDto reqDto) {
        Product product = findProductOrThrow(id);
        product.update(reqDto.name(), reqDto.description(), reqDto.price(), reqDto.stock(), reqDto.category());
        return ProductResDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + id));
    }
}
