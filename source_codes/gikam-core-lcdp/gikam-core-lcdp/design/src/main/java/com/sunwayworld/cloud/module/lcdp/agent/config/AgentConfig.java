package com.sunwayworld.cloud.module.lcdp.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lcdp.agent")
public class AgentConfig {
    
    private boolean enabled = true;
    private String model = "qwen2.5:7b";
    private String ollamaBaseUrl = "http://localhost:11434";
    private int maxTokens = 4096;
    private double temperature = 0.7;
    private int timeoutSeconds = 120;
    
    private WorkspaceConfig workspace = new WorkspaceConfig();
    private CheckoutConfig checkout = new CheckoutConfig();

    public static class WorkspaceConfig {
        private String basePath = "/tmp/lcdp-workspace";
        public String getBasePath() { return basePath; }
        public void setBasePath(String basePath) { this.basePath = basePath; }
    }

    public static class CheckoutConfig {
        private boolean autoSubmit = false;
        private int defaultTimeout = 300;
        
        public boolean isAutoSubmit() { return autoSubmit; }
        public void setAutoSubmit(boolean autoSubmit) { this.autoSubmit = autoSubmit; }
        public int getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(int defaultTimeout) { this.defaultTimeout = defaultTimeout; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getOllamaBaseUrl() { return ollamaBaseUrl; }
    public void setOllamaBaseUrl(String ollamaBaseUrl) { this.ollamaBaseUrl = ollamaBaseUrl; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public WorkspaceConfig getWorkspace() { return workspace; }
    public void setWorkspace(WorkspaceConfig workspace) { this.workspace = workspace; }
    public CheckoutConfig getCheckout() { return checkout; }
    public void setCheckout(CheckoutConfig checkout) { this.checkout = checkout; }
}
