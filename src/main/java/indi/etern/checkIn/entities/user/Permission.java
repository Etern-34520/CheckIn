package indi.etern.checkIn.entities.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "PERMISSIONS")
public class Permission {
/*
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.REFRESH})
    @JoinColumn(name = "group_name", referencedColumnName = "name")
    protected PermissionGroup group;
*/
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "description")
    @Setter
    private String description;
    @Column(name = "name", nullable = false)
    @Setter
    private String name;
    
/*
    @Column(name = "type", nullable = false)
    @Setter
    private PermissionType type;
*/
    
    public Permission(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
//        this.type = type;
    }
    
    protected Permission() {
    }
    
}
