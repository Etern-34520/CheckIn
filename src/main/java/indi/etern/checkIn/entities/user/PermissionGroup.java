package indi.etern.checkIn.entities.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PERMISSION_GROUPS")
@Getter
public class PermissionGroup {
    @Id
    public String name;
    @Setter
    public String description;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST,  CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "group_name", referencedColumnName = "name")
    private List<Permission> permissions;
    
    public PermissionGroup(String name) {
        this.name = name;
        this.permissions = new ArrayList<>();
    }
    
    protected PermissionGroup() {}
    
    public void add(Permission permission) {
        permissions.add(permission);
//        permission.setGroup(this);
    }
}
