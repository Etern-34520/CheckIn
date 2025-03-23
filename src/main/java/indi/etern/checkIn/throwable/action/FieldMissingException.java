package indi.etern.checkIn.throwable.action;

import lombok.Getter;

@Getter
public class FieldMissingException extends ActionException {
    private final String fieldName;
    public FieldMissingException(String fieldName) {
        super("field missing: " + fieldName);
        this.fieldName = fieldName;
    }
}
