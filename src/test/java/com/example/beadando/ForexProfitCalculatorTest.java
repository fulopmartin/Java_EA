package com.example.beadando;

import com.example.beadando.service.ForexProfitCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForexProfitCalculatorTest {

    private final ForexProfitCalculator calculator = new ForexProfitCalculator();

    @Test
    void longIsProfitableWhenPriceRises() {
        BigDecimal result = calculator.calculate(
                new BigDecimal("1.10"), new BigDecimal("1.12"), 1000, "LONG");
        assertEquals(0, new BigDecimal("20").compareTo(result));
    }

    @Test
    void longLosesWhenPriceFalls() {
        BigDecimal result = calculator.calculate(
                new BigDecimal("1.12"), new BigDecimal("1.10"), 1000, "LONG");
        assertEquals(0, new BigDecimal("-20").compareTo(result));
    }

    @Test
    void shortIsProfitableWhenPriceFalls() {
        BigDecimal result = calculator.calculate(
                new BigDecimal("1.12"), new BigDecimal("1.10"), 1000, "SHORT");
        assertEquals(0, new BigDecimal("20").compareTo(result));
    }

    @Test
    void shortLosesWhenPriceRises() {
        BigDecimal result = calculator.calculate(
                new BigDecimal("1.10"), new BigDecimal("1.12"), 1000, "SHORT");
        assertEquals(0, new BigDecimal("-20").compareTo(result));
    }

    @Test
    void rejectsZeroUnits() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(BigDecimal.ONE, BigDecimal.ONE, 0, "LONG"));
    }

    @Test
    void rejectsZeroOpeningPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(BigDecimal.ZERO, BigDecimal.ONE, 1, "LONG"));
    }

    @Test
    void rejectsUnknownDirection() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(BigDecimal.ONE, BigDecimal.ONE, 1, "OTHER"));
    }
}