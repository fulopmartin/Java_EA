package com.example.beadando.controller;

import com.example.beadando.service.ForexProfitCalculator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class TesztController {

    private final ForexProfitCalculator calculator;

    public TesztController(ForexProfitCalculator calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/teszt")
    public String showCalculator() {
        return "teszt";
    }

    @PostMapping("/teszt")
    public String calculate(@RequestParam("openingPrice") String openingPrice,
                            @RequestParam("closingPrice") String closingPrice,
                            @RequestParam("units") String units,
                            @RequestParam("direction") String direction,
                            Model model) {
        model.addAttribute("openingPrice", openingPrice);
        model.addAttribute("closingPrice", closingPrice);
        model.addAttribute("units", units);
        model.addAttribute("direction", direction);

        try {
            BigDecimal result = calculator.calculate(
                    new BigDecimal(openingPrice.trim()),
                    new BigDecimal(closingPrice.trim()),
                    Long.parseLong(units.trim()),
                    direction
            );
            model.addAttribute("result", result.stripTrailingZeros().toPlainString());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Adj meg érvényes árakat és egész mennyiséget.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "teszt";
    }
}