package indi.etern.checkIn.entities.convertor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Converter
public class ListJsonConvertor implements AttributeConverter<List<?>,String> {
    @Autowired
    private ObjectMapper objectMapper;
    
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
