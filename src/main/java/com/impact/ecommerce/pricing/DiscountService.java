package com.impact.ecommerce.pricing;


import com.impact.ecommerce.entities.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DiscountService {
    private final List<DiscountStrategy> strategies;

    public DiscountService(List<DiscountStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal finalPriceFor(Product product){
        BigDecimal manual = product.getDiscountPercentage();
        if (manual != null && manual.signum() > 0) {
            return product.getPrice().multiply(BigDecimal.ONE.subtract(manual.movePointLeft(2)))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return strategies.stream()
                // SPUNE CAUT FILTER DOAR CATEGORII DIN DISCOUNTSTRATEGY
                .filter(strategy -> strategy.appliesTo(product.getCategory()))
                // CAUTA PRIMUL PRODUS
                .findFirst()
                // CAUTA DIN STRATEGIE PRETUL LA PRODUS
                .map(strategy -> strategy.apply(product.getPrice()))
                // CAUTA DIN PRODUS PRETUL
                .orElse(product.getPrice());
    }
}
