package indi.etern.checkIn.entities.setting.grading;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String description;
    private String message;
    private CreatingUserStrategy creatingUserStrategy;
    
    protected GradingLevel() {}
    
    public enum CreatingUserStrategy {
        NOT_CREATE, CREATE_DISABLED, CREATE_ENABLED_AFTER_VALIDATED, CREATE_ENABLED;
        
        public static CreatingUserStrategy ofOrElse(String creatingUserStrategy, CreatingUserStrategy creatingUserStrategy1) {
            try {
                return CreatingUserStrategy.valueOf(creatingUserStrategy);
            } catch (IllegalArgumentException e) {
                return creatingUserStrategy1;
            }
        }
    }
}