package com.um.core.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public final class JsonExtHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonExtHelper() {
    }

    public static String merge(String existingJson, String key, Object value) {
        Map<String, Object> map = parse(existingJson);
        map.put(key, value);
        return toJson(map);
    }

    public static String getString(String json, String key) {
        Object v = parse(json).get(key);
        return v == null ? null : String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    public static String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
