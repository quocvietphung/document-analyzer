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
		return args -> {
			String serverUrl = "http://localhost:8080";

			printEndpointsForController("UserController", serverUrl);
			printEndpointsForController("DocumentController", serverUrl);
		};
	}

	private void printEndpointsForController(String controllerName, String serverUrl) {
		System.out.println(controllerName + " Endpoints:");
		Map<RequestMappingInfo, HandlerMethod> controllerMethods = requestMappingHandlerMapping.getHandlerMethods();
		for (RequestMappingInfo info : controllerMethods.keySet()) {
			Set<String> patterns = info.getPatternsCondition().getPatterns();
			patterns.forEach(pattern -> {
				System.out.println(serverUrl + pattern);
			});
			System.out.println(info.getMethodsCondition().getMethods());
		}
	}
}
