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
            if (trade != null) {
                redirect.addFlashAttribute("success",
                        "Sikeres nyitás. Trade ID: " + trade.tradeID()
                                + ", teljesítési ár: " + trade.price());
            } else {
                redirect.addFlashAttribute("error",
                        "Az OANDA válasza nem igazolt vissza megnyitott ügyletet.");
            }
        } catch (RestClientResponseException e) {
            redirect.addFlashAttribute("error",
                    "OANDA HTTP " + e.getStatusCode().value()
                            + ". Újrapróbálás előtt ellenőrizd az ügyleteket a gyakorlófiókban.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error",
                    "Hiba történt az OANDA-kérésnél. Újrapróbálás előtt ellenőrizd az ügyleteket.");
        }

        return "redirect:/forex-nyit";
    }

    @GetMapping("/forex-poz")
    public String forexPoz(Model model) {
        try {
            model.addAttribute("positions", oandaService.getOpenPositions());
        } catch (RestClientResponseException e) {
            model.addAttribute("error",
                    "Az OANDA nem tudta lekérni a nyitott pozíciókat. HTTP "
                            + e.getStatusCode().value() + ".");
        } catch (RestClientException e) {
            model.addAttribute("error", "Nem sikerült lekérni a nyitott pozíciókat.");
        }

        return "forex-poz";
    }
}