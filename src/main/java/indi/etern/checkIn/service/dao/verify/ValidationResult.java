package indi.etern.checkIn.service.dao.verify;

import indi.etern.checkIn.dto.manage.IssueDTO;
import indi.etern.checkIn.entities.converter.MapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Embeddable
@Getter
public class ValidationResult {
    @Convert(converter = MapConverter.class)
    @Column(name = "validation_errors", columnDefinition = "varchar(4096)")
    private final Map<String, IssueDTO> errors = new HashMap<>();
    @Convert(converter = MapConverter.class)
    @Column(name = "validation_warnings", columnDefinition = "varchar(4096)")
    private final Map<String, IssueDTO> warnings = new HashMap<>();
    @Setter
    @Column(name = "show_validation_errors")
    private boolean showError = false;
    @Setter
    @Column(name = "show_validation_warnings")
    private boolean showWarning = false;
    
    public void addError(String key, String message) {
        errors.put(key, new IssueDTO(message));
    }
    
    public void addWarning(String key, String message) {
        warnings.put(key, new IssueDTO(message));
    }
}
