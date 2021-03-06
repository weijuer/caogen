package com.caogen.config;

import com.caogen.security.MyAccessDecisionManager;
import com.caogen.security.MyAuthenticationProvider;
import com.caogen.security.MyPersistentTokenRepository;
import com.caogen.security.MyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Created by neal on 9/12/16.
 */
@Configuration
@ComponentScan("com.caogen")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@PropertySource("classpath:application.properties")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private MyAuthenticationProvider authenticationProvider;//自定义验证

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private MyPersistentTokenRepository tokenRepository;

    @Autowired
    private MyAccessDecisionManager accessDecisionManager;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                    .frameOptions().sameOrigin().disable()//disable X-Frame-Options
                .authorizeRequests()
//                    .accessDecisionManager(accessDecisionManager)//用注解替换
                    .anyRequest().fullyAuthenticated()//其他url需要鉴权
                .and()
                    .formLogin()
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .loginProcessingUrl("/login")
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .permitAll()
                .and()
                    .logout()
                        .deleteCookies("JSESSIONID")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login")
                .and()
                    .rememberMe()
                        .tokenRepository(tokenRepository)
                        .rememberMeServices(rememberMeServices())
                        .rememberMeParameter("remember-me").key("key")
                        .tokenValiditySeconds(86400)
                .and()
                    .csrf().disable() //disable csrf
                    .sessionManagement().maximumSessions(1);
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        // Key must be equal to rememberMe().key()
        PersistentTokenBasedRememberMeServices rememberMeServices =
                new PersistentTokenBasedRememberMeServices("key", userDetailsService, tokenRepository);
        rememberMeServices.setCookieName("remember-me");
        rememberMeServices.setParameter("remember-me");
        rememberMeServices.setTokenValiditySeconds(864000);
        return rememberMeServices;
    }

}
