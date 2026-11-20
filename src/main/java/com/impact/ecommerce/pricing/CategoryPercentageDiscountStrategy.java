package com.impact.ecommerce.pricing;

import com.impact.ecommerce.entities.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Order(1)
public class CategoryPercentageDiscountStrategy implements DiscountStrategy {
    private final String categoryName;
    private final BigDecimal percentage;
    // self constructor cu category si percentage
    public CategoryPercentageDiscountStrategy (
        @Value("${pricing.discount.category}") String categoryName,
        @Value("${pricing.discount.percentage}") BigDecimal percentage) {
        this.categoryName = categoryName;
        this.percentage = percentage;
    }

    @Override
    public boolean appliesTo(Category category){
        return category != null
                && categoryName.equalsIgnoreCase(category.getName());
    } // Daca exista categorie sau nu pentru discount

    @Override
    public BigDecimal apply(BigDecimal basePrice){
        BigDecimal multiplier =
                BigDecimal.ONE.subtract(percentage.movePointLeft(2)); // EXAMPLE: 15% -> 0.15 , BigDecimal.ONE == 1, 1 - 0.15 = 0.85
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP); // 0.85, HALF_UP -> 16.575 -> 16.58
    }

}
