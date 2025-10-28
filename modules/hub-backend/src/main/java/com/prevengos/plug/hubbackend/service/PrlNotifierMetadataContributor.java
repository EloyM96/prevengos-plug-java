package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.prevengos.plug.hubbackend.config.PrlNotifierProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PrlNotifierMetadataContributor implements SyncEventMetadataContributor {

    private final PrlNotifierProperties properties;

    public PrlNotifierMetadataContributor(PrlNotifierProperties properties) {
        this.properties = properties;
    }

    @Override
    public JsonNode augment(ObjectMapper objectMapper, JsonNode currentMetadata) {
        if (!properties.isEnabled()) {
            return currentMetadata != null ? currentMetadata : objectMapper.nullNode();
        }

        ObjectNode enriched = objectMapper.createObjectNode();
        if (currentMetadata != null && !currentMetadata.isNull() && !currentMetadata.isMissingNode()) {
            if (currentMetadata.isObject()) {
                enriched.setAll((ObjectNode) currentMetadata);
            } else {
                enriched.set("original", currentMetadata);
            }
        }

        if (StringUtils.hasText(properties.getEventsChannel())) {
            enriched.put("channel", properties.getEventsChannel());
        }
        if (StringUtils.hasText(properties.getEventsWebhook())) {
            enriched.put("webhook", properties.getEventsWebhook());
        }
        enriched.put("webhook_secret_set", StringUtils.hasText(properties.getSharedSecret()));
        return enriched;
    }
}
