package com.example.beadando.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactController {

    @GetMapping("/react")
    public String react() {
        return "react";
    }
}