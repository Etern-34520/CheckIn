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
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST,  CascadeType.REFRESH, CascadeType.REFRESH})
    @JoinColumn(value = "group_name", referencedColumnName = "value")
    protected PermissionGroup group;
*/
    @Id
    @Column(name = "ID", columnDefinition = "char(36)")
    private String id;
    @Column(name = "description")
    @Setter
    private String description;
    @Column(name = "name", nullable = false)
    @Setter
    private String name;
    
/*
    @Column(value = "type", nullable = false)
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
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Permission && ((Permission) obj).id.equals(this.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
