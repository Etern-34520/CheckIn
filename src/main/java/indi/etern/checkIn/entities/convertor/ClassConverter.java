package indi.etern.checkIn.entities.convertor;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter
public class ClassConverter implements AttributeConverter<Class<?>, String> {
    @Override
    public String convertToDatabaseColumn(Class<?> attribute) {
        return attribute.getName();
    }

    @Override
    public Class<?> convertToEntityAttribute(String dbData) {
        try {
            return Class.forName(dbData);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
