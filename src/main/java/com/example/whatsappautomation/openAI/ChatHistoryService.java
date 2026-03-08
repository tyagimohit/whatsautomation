package com.example.whatsappautomation.openAI;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatHistoryService {

    // Store history per WhatsApp number (use Redis for production)
    private final Map<String, List<Map<String, Object>>> userHistories = new ConcurrentHashMap<>();

    public List<Map<String, Object>> getHistory(String phoneNumber) {
        return userHistories.getOrDefault(phoneNumber, new ArrayList<>());
    }

    public void addMessage(String phoneNumber, String role, String text) {
        userHistories.computeIfAbsent(phoneNumber, k -> new ArrayList<>())
                .add(Map.of("role", role, "parts", List.of(Map.of("text", text))));

        // Keep last 10 exchanges to avoid token limits
        List<Map<String, Object>> history = userHistories.get(phoneNumber);
        if (history.size() > 20) {
            history.subList(0, history.size() - 20).clear();
        }
    }
}