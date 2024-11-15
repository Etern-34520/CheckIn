package indi.etern.checkIn.entities.user;

import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.auth.Authority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Entity
@Table(name = "Users")
public class User implements UserDetails {
    @Setter
    @Getter
    protected String name;
    @Getter
    @Id
    protected long QQNumber;
    @Setter
    @Getter
    protected String password;
    @Getter
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_TYPE")
//    @NotFound(action = NotFoundAction.IGNORE)
    protected Role role;
/*
    @PostLoad
    private void postLoad() {
        final Role role1 = Role.roleMap.get(role.getType());
        if (role1 != null) {
            role = role1;
        } else {
            Role.roleMap.put(role.getType(), role);
        }
    }
*/
    
    @Setter
    protected boolean enabled = true;
    
    public User(String name, long QQNumber, String password) {
        this.name = name;
        this.QQNumber = QQNumber;
        PasswordEncoder encoder = CheckInApplication.applicationContext.getBean(PasswordEncoder.class);
        this.password = password == null ? null : encoder.encode(password);
//        role = Role.getInstance("user",null);
    }
    
    public User() {
    }
    
    public static User exampleOfName(String name) {
        final User user = new User();
        user.name = name;
        user.QQNumber = 0;
        return user;
    }
    
    public static User exampleOfQQNumber(int qqNumber) {
        final User user = new User();
        user.QQNumber = qqNumber;
        user.name = "";
        return user;
    }
    
    public void setQQNumber(int QQNumber) {
        this.QQNumber = QQNumber;
    }
    
    public void setRole(Role role) {
        if (role == null) {
            this.role = null;
        } else {
            if (this.role != null)
                this.role.getUsers().remove(this);
            this.role = role;
            role.getUsers().add(this);
        }
    }
    
    @Override
    public String toString() {
        return "{value:\"" + name + "\",QQ:\"" + QQNumber + "\",password:\"" + password + "\",role:\"" + role.getType() + "\"}";
    }
    
    public Map<String, Object> toDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", name);
        dataMap.put("qq", QQNumber);
        dataMap.put("role", role.getType());
//        dataMap.put("userStatus",enabled?"启用":"禁用");
        dataMap.put("enabled", enabled);
        return dataMap;
    }
    
    public LinkedHashMap<String,Object> toMap() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("qq", QQNumber);
        map.put("role", role.getType());
        map.put("enabled", enabled);
        return map;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof User user) {
            return user.QQNumber == this.QQNumber;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return ((Long) QQNumber).hashCode();
    }
    
    @Override
    public String getUsername() {
        return name;
    }
    
    public void setUsername(String username) {
        this.name = username;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<Authority> authorities = new ArrayList<>();
        for (Permission permission : role.getPermissions()) {
            authorities.add(new Authority(permission.getName()/*, PermissionType.WEB*/));//FIXME
        }
        return authorities;
    }
    
    /**
     * @return 账户是否不过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * @return 账户是否不被锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * @return 密码是否不过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * @return 账户是否可用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
