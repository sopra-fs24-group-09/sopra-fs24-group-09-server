package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ch.uzh.ifi.hase.soprafs24.interceptor.AuthenticationInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer  {
    @Autowired
    AuthenticationInterceptor authenticationInterceptor;
    //Intercept all requests and decide before sending to controllers by judging whether there is a @UserLoginToken annotation
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**");
    }

}