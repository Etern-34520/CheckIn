package indi.etern.checkIn.entities.convertor;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StringListConverter implements AttributeConverter<List<String>,String> {
    final Gson gson;
    
    public StringListConverter(Gson gson) {
        this.gson = gson;
    }
    
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return gson.toJson(attribute);
    }
    
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData,List.class);
    }
}
