package com.bootcamp.paymentdemo.domain.webhook.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "webhook_events", uniqueConstraints = {
        @UniqueConstraint(name = "uk_webhook_event_rec_webhook_id", columnNames = {"rec_webhook_id"})
})
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long webhookId;

    private String recWebhookId;

    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    private LocalDateTime receiveAt;

    private LocalDateTime completeAt;

    private String eventStatus;

    public static Webhook register(String recWebhookId, WebhookStatus status, LocalDateTime completeAt) {
        Webhook webhook = new Webhook();
        webhook.recWebhookId = recWebhookId;
        webhook.status = status;
        webhook.receiveAt = LocalDateTime.now();
        webhook.completeAt = completeAt;

        return webhook;
    }

    public void complete() {
        this.status = WebhookStatus.COMPLETE;
        this.completeAt = LocalDateTime.now();
    }

    public void updateEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }
}
