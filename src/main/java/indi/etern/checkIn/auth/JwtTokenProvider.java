package indi.etern.checkIn.auth;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.entities.user.Permission;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.RobotTokenService;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.dao.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.NoSuchElementException;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    public static JwtTokenProvider singletonInstance;
    private final RobotTokenService robotTokenService;
    
    /*@Value("${jwt-secret}")*/
    private String jwtSecret;

    @Value("${jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    private final UserService userService;
    
    final RoleService roleService;

    public JwtTokenProvider(UserService userService, RoleService roleService, RobotTokenService robotTokenService, SettingService settingService) {
        this.userService = userService;
        this.roleService = roleService;
        singletonInstance = this;
        this.robotTokenService = robotTokenService;
        try {
            final SettingItem item = settingService.getItem("other", "jwtSecret");
            jwtSecret = item.getValue(String.class);
        } catch (NoSuchElementException e) {
            logger.info("setting \"other.jwtRawSecret\" not found, now generating new secret and save");
            byte[] bytes = new byte[64];
            new SecureRandom().nextBytes(bytes);
            jwtSecret = Base64.getEncoder().encodeToString(bytes);
            SettingItem settingItem = new SettingItem("other.jwtSecret", jwtSecret, String.class);
            settingService.setSetting(settingItem);
        }
    }

    // 生成 JWT token
    public String generateToken(User user) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getQQNumber()))
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    // 从 Jwt token 获取用户
    public User getUser(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String subject = claims.getSubject();
        if (subject.equals("robot") && robotTokenService.existByToken(token)) {
            return User.ANONYMOUS;
        } else {
            return userService.findByQQNumber(Long.parseLong(subject)).orElseThrow();
        }
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
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
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

    public boolean isUserHasPermission(User user ,String permissionName) {
        final Role role = roleService.findByType(user.getRole().getType()).orElse(Role.ANONYMOUS);
        for (Permission permission : role.getPermissions()) {
            if (permission.getName().equals(permissionName)/*&&authority.getPermissionType().equals(permissionType)*/) {
                return true;
            }
        }
        return false;
    }
    
    public String generateRobotToken(User applicant,String id) {
        Date expireDate = new Date(Long.MAX_VALUE);
        String token = Jwts.builder()
                .setSubject("robot")
                .setIssuer(String.valueOf(applicant.getQQNumber()))
                .setIssuedAt(new Date())
                .setHeaderParam("generateTime", String.valueOf(System.currentTimeMillis()))
                .setHeaderParam("entryId", id)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
        return token;
    }
}
