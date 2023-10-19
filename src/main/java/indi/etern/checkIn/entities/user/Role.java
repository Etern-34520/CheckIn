package indi.etern.checkIn.entities.user;

import indi.etern.checkIn.dao.PersistableWithStaticHash;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Role implements PersistableWithStaticHash {
    private final String type;
    private final Set<User> users = new HashSet<>();
    protected static Map<String,Role> roleMap;
    
    protected Role(String type){
        this.type = type;
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
    @Override
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
