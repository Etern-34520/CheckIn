package indi.etern.checkIn.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 从 request 获取 JWT token
        String token = getTokenFromRequest(request);
//        String token = request.getParameter("token");
        // 校验 token
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            UserDetails userDetails;
            
            // 加载与令 token 关联的用户
            try {
                userDetails = jwtTokenProvider.getUser(token);
                if (!userDetails.isEnabled()) {
                    response.sendRedirect("/checkIn/login/");
                    return;
                }
            } catch (Exception e) {
                response.sendRedirect("/checkIn/login/");
                return;
            }
            
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            
            authenticationToken.setDetails(userDetails);
            request.setAttribute("currentUser",userDetails);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        
        String bearerTokenFromParameter = request.getParameter("token");
        String bearerTokenFromHeader = request.getHeader("Authorization");
        String bearerTokenFromWebSocketHeader = request.getHeader("Sec-WebSocket-Protocol");
        
        if (StringUtils.hasText(bearerTokenFromParameter) && bearerTokenFromParameter.startsWith("Bearer ")) {
            return bearerTokenFromParameter.substring(7);
        } else if (StringUtils.hasText(bearerTokenFromHeader) && bearerTokenFromHeader.startsWith("Bearer ")) {
            return bearerTokenFromHeader.substring(7);
        } else if (StringUtils.hasText(bearerTokenFromWebSocketHeader)) {
            return bearerTokenFromWebSocketHeader;
        } else {
            final Cookie[] cookies = request.getCookies();
            if (cookies != null)
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("token")) {
                        return cookie.getValue();
                    }
                }
        }
        
        return null;
    }
}