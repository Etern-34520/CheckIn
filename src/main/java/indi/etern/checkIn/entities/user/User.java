package indi.etern.checkIn.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.utils.ApplicationContextUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User implements UserDetails, OAuth2User {
    public static final User ANONYMOUS;

    static {
        ANONYMOUS = new User();
        ANONYMOUS.name = "anonymous";
        ANONYMOUS.QQNumber = 0;
        ANONYMOUS.role = Role.ANONYMOUS;
    }

    @Setter
    @Getter
    protected String name;

    @JsonProperty("qq")
    @Id
    protected long QQNumber;

    @Setter
    @Getter
    @JsonIgnore
    protected String password;

    @Getter
    @JsonIgnore
    @Fetch(value = FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE},
            fetch = FetchType.LAZY)
    protected Set<OAuth2Binding> oauth2Bindings = new HashSet<>();

    @JsonSerialize(using = Role.TypeSerializer.class)
    @Getter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_TYPE", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    protected Role role;

    @Setter
    protected boolean enabled = true;

    public User(String name, long QQNumber, String password) {
        this.name = name;
        this.QQNumber = QQNumber;
        PasswordEncoder encoder = ApplicationContextUtils.getApplicationContext().getBean(PasswordEncoder.class);
        this.password = password == null ? null : encoder.encode(password);
//        role = Role.ofName("user",null);
    }

    public User() {
    }

    @JsonIgnore
    public long getQQNumber() {
        return QQNumber;
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
    @JsonIgnore
    public String getUsername() {
        return name;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getPermissions();
    }

    /**
     * @return 账户是否不过期
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @return 账户是否不被锁定
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @return 密码是否不过期
     */
    @JsonIgnore
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
