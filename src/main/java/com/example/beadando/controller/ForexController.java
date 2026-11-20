package com.example.beadando.controller;

import com.example.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.regex.Pattern;

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

    @GetMapping("/forex-nyit")
    public String forexNyit(Model model) {
        model.addAttribute("instruments", oandaService.getInstruments());
        return "forex-nyit";
    }

    @PostMapping("/forex-nyit")
    public String forexNyitSubmit(@RequestParam("instrument") String instrument,
                                  @RequestParam("units") String units,
                                  RedirectAttributes redirect) {
        if (!oandaService.getInstruments().contains(instrument)) {
            redirect.addFlashAttribute("error", "Válassz érvényes instrumentumot.");
            return "redirect:/forex-nyit";
        }

        long quantity;
        try {
            quantity = Long.parseLong(units.trim());
        } catch (NumberFormatException e) {
            redirect.addFlashAttribute("error", "A mennyiség egész szám legyen.");
            return "redirect:/forex-nyit";
        }

        if (quantity == 0) {
            redirect.addFlashAttribute("error", "A mennyiség nem lehet nulla.");
            return "redirect:/forex-nyit";
        }

        try {
            OandaService.OpenTradeResult trade = oandaService.openMarketTrade(instrument, quantity);
            if (trade == null) {
                redirect.addFlashAttribute("error", "Az OANDA nem igazolta vissza a pozíció megnyitását.");
            } else {
                redirect.addFlashAttribute("success",
                        "Sikeres nyitás. Trade ID: " + trade.tradeID()
                                + ", teljesítési ár: " + trade.price());
            }
        } catch (RestClientException e) {
            if (e instanceof RestClientResponseException response) {
                System.err.println("OANDA nyitás – HTTP " + response.getStatusCode().value());

                var reason = Pattern.compile("\"(?:rejectReason|errorCode)\"\\s*:\\s*\"([A-Z0-9_]+)\"")
                        .matcher(response.getResponseBodyAsString());

                while (reason.find()) {
                    System.err.println("OANDA nyitás – ok: " + reason.group(1));
                }
            } else {
                System.err.println("OANDA nyitás – "
                        + e.getMostSpecificCause().getClass().getSimpleName());
            }

            redirect.addFlashAttribute("error",
                    "A nyitást nem sikerült visszaigazolni. Újrapróbálás előtt ellenőrizd az OANDA-fiókot.");
        }

        return "redirect:/forex-nyit";
    }
}