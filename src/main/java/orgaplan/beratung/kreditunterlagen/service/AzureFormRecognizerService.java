package orgaplan.beratung.kreditunterlagen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import orgaplan.beratung.kreditunterlagen.config.AzureFormRecognizerConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AzureFormRecognizerService {

    private final AzureFormRecognizerConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public AzureFormRecognizerService(AzureFormRecognizerConfig config) {
        this.config = config;
    }

    public JsonNode analyzeDocument(MultipartFile file) throws Exception {
        String url = config.getEndpoint() + "/formrecognizer/documentModels/"
                + config.getModelId() + ":analyze?api-version=2023-07-31";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", config.getKey());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        // Step 1: Gửi file -> nhận 202 Accepted + operation-location
        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        String operationLocation = response.getHeaders().getFirst("operation-location");
        System.out.println("📍 Operation-Location: " + operationLocation);

        // Step 2: Poll kết quả
        int maxRetries = 10;
        int delay = 2000; // 2s
        for (int i = 0; i < maxRetries; i++) {
            ResponseEntity<String> pollResponse =
                    restTemplate.exchange(operationLocation, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode result = mapper.readTree(pollResponse.getBody());
            String status = result.get("status").asText();
            System.out.println("⏳ Polling attempt " + (i+1) + ": " + status);

            if ("succeeded".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status)) {
                return result;
            }

            Thread.sleep(delay);
        }

        throw new RuntimeException("Azure analysis did not complete in time.");
    }
}