package cz.cvut.fel.poustka.daniel.flashcards_backend.config;

import cz.cvut.fel.poustka.daniel.flashcards_backend.security.CredentialsAuthenticationFailureHandler;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.CredentialsAuthenticationSuccessHandler;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)// Allow methods to be secured using annotation
public class SecurityConfig extends WebSecurityConfigurerAdapter
{

    private static final String[] COOKIES_TO_DESTROY = {
            SecurityConstants.SESSION_COOKIE_NAME,
            SecurityConstants.REMEMBER_ME_COOKIE_NAME
    };

    private final CredentialsAuthenticationFailureHandler credentialsAuthenticationFailureHandlerHandler;
    private final CredentialsAuthenticationSuccessHandler credentialsAuthenticationSuccessHandlerHandler;
    private final LogoutSuccessHandler credentialsLogoutSuccessHandler;
    private final AuthenticationProvider authenticationProvider;

    private final UserDetailsServiceImpl userDetailsService;

    @Value("${flashcards.fe.url}")
    private String flashcardsUrl;

    @Autowired
    public SecurityConfig(CredentialsAuthenticationFailureHandler credentialsAuthenticationFailureHandlerHandler,
                          CredentialsAuthenticationSuccessHandler credentialsAuthenticationSuccessHandlerHandler,
                          LogoutSuccessHandler credentialsLogoutSuccessHandler,
                          AuthenticationProvider authenticationProvider,
                          UserDetailsServiceImpl userDetailsService)
    {
        this.credentialsAuthenticationFailureHandlerHandler = credentialsAuthenticationFailureHandlerHandler;
        this.credentialsAuthenticationSuccessHandlerHandler = credentialsAuthenticationSuccessHandlerHandler;
        this.credentialsLogoutSuccessHandler = credentialsLogoutSuccessHandler;
        this.authenticationProvider = authenticationProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
    {
        auth.authenticationProvider(authenticationProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
        configuration.setAllowedOrigins(List.of(flashcardsUrl));
        configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the
        // request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()  /*.requiresChannel().requiresSecure();*/
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))

                .and()
                .headers()
                .frameOptions().sameOrigin()
                .and()

                .authenticationProvider(authenticationProvider)
                .cors().and()
                .csrf().disable()

                .formLogin()
                .successHandler(credentialsAuthenticationSuccessHandlerHandler)
                .failureHandler(credentialsAuthenticationFailureHandlerHandler)
                .loginProcessingUrl(SecurityConstants.SECURITY_CHECK_URI)
                .usernameParameter(SecurityConstants.EMAIL_PARAM)
                .passwordParameter(SecurityConstants.PASSWORD_PARAM)
                .and()

                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(SecurityConstants.LOGOUT_URI))
                .logoutSuccessHandler(credentialsLogoutSuccessHandler)
                .deleteCookies(COOKIES_TO_DESTROY)
                .invalidateHttpSession(true)


                .and()
                .rememberMe().userDetailsService(this.userDetailsService)
                .rememberMeCookieName(SecurityConstants.REMEMBER_ME_COOKIE_NAME)
                .useSecureCookie(true)
                .rememberMeParameter("remember")
                .key("uniqueAndSecret")
                .tokenValiditySeconds(SecurityConstants.REMEMBER_TIMEOUT)
                /*.rememberMeCookieDomain(SecurityConstants.COOKIE_URI)*/

                .and()
                .sessionManagement()
                .maximumSessions(1);
    }
}
