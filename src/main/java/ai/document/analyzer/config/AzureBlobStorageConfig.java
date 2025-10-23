package ai.document.analyzer.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobStorageConfig {

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.account-key}")
    private String accountKey;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        if (accountName == null || accountName.isEmpty() || accountKey == null || accountKey.isEmpty()) {
            return null; // Return null if not configured (for local dev without Azure)
        }
        
        String connectionString = String.format(
            "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
            accountName, accountKey
        );
        
        return new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }

    public String getContainerName() {
        return containerName;
    }

    public String getAccountName() {
        return accountName;
    }
}
