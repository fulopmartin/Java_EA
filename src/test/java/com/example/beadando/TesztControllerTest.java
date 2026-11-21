package com.example.beadando;

import com.example.beadando.controller.TesztController;
import com.example.beadando.service.ForexProfitCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TesztControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new TesztController(new ForexProfitCalculator()))
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .build();
    }

    @Test
    void opensCalculator() throws Exception {
        mvc.perform(get("/teszt"))
                .andExpect(status().isOk())
                .andExpect(view().name("teszt"));
    }

    @Test
    void displaysCalculatedResult() throws Exception {
        mvc.perform(post("/teszt")
                        .param("openingPrice", "1.10")
                        .param("closingPrice", "1.12")
                        .param("units", "1000")
                        .param("direction", "LONG"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("result", "20"));
    }

    @Test
    void displaysErrorForZeroUnits() throws Exception {
        mvc.perform(post("/teszt")
                        .param("openingPrice", "1.10")
                        .param("closingPrice", "1.12")
                        .param("units", "0")
                        .param("direction", "LONG"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("result"));
    }

    @Test
    void displaysErrorForInvalidPrice() throws Exception {
        mvc.perform(post("/teszt")
                        .param("openingPrice", "abc")
                        .param("closingPrice", "1.12")
                        .param("units", "1000")
                        .param("direction", "LONG"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error",
                        "Adj meg érvényes árakat és egész mennyiséget."))
                .andExpect(model().attributeDoesNotExist("result"));
    }
}