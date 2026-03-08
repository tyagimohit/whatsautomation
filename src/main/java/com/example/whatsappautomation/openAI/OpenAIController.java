package com.example.whatsappautomation.openAI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class OpenAIController {

    @Autowired
    private OpenAIService openAIService;

    @GetMapping("/ask")
    public String askAI(@RequestParam String prompt) {

        List<Map<String, Object>> list = new ArrayList<>();
        return openAIService.askGemini(prompt, list);
    }

}
