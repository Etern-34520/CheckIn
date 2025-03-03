package indi.etern.checkIn.entities.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "PERMISSIONS")
public class Permission {
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "group_name", referencedColumnName = "name", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonSerialize(using = PermissionGroup.NameSerializer.class)
    protected PermissionGroup group;
    
    @Id
    @Column(name = "ID", columnDefinition = "char(36)")
    private String id;
    @Column(name = "description")
    @Setter
    private String description;
    @Column(name = "name", nullable = false)
    @Setter
    private String name;
    
    /*@Getter
    @ManyToMany(mappedBy = "permissions")//FIXME foreign
    @JsonIgnore
    private List<Role> rolesHasThisPermission;
    */
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
