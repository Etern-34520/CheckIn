package indi.etern.checkIn.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class Authority implements GrantedAuthority {
    private final PermissionType permissionType;
    private final String name;
    
    public Authority(String name, PermissionType permissionType) {
        this.name = name;
        this.permissionType = permissionType;
    }

    @Override
    public String getAuthority() {
        return permissionType.name() + ":" + name;
    }
}
