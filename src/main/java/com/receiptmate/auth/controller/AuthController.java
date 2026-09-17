package com.receiptmate.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/sns/{provider}")
    public ResponseEntity<Void> snsLogin(@PathVariable String provider) {
        return ResponseEntity
                .status(302)
                .location(URI.create("/oauth2/authorization/" + provider))
                .build();
    }
}