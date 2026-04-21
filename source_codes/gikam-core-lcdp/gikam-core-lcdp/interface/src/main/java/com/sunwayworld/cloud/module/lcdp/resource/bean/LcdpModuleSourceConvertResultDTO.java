package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LcdpModuleSourceConvertResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long moduleId;
    private String moduleName;
    private String targetModule;
    private String outputRoot;
    private Boolean overwrite;
    private String reportFile;
    private List<String> generatedFiles = new ArrayList<>();
    private List<String> convertedResources = new ArrayList<>();
    private List<String> skippedResources = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTargetModule() {
        return targetModule;
    }

    public void setTargetModule(String targetModule) {
        this.targetModule = targetModule;
    }

    public String getOutputRoot() {
        return outputRoot;
    }

    public void setOutputRoot(String outputRoot) {
        this.outputRoot = outputRoot;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public List<String> getGeneratedFiles() {
        return generatedFiles;
    }

    public void setGeneratedFiles(List<String> generatedFiles) {
        this.generatedFiles = generatedFiles;
    }

    public List<String> getConvertedResources() {
        return convertedResources;
    }

    public void setConvertedResources(List<String> convertedResources) {
        this.convertedResources = convertedResources;
    }

    public List<String> getSkippedResources() {
        return skippedResources;
    }

    public void setSkippedResources(List<String> skippedResources) {
        this.skippedResources = skippedResources;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
