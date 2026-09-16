package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.MpesaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final MpesaConfig config;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private String baseUrl() {
        return "sandbox".equalsIgnoreCase(config.getEnv())
                ? "https://sandbox.safaricom.co.ke"
                : "https://api.safaricom.co.ke";
    }

    /**
     * Step 1: Get OAuth access token from Safaricom.
     */
    public String getAccessToken() throws Exception {
        String credentials = config.getConsumerKey() + ":" + config.getConsumerSecret();
        String auth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(baseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + auth)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            log.debug("OAuth response status: {}", response.code());
            JsonNode node = objectMapper.readTree(body);
            String token = node.path("access_token").asText();
            if (token.isEmpty()) {
                throw new RuntimeException("Failed to get M-Pesa access token. Response: " + body);
            }
            log.info("✅ M-Pesa OAuth token acquired");
            return token;
        }
    }

    /**
     * Step 2: Initiate STK Push (prompts user to enter M-Pesa PIN).
     */
    public JsonNode initiateStkPush(String phoneNumber, double amount,
                                    String accountReference, String description) throws Exception {
        if (!config.isEnabled()) {
            throw new RuntimeException("M-Pesa is disabled. Set mpesa.enabled=true");
        }

        String token = getAccessToken();
        String normalizedPhone = normalizePhone(phoneNumber);

        log.info("Initiating STK Push to {} for KES {}", normalizedPhone, amount);

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String password = Base64.getEncoder().encodeToString(
                (config.getShortcode() + config.getPasskey() + timestamp)
                        .getBytes(StandardCharsets.UTF_8));

        String jsonBody = objectMapper.writeValueAsString(
                new StkPushRequest(
                        config.getShortcode(),
                        password,
                        timestamp,
                        "CustomerPayBillOnline",
                        (int) Math.ceil(amount),
                        normalizedPhone,
                        config.getShortcode(),
                        normalizedPhone,
                        config.getCallbackUrl(),
                        accountReference,
                        description
                )
        );

        Request request = new Request.Builder()
                .url(baseUrl() + "/mpesa/stkpush/v1/processrequest")
                .header("Authorization", "Bearer " + token)
                .post(RequestBody.create(jsonBody, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            log.info("STK Push response: {}", body);
            return objectMapper.readTree(body);
        }
    }

    private String normalizePhone(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("0")) {
            cleaned = "254" + cleaned.substring(1);
        } else if (cleaned.startsWith("7") || cleaned.startsWith("1")) {
            cleaned = "254" + cleaned;
        }
        return cleaned;
    }

    public record StkPushRequest(
            String BusinessShortCode,
            String Password,
            String Timestamp,
            String TransactionType,
            Integer Amount,
            String PartyA,
            String PartyB,
            String PhoneNumber,
            String CallBackURL,
            String AccountReference,
            String TransactionDesc
    ) {}
}