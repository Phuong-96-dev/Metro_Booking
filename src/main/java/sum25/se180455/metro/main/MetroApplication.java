package sum25.se180455.metro.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "sum25.se180455.metro")
@EntityScan(basePackages = "sum25.se180455.metro.pojo")
@EnableJpaRepositories(basePackages = "sum25.se180455.metro.repository")

public class MetroApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetroApplication.class, args);
	}

}
