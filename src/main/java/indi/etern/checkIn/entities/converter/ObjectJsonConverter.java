package indi.etern.checkIn.entities.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
@Converter
public class ObjectJsonConverter implements AttributeConverter<Object,String> {
    final ObjectMapper objectMapper;
    
    public ObjectJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(Object attribute) {
        return attribute == null ? null:attribute.getClass().getName() + "#" + objectMapper.writeValueAsString(attribute);
    }
    
    @SneakyThrows
    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        final String[] split = dbData.split("#",2);
        if (split.length == 2) {
            String className = split[0];
            return objectMapper.readValue(split[1], Class.forName(className));
        } else {
            return objectMapper.readValue(dbData, LinkedHashMap.class);
        }
    }
}
