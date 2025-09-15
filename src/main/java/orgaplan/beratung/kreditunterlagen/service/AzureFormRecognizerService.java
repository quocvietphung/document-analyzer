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
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Azure yêu cầu application/octet-stream

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return mapper.readTree(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // In chi tiết lỗi từ Azure
            System.err.println("Azure Form Recognizer error: " + ex.getResponseBodyAsString());
            throw ex;
        }
    }
}