package com.example.beadando.controller;

import com.example.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForexController {

    private final OandaService oandaService;

    public ForexController(OandaService oandaService) {
        this.oandaService = oandaService;
    }

    @GetMapping("/forex-account")
    public String forexAccount(Model model) {
        try {
            model.addAttribute("accountInfo", oandaService.getAccountSummary());
        } catch (Exception e) {
            model.addAttribute("error", "Nem sikerült lekérdezni a számlainformációkat: " + e.getMessage());
        }
        return "forex-account";
    }
}