package indi.etern.checkIn.entities.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Converter
public class MapConverter implements AttributeConverter<Map<?,?>,String> {
    final ObjectMapper objectMapper;
    
    public MapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(Map<?,?> attribute) {
        return attribute==null||attribute.isEmpty()?null:objectMapper.writeValueAsString(attribute);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public Map<String,String> convertToEntityAttribute(String dbData) {
        return dbData==null?null:objectMapper.readValue(dbData,Map.class);
    }
}
