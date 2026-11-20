package com.impact.ecommerce.dtos.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 5000) String description,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer stock,
        @Positive Long categoryId,
        @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2) BigDecimal discountPercentage) {
    public CreateProductRequest(String name, String description, BigDecimal price, Integer stock, Long categoryId) {
        this(name, description, price, stock, categoryId, null);
    }
}
