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

@Service
public class OpenAIService {

    @Value("${openai.gemini-key}")
    private String apiKey;

    @Value("${openai.gemini-url}")
    private String apiUrl;

    public String askGemini(String prompt) {

        RestTemplate restTemplate = new RestTemplate();

        String context = "You act as a professional singer/artist and respond in one line only approx 8 to 10 words.";
        prompt=context+prompt;
        String requestBody = """
        {
          "contents":[
            {
              "parts":[
                {
                  "text":"%s"
                }
              ]
            }
          ]
        }
        """.formatted(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

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