package com.duoc.web_bff.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.web_bff.dto.TransaccionResponse;
import com.duoc.web_bff.service.WebBffService;

@RestController
@RequestMapping("/api/web")
public class WebBffController {

    private final WebBffService webBffService;

    public WebBffController(WebBffService webBffService) {
        this.webBffService = webBffService;
    }

    @GetMapping("/transacciones")
    public List<TransaccionResponse> obtenerTransacciones(
            @RequestHeader("Authorization") String authorization) { //lee el header con el bearer token

        return webBffService.obtenerTransacciones(authorization);
    }
}
