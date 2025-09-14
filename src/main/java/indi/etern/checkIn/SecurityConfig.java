package indi.etern.checkIn;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.auth.JwtAuthenticationFilter;
import indi.etern.checkIn.auth.LogoutHandler;
import indi.etern.checkIn.service.dao.UserService;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    final UserService userService;
    final LogoutHandler logoutHandler;
    AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper;

    @Value("${oauth2.redirectUriTemplatePrefix}")
    protected String oauth2CallbackBaseUri;

    public SecurityConfig(UserService userService, LogoutHandler logoutHandler, ObjectMapper objectMapper) {
        this.userService = userService;
        this.logoutHandler = logoutHandler;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers("/manage/**").authenticated()
                                .requestMatchers("/api/websocket/**").authenticated()
                                .requestMatchers("/api/qualify").authenticated()
                                .anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(), BasicAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/api/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri(oauth2CallbackBaseUri)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(userService)
                        )
                        // 重要：配置认证成功和失败的处理器，返回JSON
                        .successHandler(oauth2AuthenticationSuccessHandler())
                        .failureHandler(oauth2AuthenticationFailureHandler())
                )
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> response.sendRedirect("/checkIn/login/"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.sendRedirect("/checkIn/login/"))
                ).logout((logout) -> {
                    logout.addLogoutHandler(logoutHandler);
                });
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        if (authenticationManager != null) {
            return authenticationManager;
        }
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationManager = new ProviderManager(authenticationProvider);
        return authenticationManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return ENCODER;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(ENCODER);
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/checkIn/manage");
        };
    }

    public AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            String exceptionMessage = exception.getMessage();
            String message;
            if (exception instanceof OAuth2AuthenticationException e) {
                message = exceptionMessage != null ? exceptionMessage : e.getError().getErrorCode();
            } else {
                message = exceptionMessage;
            }
            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
            Cookie errorMessage = new Cookie("OAuth2ErrorMessage", encoded);
            errorMessage.setPath("/checkIn");
            response.addCookie(errorMessage);
            response.sendRedirect("/checkIn/oauth2/error");
        };
    }
}