package indi.etern.checkIn.entities.user;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Getter
@Entity
public class Role {
    @Id
    private String type;
    
    @OneToMany(mappedBy = "role",cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH,CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private final Set<User> users = new HashSet<>();
    protected static Map<String,Role> roleMap = new HashMap<>();
    
    protected Role(String type){
        this.type = type;
    }
    
    protected Role() {
    
    }
    
    static public Role getInstance(String type){
        if (roleMap.get(type)==null){
            Role role = new Role(type);
            roleMap.put(type, role);
            return role;
        } else {
            return roleMap.get(type);
        }
    }
    
    @Override
    public boolean equals(Object object){
        if (object instanceof Role role) {
            return role.type.equals(this.type);
        } else {
            return false;
        }
    }
    
    public SimpleGrantedAuthority getSimpleGrantedAuthority(){
        return new SimpleGrantedAuthority(type);
    }
}
