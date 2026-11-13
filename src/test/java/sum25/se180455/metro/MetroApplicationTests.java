package sum25.se180455.metro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sum25.se180455.metro.main.MetroApplication;

@SpringBootTest(
		classes = MetroApplication.class,
		properties = {
				"spring.datasource.url=jdbc:h2:mem:metro;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
				"spring.datasource.driver-class-name=org.h2.Driver",
				"spring.datasource.username=sa",
				"spring.datasource.password=",
				"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
				"spring.jpa.hibernate.ddl-auto=none"
		}
)
class MetroApplicationTests {

	@Test
	void contextLoads() {
	}

}
