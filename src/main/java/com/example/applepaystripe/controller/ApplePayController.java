package com.example.applepaystripe.controller;

import com.example.applepaystripe.service.ApplePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ApplePayController {

    @Autowired
    private ApplePayService applePayService;

    @PostMapping("/validate-merchant")
    public ResponseEntity<?> validateMerchant(@RequestBody Map<String, String> body) {
        try {
            String validationURL = body.get("validationURL");
            if (validationURL == null) return ResponseEntity.badRequest().body(Map.of("error", "validationURL required"));
            Map<String, Object> session = applePayService.performMerchantValidation(validationURL);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody Map<String, Object> body) {
        try {
            Object token = body.get("token");
            Integer amount = (body.get("amount") instanceof Integer) ? (Integer) body.get("amount") : ((Number)body.get("amount")).intValue();
            String currency = (String) body.get("currency");

            Map<String, Object> result = applePayService.processPayment(token, amount, currency);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
