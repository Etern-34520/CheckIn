package indi.etern.checkIn.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Getter
@Entity
@Table(name = "PERMISSIONS")
@JsonIgnoreProperties("authority")
public class Permission implements GrantedAuthority {
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
    
    public Permission(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
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
    
    @Override
    public String getAuthority() {
        return name;
    }
}
