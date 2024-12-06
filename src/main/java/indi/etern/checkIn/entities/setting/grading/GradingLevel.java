package indi.etern.checkIn.entities.setting.grading;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    private String id;
    private String name;
    private String colorHex;
    private String description;
    private String message;
    
    protected GradingLevel() {}
}
