package com.impact.ecommerce.pricing;

import com.impact.ecommerce.entities.Category;
import java.math.BigDecimal;

public interface DiscountStrategy {
    boolean appliesTo(Category category);
    BigDecimal apply(BigDecimal basePrice);
}
