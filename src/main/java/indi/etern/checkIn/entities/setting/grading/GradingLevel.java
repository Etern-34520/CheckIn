package indi.etern.checkIn.entities.setting.grading;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.entities.user.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "grading_levels")
@AllArgsConstructor
@Builder
@JsonSerialize
@Getter
@Setter
public class GradingLevel {
    @Id
    @Column(columnDefinition = "char(36)")
    private String id;
    private String name;
    @Column(columnDefinition = "char(7)")
    private String colorHex;
    @Column(columnDefinition = "text")
    private String description;
    @Column(columnDefinition = "text")
    private String message;
    private CreatingUserStrategy creatingUserStrategy;
    private int levelIndex;
    @OneToOne
    @JoinColumn(name = "role_type", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonSerialize(using = Role.TypeSerializer.class)
    private Role creatingUserRole;
    
    protected GradingLevel() {}
    
    public enum CreatingUserStrategy {
        NOT_CREATE, CREATE_AND_DISABLED, CREATE_AND_ENABLED_AFTER_VALIDATED, CREATE_AND_ENABLED;
        
        public static CreatingUserStrategy ofOrElse(String creatingUserStrategy, CreatingUserStrategy creatingUserStrategy1) {
            try {
                return CreatingUserStrategy.valueOf(creatingUserStrategy);
            } catch (IllegalArgumentException e) {
                return creatingUserStrategy1;
            }
        }
    }
}