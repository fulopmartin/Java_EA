package com.example.beadando.controller;

import com.example.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping("/forex-aktar")
    public String forexAktAr(@RequestParam(required = false) String instrument, Model model) {
        model.addAttribute("instruments", oandaService.getInstruments());
        model.addAttribute("selectedInstrument", instrument != null ? instrument : "EUR_USD");

        if (instrument != null && !instrument.isBlank()) {
            try {
                model.addAttribute("priceInfo", oandaService.getCurrentPrice(instrument));
            } catch (Exception e) {
                model.addAttribute("error", "Nem sikerült lekérdezni az aktuális árat: " + e.getMessage());
            }
        }
        return "forex-aktar";
    }
}