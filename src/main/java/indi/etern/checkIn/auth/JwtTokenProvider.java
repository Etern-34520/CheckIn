package indi.etern.checkIn.auth;

import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    public static JwtTokenProvider singletonInstance;
    
    @Value("${jwt-secret}")
    private String jwtSecret;

    @Value("${jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    private final UserService userService;
    
    final RoleService roleService;

    public JwtTokenProvider(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
        singletonInstance = this;
    }

    // 生成 JWT token
    public String generateToken(User user) {

        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        String token = Jwts.builder()
                .setSubject(user.getName() + ":" + user.getQQNumber())
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
        return token;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    // 从 Jwt token 获取用户
//    @Cacheable(value = "token", key = "#token")
    public User getUser(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String usernameAndQQ = claims.getSubject();
        return userService.findByQQNumber(Long.parseLong(usernameAndQQ.split(":")[1])).orElseThrow();
    }

    // 验证 Jwt token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
    
    /*public void invalidateToken(String token) {
        //noinspection unchecked
        Jwt<?, Map<String,String>> jwt = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parse(token);
        jwt.getBody().get("exp");
    }*/

    public boolean currentUserHasPermission(String permissionName) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = (User) authentication.getPrincipal();
        final Role role = roleService.findByType(user.getRole().getType()).orElseThrow();
        for (Permission permission : role.getPermissions()) {
            if (permission.getName().equals(permissionName)/*&&authority.getPermissionType().equals(permissionType)*/) {
                return true;
            }
        }
        return false;
    }
}
