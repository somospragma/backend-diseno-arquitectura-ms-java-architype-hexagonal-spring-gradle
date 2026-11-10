package co.com.pragma.accounts.infrastructure.publisher;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-publisher")
public class EventPublisherProperties {

    private String queueUrl = "http://localhost/queue/account-events";

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }
}
