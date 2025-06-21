package indi.etern.checkIn.service.dao.verify;

import indi.etern.checkIn.entities.converter.MapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Embeddable
@Getter
public class ValidationResult {
    @Convert(converter = MapConverter.class)
    @Column(name = "validation_errors", columnDefinition = "varchar(4096)")
    private final Map<String, String> errors = new HashMap<>();
    @Convert(converter = MapConverter.class)
    @Column(name = "validation_warnings", columnDefinition = "varchar(4096)")
    private final Map<String, String> warnings = new HashMap<>();
    
    public void addError(String key, String message) {
        errors.put(key, message);
    }
    
    public void addWarning(String key, String message) {
        warnings.put(key, message);
    }
}
