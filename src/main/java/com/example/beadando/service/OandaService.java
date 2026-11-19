package com.example.beadando.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OandaService {

    private final RestClient restClient;
    private final String apiToken;
    private final String accountId;

    public OandaService(
            @Value("${oanda.api.url}") String apiUrl,
            @Value("${oanda.api.token}") String apiToken,
            @Value("${oanda.api.account-id}") String accountId) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.apiToken = apiToken;
        this.accountId = accountId;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getAccountSummary() {
        Map<String, Object> response = restClient.get()
                .uri("/accounts/{accountId}/summary", accountId)
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.containsKey("account")) {
            throw new RuntimeException("Üres vagy érvénytelen válasz érkezett az Oanda API-tól.");
        }

        Map<String, Object> account = (Map<String, Object>) response.get("account");

        Map<String, String> adatok = new LinkedHashMap<>();
        adatok.put("Számla azonosító (ID)", String.valueOf(account.getOrDefault("id", "-")));
        adatok.put("Számla megnevezése (Alias)", String.valueOf(account.getOrDefault("alias", "-")));
        adatok.put("Alap pénznem (Currency)", String.valueOf(account.getOrDefault("currency", "-")));
        adatok.put("Egyenleg (Balance)", String.valueOf(account.getOrDefault("balance", "-")));
        adatok.put("Nettó eszközérték (NAV)", String.valueOf(account.getOrDefault("NAV", "-")));
        adatok.put("Nem realizált eredmény (Unrealized P/L)", String.valueOf(account.getOrDefault("unrealizedPL", "-")));
        adatok.put("Realizált eredmény (Realized P/L)", String.valueOf(account.getOrDefault("pl", "-")));
        adatok.put("Felhasznált fedezet (Margin Used)", String.valueOf(account.getOrDefault("marginUsed", "-")));
        adatok.put("Szabad fedezet (Margin Available)", String.valueOf(account.getOrDefault("marginAvailable", "-")));
        adatok.put("Nyitott ügyletek száma (Open Trades)", String.valueOf(account.getOrDefault("openTradeCount", "0")));
        adatok.put("Nyitott pozíciók száma (Open Positions)", String.valueOf(account.getOrDefault("openPositionCount", "0")));
        adatok.put("Függő megbízások (Pending Orders)", String.valueOf(account.getOrDefault("pendingOrderCount", "0")));

        return adatok;
    }
}
