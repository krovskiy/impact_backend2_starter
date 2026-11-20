package com.impact.ecommerce.pricing;

import com.impact.ecommerce.entities.Category;
import java.math.BigDecimal;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public boolean appliesTo(Category category){
        return true;
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice){
        return basePrice;
    }
}
