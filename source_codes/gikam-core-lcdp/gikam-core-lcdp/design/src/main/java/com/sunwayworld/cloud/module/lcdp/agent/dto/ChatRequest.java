package com.sunwayworld.cloud.module.lcdp.agent.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
    private String userId;
    private String userName;
}
