package indi.etern.checkIn.entities.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
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
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
//    @JoinColumn(name = "group_name", referencedColumnName = "name", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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
    
    public static class NameSerializer extends JsonSerializer<PermissionGroup> {
        @Override
        public void serialize(PermissionGroup permissionGroup, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(permissionGroup.name);
        }
    }
}
