package edu.acadvision.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ResendEmailClient {
    private final RestClient client;
    private final String from;

    public ResendEmailClient(
            @Value("${RESEND_API_KEY}") String apiKey,
            @Value("${MAIL_FROM}") String from) {
        this.from = from;
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public void send(SimpleMailMessage message) {
        Map<String, Object> payload = Map.of(
                "from", from,
                "to", List.of(message.getTo()),
                "subject", message.getSubject(),
                "text", message.getText());

        client.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}