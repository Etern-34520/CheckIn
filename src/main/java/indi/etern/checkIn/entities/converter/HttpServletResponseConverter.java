package indi.etern.checkIn.entities.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@Converter
public class HttpServletResponseConverter implements AttributeConverter<HttpServletResponse,String> {
    private final ObjectMapper objectMapper;
    public HttpServletResponseConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(HttpServletResponse attribute) {
        return objectMapper.writeValueAsString(attribute);
    }
    
    @SneakyThrows
    @Override
    public HttpServletResponse convertToEntityAttribute(String dbData) {
        return objectMapper.readValue(dbData,HttpServletResponse.class);
    }
}