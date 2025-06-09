package indi.etern.checkIn.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        final String contentType = httpServletRequest.getContentType();
        if (contentType.contains("application/json")) {
            httpServletResponse.setContentType("application/json");
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "{\"type\":\"error\", \"message\":\"Access denied\"}");
        } else if (contentType.contains("text/html")) {
            httpServletResponse.sendRedirect("/login/");
        }
    }
}

