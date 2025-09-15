package ai.document.analyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureFormRecognizerConfig {

    @Value("${azure.document.endpoint}")
    private String endpoint;

    @Value("${azure.document.key}")
    private String key;

    @Value("${azure.document.model-id}")
    private String modelId;

    public String getEndpoint() { return endpoint; }
    public String getKey() { return key; }
    public String getModelId() { return modelId; }
}