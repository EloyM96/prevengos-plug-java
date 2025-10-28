package com.prevengos.plug.hubbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("prl.notifier")
@Validated
public class PrlNotifierProperties {

    private boolean enabled = false;
    private String baseUrl = "http://localhost:9000";
    private String apiToken = "";
    private String eventsChannel = "hub.sync";
    private String eventsWebhook = "/api/hooks/hub-sync";
    private String sharedSecret = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getEventsChannel() {
        return eventsChannel;
    }

    public void setEventsChannel(String eventsChannel) {
        this.eventsChannel = eventsChannel;
    }

    public String getEventsWebhook() {
        return eventsWebhook;
    }

    public void setEventsWebhook(String eventsWebhook) {
        this.eventsWebhook = eventsWebhook;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
}
