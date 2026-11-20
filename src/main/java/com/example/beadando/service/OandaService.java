package com.example.beadando.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    public List<String> getInstruments() {
        return List.of(
                "EUR_USD",
                "GBP_USD",
                "USD_JPY",
                "EUR_HUF",
                "USD_HUF",
                "GBP_HUF",
                "USD_CHF",
                "AUD_USD",
                "USD_CAD",
                "EUR_GBP"
        );
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

    @SuppressWarnings("unchecked")
    public Map<String, String> getCurrentPrice(String instrument) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accounts/{accountId}/pricing")
                        .queryParam("instruments", instrument)
                        .build(accountId))
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.containsKey("prices")) {
            throw new RuntimeException("Nem érkezett árfolyamadat az Oanda API-tól.");
        }

        List<?> prices = (List<?>) response.get("prices");
        if (prices == null || prices.isEmpty()) {
            throw new RuntimeException("Nem található árfolyam a kiválasztott instrumentumhoz: " + instrument);
        }

        Map<String, Object> priceData = (Map<String, Object>) prices.get(0);
        List<?> bids = (List<?>) priceData.get("bids");
        List<?> asks = (List<?>) priceData.get("asks");

        String bidStr = (bids != null && !bids.isEmpty())
                ? String.valueOf(((Map<?, ?>) bids.get(0)).get("price"))
                : String.valueOf(priceData.getOrDefault("closeoutBid", "-"));
        String askStr = (asks != null && !asks.isEmpty())
                ? String.valueOf(((Map<?, ?>) asks.get(0)).get("price"))
                : String.valueOf(priceData.getOrDefault("closeoutAsk", "-"));
        String spreadStr = "-";

        try {
            double bidVal = Double.parseDouble(bidStr);
            double askVal = Double.parseDouble(askStr);
            spreadStr = String.format(Locale.US, "%.5f", askVal - bidVal);
        } catch (NumberFormatException ignored) {
        }

        Map<String, String> arfolyam = new LinkedHashMap<>();
        arfolyam.put("Instrumentum", String.valueOf(priceData.getOrDefault("instrument", instrument)));
        arfolyam.put("Vételi ár (Bid)", bidStr);
        arfolyam.put("Eladási ár (Ask)", askStr);
        arfolyam.put("Spread (Ask - Bid)", spreadStr);
        arfolyam.put("Státusz", String.valueOf(priceData.getOrDefault("status", "-")));
        arfolyam.put("Időbélyeg (UTC)", String.valueOf(priceData.getOrDefault("time", "-")));

        return arfolyam;
    }
    public List<HistoricalCandle> getHistoricalPrices(String instrument, String granularity) {
        CandlesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/instruments/{instrument}/candles")
                        .queryParam("granularity", granularity)
                        .queryParam("count", 10)
                        .queryParam("price", "M")
                        .build(instrument))
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(CandlesResponse.class);

        return response == null || response.candles() == null ? List.of() : response.candles();
    }

    private record CandlesResponse(List<HistoricalCandle> candles) {
    }

    public record HistoricalCandle(String time, MidPrice mid, boolean complete) {
    }

    public record MidPrice(String c) {
    }
    public OpenTradeResult openMarketTrade(String instrument, long units) {
        Map<String, String> order = Map.of(
                "instrument", instrument,
                "units", Long.toString(units),
                "type", "MARKET",
                "timeInForce", "FOK",
                "positionFill", "OPEN_ONLY"
        );

        MarketOrderResponse response = restClient.post()
                .uri("/accounts/{accountId}/orders", accountId)
                .header("Authorization", "Bearer " + apiToken)
                .body(Map.of("order", order))
                .retrieve()
                .body(MarketOrderResponse.class);

        if (response == null || response.orderFillTransaction() == null
                || response.orderFillTransaction().tradeOpened() == null) {
            return null;
        }

        return response.orderFillTransaction().tradeOpened();
    }

    private record MarketOrderResponse(OrderFill orderFillTransaction) {
    }

    private record OrderFill(OpenTradeResult tradeOpened) {
    }

    public record OpenTradeResult(String tradeID, String price) {
    }
}
