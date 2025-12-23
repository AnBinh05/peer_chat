package com.p2p.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.p2p.model.Message;

/**
 * Utility class để serialize/deserialize JSON
 * Dùng cho Message / File / Group / Signal
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    // ================= MESSAGE =================

    public static String toJson(Message message) {
        try {
            return gson.toJson(message);
        } catch (Exception e) {
            System.err.println("JSON serialize error:");
            e.printStackTrace();
            return "{}";
        }
    }

    public static Message fromJson(String json) {
        try {
            return gson.fromJson(json, Message.class);
        } catch (Exception e) {
            System.err.println("JSON parse error (Message):");
            System.err.println(json);
            e.printStackTrace();
            return null;
        }
    }

    // ================= GENERIC =================

    public static String toJsonObject(Object obj) {
        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            System.err.println("JSON serialize error:");
            e.printStackTrace();
            return "{}";
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            System.err.println("JSON parse error (" + clazz.getSimpleName() + ")");
            e.printStackTrace();
            return null;
        }
    }
}
