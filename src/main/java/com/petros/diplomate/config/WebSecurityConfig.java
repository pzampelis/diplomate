package com.petros.diplomate.config;

import com.petros.diplomate.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public WebSecurityConfig(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/api").authenticated()
                .antMatchers("/api/admin", "/api/admin/**").hasAuthority("ADMIN_ROLE")
                .antMatchers("/api/professor/{id}/**")
                .access("hasAuthority('PROFESSOR_ROLE') and @guard.checkUserId(authentication, #id)")
                .antMatchers("/api/student/{id}/**")
                .access("hasAuthority('STUDENT_ROLE') and @guard.checkUserId(authentication, #id)")
                .antMatchers("/api/login", "/api/register").permitAll()
                .and()
                .formLogin().loginPage("/api/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/api/redirect-home", true)
                .permitAll()
                .and()
                .logout().logoutSuccessUrl("/api/login").permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(userService);

        return provider;
    }

}
