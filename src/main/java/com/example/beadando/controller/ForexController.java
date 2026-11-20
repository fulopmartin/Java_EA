package com.example.beadando.controller;

import com.example.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import java.util.List;

@Controller
public class ForexController {

    private static final List<String> GRANULARITIES = List.of("M1", "H1", "D", "W", "M");
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

    @GetMapping("/forex-histar")
    public String forexHistAr(@RequestParam(name = "instrument", required = false) String instrument,
                              @RequestParam(name = "granularity", defaultValue = "D") String granularity,
                              Model model) {
        List<String> instruments = oandaService.getInstruments();
        model.addAttribute("instruments", instruments);
        model.addAttribute("selectedInstrument",
                instrument != null && instruments.contains(instrument) ? instrument : "EUR_USD");
        model.addAttribute("selectedGranularity", GRANULARITIES.contains(granularity) ? granularity : "D");

        if (instrument != null) {
            if (!instruments.contains(instrument) || !GRANULARITIES.contains(granularity)) {
                model.addAttribute("error", "Válasszon érvényes instrumentumot és időfelbontást.");
            } else {
                try {
                    model.addAttribute("candles", oandaService.getHistoricalPrices(instrument, granularity));
                } catch (RestClientException e) {
                    model.addAttribute("error", "Nem sikerült lekérdezni a historikus árakat az OANDA-tól.");
                }
            }
        }
        return "forex-histar";
    }
}