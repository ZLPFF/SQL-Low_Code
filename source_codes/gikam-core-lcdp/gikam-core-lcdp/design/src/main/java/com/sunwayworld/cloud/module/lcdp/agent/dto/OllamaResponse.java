package com.sunwayworld.cloud.module.lcdp.agent.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OllamaResponse {
    private String model;
    private String response;
    private boolean done;
    private Map<String, Object> context;
}
