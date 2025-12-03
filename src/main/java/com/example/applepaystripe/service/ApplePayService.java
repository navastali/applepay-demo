package com.example.applepaystripe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Token;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplePayService {

    @Value("${apple.merchant.id}")
    private String merchantId;

    @Value("${apple.merchant.displayName}")
    private String displayName;

    @Value("${apple.merchant.domain}")
    private String domainName;

    @Value("${apple.merchant.p12.path}")
    private String merchantP12Path;

    @Value("${apple.merchant.p12.password}")
    private String merchantP12Password;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> performMerchantValidation(String validationURL) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream ksStream = new FileInputStream(merchantP12Path)) {
            keyStore.load(ksStream, merchantP12Password.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, merchantP12Password.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        String body = String.format(
            "{"merchantIdentifier":"%s","domainName":"%s","displayName":"%s"}",
            merchantId, domainName, displayName
        );

        URL url = new URL(validationURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        InputStream is = conn.getInputStream();
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return mapper.readValue(json, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> processPayment(Object tokenObj, Integer amount, String currency) throws Exception {
        Stripe.apiKey = stripeApiKey;

        if (amount == null) amount = 100;
        if (currency == null) currency = "usd";

        // tokenObj is the payment.token object from Apple Pay
        Map<String, Object> tokenMap = (Map<String, Object>) tokenObj;
        Map<String, Object> paymentData = (Map<String, Object>) tokenMap.get("paymentData");

        if (paymentData == null) {
            throw new IllegalArgumentException("Invalid Apple Pay token: missing paymentData");
        }

        // Build params to create a Stripe Token from the Apple Pay payment data
        Map<String, Object> applePayParams = new HashMap<>();
        applePayParams.put("token", paymentData);

        Map<String, Object> tokenParams = new HashMap<>();
        tokenParams.put("apple_pay", applePayParams);

        // Create a Stripe Token representing the Apple Pay card info
        Token stripeToken = Token.create(tokenParams);

        // Create & confirm a PaymentIntent using the Stripe Token
        PaymentIntentCreateParams params =
            PaymentIntentCreateParams.builder()
                .setAmount(amount.longValue())
                .setCurrency(currency)
                .setConfirm(true)
                .setPaymentMethodData(
                        PaymentIntentCreateParams.PaymentMethodData.builder()
                            .setType("card")
                            .setCard(
                                PaymentIntentCreateParams.PaymentMethodData.Card.builder()
                                    .setToken(stripeToken.getId())
                                    .build()
                            )
                            .build()
                )
                .build();

        PaymentIntent pi = PaymentIntent.create(params);

        Map<String,Object> result=new HashMap<>();
        result.put("success",true);
        result.put("payment_intent_id",pi.getId());
        result.put("client_secret",pi.getClientSecret());
        return result;
    }
}
