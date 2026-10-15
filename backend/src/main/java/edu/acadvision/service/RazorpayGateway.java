package edu.acadvision.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
public class RazorpayGateway {
  private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};
  private final RestClient client;
  private final String keyId;
  private final String keySecret;

  public RazorpayGateway(RestClient.Builder builder,
      @Value("${app.razorpay.key-id:}") String keyId,
      @Value("${app.razorpay.key-secret:}") String keySecret) {
    this.keyId = keyId == null ? "" : keyId.trim();
    this.keySecret = keySecret == null ? "" : keySecret.trim();
    RestClient.Builder configured = builder.baseUrl("https://api.razorpay.com/v1");
    if (!this.keyId.isBlank() && !this.keySecret.isBlank()) {
      configured.defaultHeaders(headers -> headers.setBasicAuth(this.keyId, this.keySecret));
    }
    this.client = configured.build();
  }

  public String getKeyId() { requireTestCredentials(); return keyId; }

  public Map<String, Object> createOrder(long amountPaise, String receipt, String invoiceId, String studentId) {
    requireTestCredentials();
    try {
      return client.post().uri("/orders").contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("amount", amountPaise, "currency", "INR", "receipt", receipt,
              "notes", Map.of("invoice_id", invoiceId, "student_id", studentId)))
          .retrieve().body(MAP_TYPE);
    } catch (RestClientException ex) {
      throw gatewayUnavailable();
    }
  }

  public Map<String, Object> fetchPayment(String paymentId) {
    requireTestCredentials();
    try {
      return client.get().uri("/payments/{paymentId}", paymentId).retrieve().body(MAP_TYPE);
    } catch (RestClientException ex) {
      throw gatewayUnavailable();
    }
  }

  public Map<String, Object> capturePayment(String paymentId, long amountPaise) {
    requireTestCredentials();
    try {
      return client.post().uri("/payments/{paymentId}/capture", paymentId)
          .contentType(MediaType.APPLICATION_JSON).body(Map.of("amount", amountPaise, "currency", "INR"))
          .retrieve().body(MAP_TYPE);
    } catch (RestClientException ex) {
      throw gatewayUnavailable();
    }
  }

  public boolean verifySignature(String orderId, String paymentId, String suppliedSignature) {
    requireTestCredentials();
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] expected = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
      byte[] supplied = HexFormat.of().parseHex(suppliedSignature);
      return MessageDigest.isEqual(expected, supplied);
    } catch (IllegalArgumentException ex) {
      return false;
    } catch (Exception ex) {
      throw new IllegalStateException("Could not verify Razorpay payment signature", ex);
    }
  }

  private void requireTestCredentials() {
    if (keyId.isBlank() || keySecret.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "Razorpay test keys are not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
    }
    if (!keyId.startsWith("rzp_test_")) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "Only Razorpay test-mode keys are accepted by this integration.");
    }
  }

  private ResponseStatusException gatewayUnavailable() {
    return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
        "Razorpay could not complete the request. Check the test keys and try again.");
  }
}
