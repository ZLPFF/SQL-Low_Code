package com.sunwayworld.cloud.module.lcdp.agent.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class OllamaRequest {
    private String model;
    private List<Map<String, String>> messages;
    private boolean stream;
    private Options options;

    @Data
    public static class Options {
        private Integer num_predict;
        private Double temperature;
    }
}
