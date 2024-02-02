package indi.etern.checkIn;

import indi.etern.checkIn.auth.JwtAuthenticationFilter;
import indi.etern.checkIn.auth.LogoutHandler;
import indi.etern.checkIn.service.dao.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class SecurityConfig {
    public static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    final UserService userService;
    final LogoutHandler logoutHandler;
    AuthenticationManager authenticationManager;
    
    public SecurityConfig(UserService userService, LogoutHandler logoutHandler) {
        this.userService = userService;
        this.logoutHandler = logoutHandler;
    }
    
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers("/manage/**").authenticated()
//                                        .requestMatchers("/manage/page/").hasAnyAuthority("editing others question")
//                                        .requestMatchers("/manage/css/**").authenticated()
//                                        .requestMatchers("/manage/js/**").authenticated()
//                                        .requestMatchers("/api/websocket/**").authenticated()
                                .anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(jwtAuthenticationFilter())
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> response.sendRedirect("/checkIn/login/"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.sendRedirect("/checkIn/login/"))
                ).logout((logout) -> {
//                    logout.addLogoutHandler(logoutHandler);
                });
        return http.build();
    }
    
    //    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        if (authenticationManager != null) {
            return authenticationManager;
        }
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationManager = new ProviderManager(authenticationProvider);
        return authenticationManager;
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return ENCODER;
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(new UserService.CustomPasswordEncoder());
    }
    
    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        if (authenticationManager == null) {
            authenticationManager = authenticationManager(userDetailsService(), passwordEncoder());
        }
        return new JwtAuthenticationFilter(authenticationManager);
    }
}
