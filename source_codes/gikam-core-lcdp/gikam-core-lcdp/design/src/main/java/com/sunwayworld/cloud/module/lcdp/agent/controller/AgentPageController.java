package com.sunwayworld.cloud.module.lcdp.agent.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
public class AgentPageController {

    @Value("${lcdp.agent.api-base:/open/core/module/lcdp/agent}")
    private String apiBasePath;

    @GetMapping("/agent/chat")
    public ResponseEntity<String> chatPage() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/agent/agent-chat.html");
        String content = Files.readString(resource.getFile().toPath());
        content = content.replace("${API_BASE}", apiBasePath);
        return ResponseEntity.ok(content);
    }
}
