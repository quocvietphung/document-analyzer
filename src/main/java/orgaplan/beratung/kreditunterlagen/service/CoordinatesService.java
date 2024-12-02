package orgaplan.beratung.kreditunterlagen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class CoordinatesService {

    private static final Logger logger = LoggerFactory.getLogger(CoordinatesService.class);
    private final WebClient webClient;

    public CoordinatesService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String fetchCoordinates(String streetNumber, String postalCode, String city, String country) {
        if (streetNumber == null || postalCode == null || city == null || country == null) {
            logger.warn("Cannot fetch coordinates due to missing address information.");
            return null;
        }

        String addressQuery = String.format("%s, %s, %s, %s", streetNumber, postalCode, city, country);

        String url = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", addressQuery)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .toUriString();

        logger.info("Fetching coordinates for address: {}", addressQuery);
        logger.info("Constructed URL: {}", url);

        try {
            List<Map<String, Object>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            logger.info("Response from OSM: {}", response);

            if (response != null && !response.isEmpty()) {
                Map<String, Object> locationData = response.get(0);
                String latitude = (String) locationData.get("lat");
                String longitude = (String) locationData.get("lon");

                logger.info("Extracted latitude: {}", latitude);
                logger.info("Extracted longitude: {}", longitude);

                if (latitude != null && longitude != null) {
                    return latitude + ", " + longitude;
                } else {
                    logger.warn("No latitude/longitude found in the response.");
                    return null;
                }
            } else {
                logger.warn("No coordinates found for the given address: {}", addressQuery);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching coordinates from OSM: ", e);
            return null;
        }
    }
}