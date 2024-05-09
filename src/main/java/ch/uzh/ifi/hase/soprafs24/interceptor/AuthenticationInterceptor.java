package ch.uzh.ifi.hase.soprafs24.interceptor;

import ch.uzh.ifi.hase.soprafs24.annotation.UserLoginToken;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.socket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        // 标准化提取Token逻辑
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 移除 "Bearer " 前缀
        } else {
            token = null;
        }

        // 针对普通HTTP请求的拦截
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            if (method.isAnnotationPresent(UserLoginToken.class)) {
                UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
                if (userLoginToken.required() && (token == null || !userService.findByToken(token))) {
                    String tokenNullMessage = "Please log in with correct credentials. Not AUTHORIZED.";
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, tokenNullMessage);
                }
            }
        }
//         WebSocket的握手请求也可能通过这个拦截器
        else if (handler instanceof WebSocketHandler) {
            if (token == null || !userService.findByToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 设置HTTP状态码
                response.getWriter().write("Not AUTHORIZED");
                return false;
            }
        }

        return true;
    }

}