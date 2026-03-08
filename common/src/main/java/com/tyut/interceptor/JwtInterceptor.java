package com.tyut.interceptor;

import com.tyut.constant.AccountConstant;
import com.tyut.context.BaseContext;
import com.tyut.context.UserContext;
import com.tyut.context.WebSocketContext;
import com.tyut.exception.BaseException;
import com.tyut.properties.JwtProperties;
import com.tyut.result.ResultCode;
import com.tyut.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 获取 token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            response.setStatus(ResultCode.UNAUTHORIZED);
            return false;
        }

        try {

            // 使用统一秘钥解析
            Claims claims = JwtUtil.parseToken(
                    token,
                    jwtProperties.getSecretKey()
            );

            // 读取 token 中的数据
            Integer role = claims.get("role", Integer.class);
            Long id = claims.get("id", Long.class);

            if (role == null || id == null) {
                log.error("Token数据不完整");
                response.setStatus(401);
                return false;
            }

            // 写入 ThreadLocal
            UserContext context = new UserContext();
            context.setRole(role);
            context.setId(id);

            BaseContext.set(context);

            WebSocketContext.setUserId(id);
            WebSocketContext.setUserType(role);

            return true;

        } catch (BaseException e) {
            log.error("Token异常: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        BaseContext.remove();
        WebSocketContext.remove();
    }
}
