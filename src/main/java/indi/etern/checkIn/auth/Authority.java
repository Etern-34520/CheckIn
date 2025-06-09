package indi.etern.checkIn.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class Authority implements GrantedAuthority {
    private final String name;
    
    public Authority(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}
