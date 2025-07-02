package sum25.se180455.metro.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "sum25.SE180455.metro.pojo")
@ComponentScan(basePackages = {"sum25.se180455.metro.controller", "sum25.se180455.metro.service", "sum25.se180455.metro.repository"})
@EnableJpaRepositories(basePackages = "sum25.se180455.metro.repository")

public class MetroApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetroApplication.class, args);
	}

}
