package indi.etern.checkIn.throwable.action;

import lombok.Getter;

@Getter
public class FieldClassNotMatchException extends ActionException {
    private final String fieldName;
    private final Class<?> expectedFieldType;
    public FieldClassNotMatchException(String fieldName, Class<?> expectedType) {
        super("field class not match. name: " + fieldName + ", expected " + expectedType);
        this.fieldName = fieldName;
        this.expectedFieldType = expectedType;
    }
}
