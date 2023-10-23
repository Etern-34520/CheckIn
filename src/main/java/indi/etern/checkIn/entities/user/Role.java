package indi.etern.checkIn.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Entity
public class Role {
    @Id
    private String type;
    @OneToMany(mappedBy = "role")
    private final Set<User> users = new HashSet<>();
    protected static Map<String,Role> roleMap;
    
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
    
    public Set<User> getUsers() {
        return users;
    }
    
    public String getType() {
        return type;
    }
//    @Override
    public String getStaticHash() {
        return type;
    }
    @Override
    public boolean equals(Object object){
        if (object instanceof Role role) {
            return role.type.equals(this.type);
        } else {
            return false;
        }
    }
}
