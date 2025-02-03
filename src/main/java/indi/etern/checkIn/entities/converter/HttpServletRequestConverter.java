package indi.etern.checkIn.entities.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@Converter
public class HttpServletRequestConverter implements AttributeConverter<HttpServletRequest,String> {
    private final ObjectMapper objectMapper;
    public HttpServletRequestConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(HttpServletRequest attribute) {
        return objectMapper.writeValueAsString(attribute);
    }
    
    @SneakyThrows
    @Override
    public HttpServletRequest convertToEntityAttribute(String dbData) {
        return objectMapper.readValue(dbData,HttpServletRequest.class);
    }
}