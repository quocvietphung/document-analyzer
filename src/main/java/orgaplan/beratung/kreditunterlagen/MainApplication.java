package orgaplan.beratung.kreditunterlagen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

@SpringBootApplication
public class MainApplication {

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return new ApplicationRunner() {

			@Override
			public void run(ApplicationArguments args) {
				String serverUrl = "http://localhost:8080";
				Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
				for (RequestMappingInfo info : handlerMethods.keySet()) {
					Set<String> patternSet = info.getPatternsCondition().getPatterns();
					patternSet.forEach(pattern -> {
						System.out.println(serverUrl + pattern);
					});
					System.out.println(info.getMethodsCondition().getMethods());
				}
			}
		};
	}
}
