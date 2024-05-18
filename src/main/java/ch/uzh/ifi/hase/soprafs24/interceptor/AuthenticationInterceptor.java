package ch.uzh.ifi.hase.soprafs24.interceptor;

import ch.uzh.ifi.hase.soprafs24.annotation.UserLoginToken;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;

    private static final String MOCK_TOKEN = "mockToken";
    
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        String token = httpServletRequest.getHeader("Authorization");
        // Get token from http request header.
//        schema of Authorization header looks like this: Bearer <token>,
//        therefore we need to remove the "Bearer " part. What remains, which is token itself.
        // if there is no Annotation
        if(!(object instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod)object;
        Method method=handlerMethod.getMethod();

        //Check if Annotated with @UserLoginToken
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // peform simple token based authentication
                if (token == null) {
                    String tokenNullMessage = "Please log in with correct credentials. Not AUTHORIZED.";
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format(tokenNullMessage));
                }
                token=token.substring(7);

                // Check for the mock token
                if (MOCK_TOKEN.equals(token)) {
                    return true;
                }

                if (!userService.findByToken(token)) {
                    String tokenNullMessage = "Please log in with correct credentials. Not AUTHORIZED.";
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format(tokenNullMessage));
                }

                return true;
            }
        }
        return true;
    }

}