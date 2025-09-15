package ai.document.analyzer.config;

import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class DateFormatConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.simpleDateFormat("dd-MM-yyyy");
            builder.serializers(new DateSerializer(false, new SimpleDateFormat("dd-MM-yyyy")));
        };
    }
}

