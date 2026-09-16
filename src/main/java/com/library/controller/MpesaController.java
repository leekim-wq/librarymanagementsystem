package com.library.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.library.service.MpesaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
public class MpesaController {

    private static final Logger log = LoggerFactory.getLogger(MpesaController.class);
    private final MpesaService mpesaService;

    /**
     * POST /api/mpesa/pay
     * Body: { "phoneNumber": "0712345678", "amount": 500, "memberId": 3 }
     */
    @PostMapping("/pay")
    public ResponseEntity<?> payFine(@RequestBody Map<String, Object> body) {
        try {
            String phone = (String) body.get("phoneNumber");
            double amount = Double.parseDouble(body.get("amount").toString());
            String memberId = body.getOrDefault("memberId", "M").toString();

            // ---- Custom Account Reference ----
            // This appears in the member's M-Pesa SMS confirmation.
            // Sandbox always shows "Daraja Sandbox" as the top-level name;
            // the Account field is what we can customize.
            String accountRef = "SmartLibrary-Fine";
            String description = "Smart Library - Fine Payment";

            JsonNode response = mpesaService.initiateStkPush(
                    phone, amount, accountRef, description);

            String checkoutId = response.path("CheckoutRequestID").asText();
            String message = response.path("CustomerMessage").asText("STK Push sent");

            if (checkoutId.isEmpty()) {
                return ResponseEntity.status(500).body(Map.of(
                        "success", false,
                        "message", "M-Pesa request failed",
                        "raw", response.toString()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", message,
                    "checkoutRequestId", checkoutId
            ));
        } catch (Exception e) {
            log.error("M-Pesa payment error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Payment failed: " + e.getMessage()
            ));
        }
    }

    /**
     * POST /api/mpesa/callback
     * Safaricom sends the payment result here.
     */
    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody JsonNode body) {
        try {
            log.info("📩 M-Pesa callback received: {}", body.toString());

            JsonNode stkCallback = body.path("Body").path("stkCallback");
            String resultCode = stkCallback.path("ResultCode").asText();
            String resultDesc = stkCallback.path("ResultDesc").asText();
            String checkoutId = stkCallback.path("CheckoutRequestID").asText();

            if ("0".equals(resultCode)) {
                JsonNode items = stkCallback.path("CallbackMetadata").path("Item");
                String amount = "", receipt = "", phone = "";

                for (JsonNode item : items) {
                    String name = item.path("Name").asText();
                    switch (name) {
                        case "Amount" -> amount = item.path("Value").asText();
                        case "MpesaReceiptNumber" -> receipt = item.path("Value").asText();
                        case "PhoneNumber" -> phone = item.path("Value").asText();
                    }
                }

                log.info("✅ Payment SUCCESS: Receipt={} Amount={} Phone={} CheckoutId={}",
                        receipt, amount, phone, checkoutId);
            } else {
                log.warn("❌ Payment FAILED: Code={} Reason={} CheckoutId={}",
                        resultCode, resultDesc, checkoutId);
            }

            return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
        } catch (Exception e) {
            log.error("Error handling M-Pesa callback: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
        }
    }

    /**
     * POST /api/mpesa/timeout — required by Daraja.
     */
    @PostMapping("/timeout")
    public ResponseEntity<?> timeout(@RequestBody JsonNode body) {
        log.warn("⏱️ M-Pesa timeout: {}", body.toString());
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    /**
     * GET /api/mpesa/health — verify token acquisition.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            String token = mpesaService.getAccessToken();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "M-Pesa API reachable",
                    "tokenAcquired", !token.isEmpty()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "M-Pesa API unreachable: " + e.getMessage()
            ));
        }
    }
}