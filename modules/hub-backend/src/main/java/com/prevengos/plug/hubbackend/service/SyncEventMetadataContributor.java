package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface SyncEventMetadataContributor {

    JsonNode augment(ObjectMapper objectMapper, JsonNode currentMetadata);
}
