package indi.etern.checkIn.entities.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Converter
public class ListJsonConverter implements AttributeConverter<List<?>,String> {
    private final ObjectMapper objectMapper;
    
    public ListJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(List<?> attribute) {
        return objectMapper.writeValueAsString(attribute);
    }
    
    @SneakyThrows
    @Override
    public List<?> convertToEntityAttribute(String dbData) {
        return objectMapper.readValue(dbData,List.class);
    }
}
