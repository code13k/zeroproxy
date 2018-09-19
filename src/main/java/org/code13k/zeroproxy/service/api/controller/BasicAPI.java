package org.code13k.zeroproxy.service.api.controller;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicAPI {
    private static String RESULT_JSON_KEY = "data";

    protected String toResultJsonString(Map result) {
        if (result != null) {
            Map<String, Object> jsonResult = new HashMap<>();
            jsonResult.put(RESULT_JSON_KEY, result);
            return new GsonBuilder().create().toJson(jsonResult);
        }
        return null;
    }

    protected String toResultJsonString(List result) {
        if (result != null) {
            Map<String, Object> jsonResult = new HashMap<>();
            jsonResult.put(RESULT_JSON_KEY, result);
            return new GsonBuilder().create().toJson(jsonResult);
        }
        return null;
    }

    protected String toResultJsonString(String result) {
        if (StringUtils.isBlank(result) == false) {
            Map<String, Object> jsonResult = new HashMap<>();
            jsonResult.put(RESULT_JSON_KEY, result);
            return new GsonBuilder().create().toJson(jsonResult);
        }
        return null;
    }

    protected String toResultJsonString(int result) {
        Map<String, Object> jsonResult = new HashMap<>();
        jsonResult.put(RESULT_JSON_KEY, result);
        return new GsonBuilder().create().toJson(jsonResult);
    }

    protected String toResultJsonString(boolean result) {
        Map<String, Object> jsonResult = new HashMap<>();
        jsonResult.put(RESULT_JSON_KEY, result);
        return new GsonBuilder().create().toJson(jsonResult);
    }
}
