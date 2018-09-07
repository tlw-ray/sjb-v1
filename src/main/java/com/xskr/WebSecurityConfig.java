package com.xskr;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and().formLogin().and().httpBasic()
            .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login");
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withDefaultPasswordEncoder().username("dss").password("dss").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("wcy").password("wcy").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("wln").password("wln").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("twt").password("twt").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("yy").password("yy").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("yl").password("yl").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("wyh").password("wyh").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("ch").password("ch").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("tlw").password("tlw").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("wc").password("wc").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("wx").password("wx").roles("USER").build(),
                User.withDefaultPasswordEncoder().username("ljn").password("ljn").roles("USER").build()
        );
    }
}