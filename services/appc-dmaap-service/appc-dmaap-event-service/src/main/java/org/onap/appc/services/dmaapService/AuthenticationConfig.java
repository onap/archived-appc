package org.onap.appc.services.dmaapService;

import java.util.Properties;

import org.onap.appc.configuration.ConfigurationFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfig extends WebSecurityConfigurerAdapter {
    
    private final String PROPERTIES_PREFIX = "appc.srvcomm.messaging";
    private final String DEFAULT_USER = "appc";
    private final String DEFAULT_PASSWORD = "onapappc";
    
 //Uncomment to require https
 /*   
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        org.onap.appc.configuration.Configuration configuration = ConfigurationFactory.getConfiguration();
        Properties props = configuration.getProperties();
        String user = props.getProperty(PROPERTIES_PREFIX + ".user");
        String pass = props.getProperty(PROPERTIES_PREFIX + ".pass");
        if(user == null) {
            user = DEFAULT_USER;
        }
        if(pass == null) {
            pass = DEFAULT_PASSWORD;
        }
        auth.inMemoryAuthentication().withUser(user).password(encoder().encode(pass)).roles("USER");
    }
    
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
    
    */

}
