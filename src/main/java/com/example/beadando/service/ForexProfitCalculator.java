package com.example.beadando.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ForexProfitCalculator {

    public BigDecimal calculate(BigDecimal openingPrice, BigDecimal closingPrice,
                                long units, String direction) {
        if (openingPrice.signum() <= 0 || closingPrice.signum() <= 0 || units <= 0) {
            throw new IllegalArgumentException(
                    "Az árak és a mennyiség legyenek nullánál nagyobbak.");
        }

        BigDecimal difference = switch (direction) {
            case "LONG" -> closingPrice.subtract(openingPrice);
            case "SHORT" -> openingPrice.subtract(closingPrice);
            default -> throw new IllegalArgumentException("Válassz Long vagy Short irányt.");
        };

        return difference.multiply(BigDecimal.valueOf(units));
    }
}