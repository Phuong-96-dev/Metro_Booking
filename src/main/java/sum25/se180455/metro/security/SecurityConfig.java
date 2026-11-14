package sum25.se180455.metro.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import sum25.se180455.metro.repository.MemberRepository;

@Configuration
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    @Conditional(GoogleConfiguredCondition.class)
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.client-secret}") String secret,
            @Value("${app.google.redirect-uri}") String redirect,
            @Value("${app.google.link-redirect-uri}") String linkRedirect) {
        ClientRegistration login = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId).clientSecret(secret).redirectUri(redirect).scope("openid", "profile", "email").build();
        ClientRegistration link = ClientRegistration.withClientRegistration(login)
                .registrationId("google-link").redirectUri(linkRedirect).build();
        return new InMemoryClientRegistrationRepository(login, link);
    }

    @Bean public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            request.getSession().removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            request.getSession().removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(request.getContextPath() + (admin ? "/admindashboard" : "/"));
        };
    }

    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http, MemberAuthenticationProvider provider,
            MemberRepository members, GoogleOidcUserService oidcService,
            ObjectProvider<ClientRegistrationRepository> registrations, AuthenticationSuccessHandler success) throws Exception {
        http.authenticationProvider(provider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/authorization/google-link").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/chat/message", "/api/chat/reset").permitAll()
                .requestMatchers(HttpMethod.GET, "/chat/**").permitAll()
                .requestMatchers("/scanner", "/api/scan", "/api/scan/**", "/api/users/**").hasRole("ADMIN")
                .requestMatchers("/admin/student-verifications", "/admin/student-verifications/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/tickets/**", "/api/tickets/**").authenticated()
                .requestMatchers("/adminManage/**", "/admindashboard", "/train-schedules/add", "/train-schedules/edit/**", "/train-schedules/delete/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/train-schedules", "/bus-routes/*/destinations/**").hasRole("ADMIN")
                .requestMatchers("/profile", "/update-profile", "/myTicket", "/payment", "/payment/**", "/ticket/buy", "/ticket/use", "/feedback/send", "/account/**",
                        "/student-verification", "/student-verification/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/", "/login", "/register", "/more", "/blog", "/ticket", "/ticket/search",
                        "/feedback", "/feedback/all", "/trains", "/trains/*", "/train-stations", "/train-stations/*",
                        "/train-schedules", "/bus-routes", "/bus-routes/*", "/search-schedules", "/error",
                        "/oauth2/authorization/google", "/login/oauth2/code/*", "/images/**", "/common/**", "/train/**", "/member/**", "/admin/**", "/bus/**", "/style.css", "/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/register", "/login", "/search-schedules", "/bus-routes/search").permitAll()
                .anyRequest().denyAll())
            .formLogin(form -> form.loginPage("/login").usernameParameter("email").successHandler(success).failureUrl("/login?error").permitAll())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"))
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
            .addFilterBefore(new AuthRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new MemberSessionFilter(members), AnonymousAuthenticationFilter.class);
        ClientRegistrationRepository clients = registrations.getIfAvailable();
        if (clients != null) {
            DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(clients, "/oauth2/authorization");
            resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
            http.oauth2Login(oauth -> oauth.loginPage("/login")
                    .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(resolver))
                    .userInfoEndpoint(info -> info.oidcUserService(oidcService))
                    .successHandler(success)
                    .failureHandler((request, response, exception) -> {
                        if (request.getSession(false) != null) {
                            request.getSession(false).removeAttribute(GoogleOidcUserService.LINK_MEMBER);
                            request.getSession(false).removeAttribute(GoogleOidcUserService.LINK_EXPIRES);
                        }
                        String error = exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthError
                                && "link_required".equals(oauthError.getError().getErrorCode()) ? "link_required" : "failed";
                        response.sendRedirect(request.getContextPath() + "/login?google=" + error);
                    }));
        }
        return http.build();
    }
}
