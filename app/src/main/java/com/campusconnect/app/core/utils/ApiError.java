package com.campusconnect.app.core.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import retrofit2.Response;

/**
 * Pulls a human-readable message out of a failed API response instead of
 * showing a generic "something went wrong" Toast. Handles both response
 * shapes used across the backend:
 *   - {"error": "message"}                (LoginView, plain string errors)
 *   - {"field": ["message", ...]}         (DRF serializer validation errors)
 */
public final class ApiError {

    private ApiError() {}

    /**
     * @param fieldOrder preferred field names to check first (e.g. "email"
     *                   before "password"), for when a request could fail on
     *                   more than one field at once. Any remaining field is
     *                   used as a fallback if none of these are present.
     */
    public static String extract(Response<?> response, String fallback, String... fieldOrder) {
        try {
            if (response.errorBody() == null) return fallback;
            JSONObject json = new JSONObject(response.errorBody().string());

            if (json.has("error")) {
                Object error = json.get("error");
                if (error instanceof String) return (String) error;
            }

            for (String field : fieldOrder) {
                String message = firstMessage(json, field);
                if (message != null) return message;
            }

            Iterator<String> keys = json.keys();
            if (keys.hasNext()) {
                String message = firstMessage(json, keys.next());
                if (message != null) return message;
            }
        } catch (Exception ignored) {
            // malformed/unexpected body — fall through to the generic message
        }
        return fallback;
    }

    private static String firstMessage(JSONObject json, String field) throws JSONException {
        if (!json.has(field)) return null;
        Object value = json.get(field);
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            return array.length() > 0 ? array.getString(0) : null;
        }
        if (value instanceof String) return (String) value;
        return null;
    }
}
