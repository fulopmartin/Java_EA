package com.example.beadando.service;

import com.example.beadando.model.ArfolyamAdat;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MnbSoapService {

    private static final String MNB_URL = "http://www.mnb.hu/arfolyamok.asmx";

    public List<ArfolyamAdat> getExchangeRates(String startDate, String endDate, String currency) throws Exception {
        String soapXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetExchangeRates xmlns="http://www.mnb.hu/webservices/">
                      <startDate>%s</startDate>
                      <endDate>%s</endDate>
                      <currencyNames>%s</currencyNames>
                    </GetExchangeRates>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(startDate, endDate, currency);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MNB_URL))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"http://www.mnb.hu/webservices/GetExchangeRates\"")
                .POST(HttpRequest.BodyPublishers.ofString(soapXml))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document outerDoc = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(response.body())));
        String innerXml = outerDoc.getElementsByTagName("GetExchangeRatesResult").item(0).getTextContent();

        Document innerDoc = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(innerXml)));
        NodeList dayNodes = innerDoc.getElementsByTagName("Day");

        List<ArfolyamAdat> lista = new ArrayList<>();
        for (int i = 0; i < dayNodes.getLength(); i++) {
            Element dayEl = (Element) dayNodes.item(i);
            String date = dayEl.getAttribute("date");
            NodeList rateNodes = dayEl.getElementsByTagName("Rate");
            if (rateNodes.getLength() > 0) {
                Element rateEl = (Element) rateNodes.item(0);
                double unit = Double.parseDouble(rateEl.getAttribute("unit"));
                double rateValue = Double.parseDouble(rateEl.getTextContent().replace(",", ".")) / unit;
                lista.add(new ArfolyamAdat(date, Math.round(rateValue * 100.0) / 100.0));
            }
        }

        Collections.reverse(lista);
        return lista;
    }
}