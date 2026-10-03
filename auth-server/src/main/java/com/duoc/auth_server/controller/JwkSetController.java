package com.duoc.auth_server.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//expone la clave publica para que transacciones-service pueda validar los jwt
@RestController
public class JwkSetController {

    private final JWKSet publicJwkSet;

    public JwkSetController(JWKSet publicJwkSet) {
        this.publicJwkSet = publicJwkSet;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getPublicKeys() {
        return publicJwkSet.toJSONObject();
    }

}
