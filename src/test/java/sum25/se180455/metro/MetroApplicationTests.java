package sum25.se180455.metro;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sum25.se180455.metro.main.MetroApplication;
import sum25.se180455.metro.request.RegistrationRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MetroApplication.class)
@ActiveProfiles("test")
class MetroApplicationTests {
	@Autowired Validator validator;

	@Test
	void contextLoads() {
	}

	@Test
	void validationProviderEnforcesRegistrationConstraints() {
		assertThat(validator.validate(new RegistrationRequest()))
				.extracting(violation -> violation.getPropertyPath().toString())
				.contains("name", "email", "password", "confirmPassword", "passwordMatching", "passwordPolicyValid");
	}

}
