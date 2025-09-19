package indi.etern.checkIn.auth;

import indi.etern.checkIn.entities.user.User;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class JwtAuthenticationFilter implements Filter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws ServletException, IOException {
        if (servletRequest instanceof HttpServletRequest request && servletResponse instanceof HttpServletResponse response) {
            TokenData tokenData = getTokenFromRequest(request);
            String token = null;
            if (tokenData != null) {
                token = tokenData.token;
            }
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                User userDetails;
                
                // 加载与令 token 关联的用户
                try {
                    userDetails = jwtTokenProvider.getUser(token);
                    if (!userDetails.isEnabled()) {
                        final Cookie nameCookie = new Cookie("name", userDetails.getUsername());
                        nameCookie.setPath("/checkIn");
                        response.addCookie(nameCookie);
                        final Cookie qqCookie = new Cookie("qq", String.valueOf(userDetails.getQQNumber()));
                        qqCookie.setPath("/checkIn");
                        response.addCookie(qqCookie);
//                    response.sendRedirect("/checkIn/login/");
                        return;
                    }
                } catch (Exception e) {
                    if (tokenData.authenticationMode == TokenData.AuthenticationMode.EXAM) {
                        userDetails = User.ANONYMOUS;
                    } else {
                        return;
                    }
                }
                
                setUserToSecurityContextHolder(userDetails);
                request.setAttribute("currentUser", userDetails);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    
    public static void setUserToSecurityContextHolder(User userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        
        authenticationToken.setDetails(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private record TokenData(String token, AuthenticationMode authenticationMode) {
        enum AuthenticationMode {
            LOGIN, EXAM
        }
    }
    
    private TokenData getTokenFromRequest(HttpServletRequest request) {
        String bearerTokenFromParameter = request.getParameter("token");
        
        if (StringUtils.hasText(bearerTokenFromParameter)) {
            return new TokenData(bearerTokenFromParameter, TokenData.AuthenticationMode.LOGIN);
        } else {
            final String header = request.getHeader("Token");
            if (header != null) {
                return new TokenData(header, TokenData.AuthenticationMode.LOGIN);
            } else {
                final Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    String token = null;
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("token")) {
                            token = cookie.getValue();
                            return new TokenData(token, TokenData.AuthenticationMode.LOGIN);
                        }
                        if (cookie.getName().equals("examToken") && token == null) {
                            token = cookie.getValue();
                        }
                    }
                    if (token != null) {
                        return new TokenData(token, TokenData.AuthenticationMode.EXAM);
                    }
                }
            }
        }
        return null;
    }
}