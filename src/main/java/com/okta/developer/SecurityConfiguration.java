package com.okta.developer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final String clientSecret;
    private final String clientId;
    private final String issuerUri;

    @Autowired
    public SecurityConfiguration(@Value("${okta.issuer-uri}") String issuerUri,
                                 @Value("${okta.client-id}") String clientId,
                                 @Value("${okta.client-secret}") String clientSecret) {
        this.issuerUri = issuerUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .and()
            .csrf()
                .csrfTokenRepository(new CookieCsrfTokenRepository())
                .and()
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .oauth2Login();
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration okta = getRegistration();
        return new InMemoryClientRegistrationRepository(okta);
    }

    private ClientRegistration getRegistration() {
        return ClientRegistrations.fromOidcIssuerLocation(this.issuerUri)
                .registrationId("okta")
                .clientId(this.clientId)
                .clientSecret(this.clientSecret)
                .build();
    }


}