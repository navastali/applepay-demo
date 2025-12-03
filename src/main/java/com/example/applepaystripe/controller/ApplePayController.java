package com.example.applepaystripe.controller;

import com.example.applepaystripe.service.ApplePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ApplePayController {

    @Autowired
    private ApplePayService applePayService;

    @PostMapping("/validate-merchant")
    public ResponseEntity<?> validateMerchant(@RequestBody Map<String, String> body) {
        try {
            String validationURL = body.get("validationURL");
            if (validationURL == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "validationURL required");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> session = applePayService.performMerchantValidation(validationURL);
            return ResponseEntity.ok(session);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody Map<String, Object> body) {
        try {
            Object token = body.get("token");

            Integer amount = null;
            if (body.get("amount") instanceof Integer) {
                amount = (Integer) body.get("amount");
            } else if (body.get("amount") instanceof Number) {
                amount = ((Number) body.get("amount")).intValue();
            }

            String currency = (String) body.get("currency");

            Map<String, Object> result = applePayService.processPayment(token, amount, currency);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
