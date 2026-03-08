package com.example.whatsappautomation.openAI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.gemini-key}")
    private String apiKey;

    @Value("${openai.gemini-url}")
    private String apiUrl;

    public String askGemini(String prompt,  List<Map<String, Object>> chatHistory) {
        System.out.println("----user entered prompt----->>"+prompt);

        RestTemplate restTemplate = new RestTemplate();
        List<Map<String, Object>> contents = new ArrayList<>();

        // Inject persona as first user turn (Gemini doesn't have a system role)
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", "Act as a professional singer"))
        ));
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text", "Understood! I am Aria, your professional artist guide. How can I help you today? 🎨"))
        ));

        // Add chat history for context
        contents.addAll(chatHistory);

        // Add current user message
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        Map<String, Object> entity = Map.of("contents", contents);

        int retries = 3;

        for (int i = 0; i < retries; i++) {
            try {
                ResponseEntity<String> response =
                        restTemplate.postForEntity(apiUrl, entity, String.class);

                String responseText = extractText(response.getBody());
                System.out.println("----exact-response----->>"+responseText);
                return responseText;
            } catch (HttpServerErrorException.ServiceUnavailable ex) {
                System.out.println("Gemini busy. Retrying...");
                try {
                    Thread.sleep(2000); // wait 2 seconds
                } catch (InterruptedException ignored) {}

            }
        }

        return "Gemini API busy. Please try again later.";
    }

    public String getChatResponse(String prompt) {
        String url = apiUrl + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        String body = String.format("""
{
  "contents":[
    {"parts":[{"text":"%s"}]}
  ]
}
""", prompt);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, entity, String.class);


        String responseText = extractText(response.getBody());
        System.out.println("----exact-response----->>"+responseText);
        return responseText;

    }

    private String extractText(String jsonResponse) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}