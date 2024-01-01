package indi.etern.checkIn.entities.user;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Entity
@Table(name = "ROLE")
public class Role {
    protected static Map<String, Role> roleMap = new HashMap<>();
    @Id
    private String type;
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.REFRESH}, fetch = FetchType.EAGER)
//    @JoinColumn(name = "ROLE_TYPE",referencedColumnName = "TYPE")
//    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "ROLE_PERMISSION_MAPPING",
            joinColumns = @JoinColumn(name = "ROLE_TYPE", referencedColumnName = "TYPE"),
            inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID"))
    private Set<Permission> permissions = new HashSet<>();
    
    public Role(String type) {
        this.type = type;
    }
    
    protected Role() {
    }
    
    static public Role getInstance(String type) {
        if (roleMap.get(type) == null) {
            Role role = new Role(type);
            roleMap.put(type, role);
            return role;
        } else {
            return roleMap.get(type);
        }
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof Role role) {
            return role.type.equals(this.type);
        } else {
            return false;
        }
    }
    
}
