package indi.etern.checkIn.entities.user;

import indi.etern.checkIn.service.dao.RoleService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Entity
@Table(name = "ROLE")
public class Role {
    protected static Map<String, Role> roleMap = new HashMap<>();
    @Id
    private String type;
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    @ManyToMany(cascade = {CascadeType.PERSIST,  CascadeType.REFRESH, CascadeType.REFRESH}, fetch = FetchType.EAGER)
//    @JoinColumn(value = "ROLE_TYPE",referencedColumnName = "TYPE")
//    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(name = "ROLE_PERMISSION_MAPPING",
            joinColumns = @JoinColumn(name = "ROLE_TYPE", referencedColumnName = "TYPE"),
            inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "ID"))
    private Set<Permission> permissions = new HashSet<>();
    @Setter
    private int level;
    
    public Role(String type,int level) {
        this.type = type;
        this.level = level;
    }
    
    protected Role() {
    }
    
    static public Role getInstance(String type,Integer level) {
        if (roleMap.get(type) == null) {
            if (level == null) {
                throw new EntityNotFoundException("Role not found \""+type+"\"");
            } else if (level == -1) {
                final Optional<Role> optionalRole = RoleService.singletonInstance.findById(type);
                if (optionalRole.isPresent()) {
                    return optionalRole.get();
                } else {
                    level = RoleService.singletonInstance.count();
                }
            }
            Role role = new Role(type, level);
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
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
}