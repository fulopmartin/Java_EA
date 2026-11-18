package com.example.beadando.controller;

import com.example.beadando.model.ArfolyamAdat;
import com.example.beadando.service.MnbSoapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SoapController {

    private final MnbSoapService mnbSoapService;

    public SoapController(MnbSoapService mnbSoapService) {
        this.mnbSoapService = mnbSoapService;
    }

    @GetMapping("/soap")
    public String soapOldal(Model model) {
        String endDate = LocalDate.now().toString();
        String startDate = LocalDate.now().minusDays(30).toString();
        return lekerdezEsMegjelenit("EUR", startDate, endDate, model);
    }

    @PostMapping("/soap")
    public String soapLekerdezes(@RequestParam String currency,
                                 @RequestParam String startDate,
                                 @RequestParam String endDate,
                                 Model model) {
        return lekerdezEsMegjelenit(currency, startDate, endDate, model);
    }

    private String lekerdezEsMegjelenit(String currency, String startDate, String endDate, Model model) {
        model.addAttribute("selectedCurrency", currency);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        try {
            List<ArfolyamAdat> adatok = mnbSoapService.getExchangeRates(startDate, endDate, currency);
            model.addAttribute("adatok", adatok);
            model.addAttribute("dates", adatok.stream().map(ArfolyamAdat::datum).toList());
            model.addAttribute("rates", adatok.stream().map(ArfolyamAdat::ertek).toList());
        } catch (Exception e) {
            model.addAttribute("error", "Hiba az MNB SOAP lekérdezés során: " + e.getMessage());
        }
        return "soap";
    }
}